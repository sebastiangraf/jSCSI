/**
 * 
 */
package org.jscsi.target.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jscsi.target.storage.buffering.BufferedTaskWorker;
import org.jscsi.target.storage.buffering.BufferedWriteTask;
import org.jscsi.target.storage.buffering.BufferedWriteTask.PoisonTask;
import org.jscsi.target.storage.buffering.Collision;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class JCloudsStorageModule implements IStorageModule {

    /** Number of Blocks in one Cluster. */
    public static final int BLOCK_IN_CLUSTER = 512;

    private final long mNumberOfCluster;

    private final String mContainerName;

    private final BlobStore mStore;

    private final BlobStoreContext mContext;

    /**
     * The service that holds the BufferedTaskWorker
     */
    private final ExecutorService mWriterService;

    /**
     * The worker to process write tasks
     */
    private final BufferedTaskWorker mWorker;

    /**
     * Creates a new {@link JCloudsStorageModules} backed by the specified
     * file. If no such file exists, a {@link FileNotFoundException} will be
     * thrown.
     * 
     * @param pSizeInBlocks
     *            blocksize for this module
     * 
     * @throws FileNotFoundException
     *             if the specified file does not exist
     */
    public JCloudsStorageModule(final long pSizeInBlocks, final File pFile) {
        try {
            mNumberOfCluster = pSizeInBlocks / BLOCK_IN_CLUSTER;
            mContainerName = "grave9283746";
            // Init
            mContext =
                ContextBuilder.newBuilder("aws-s3").credentials(getCredentials()[0], getCredentials()[1])
                    .buildView(BlobStoreContext.class);

            // Create Container
            mStore = mContext.getBlobStore();
            if (!mStore.containerExists(mContainerName)) {
                mStore.createContainerInLocation(null, mContainerName);
            }
            mWriterService = Executors.newSingleThreadExecutor();
            mWorker = new BufferedTaskWorker(mStore, mContainerName);
            mWriterService.submit(mWorker);
            mWriterService.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int checkBounds(long logicalBlockAddress, int transferLengthInBlocks) {
        if (logicalBlockAddress < 0 || logicalBlockAddress >= getSizeInBlocks()) {
            return 1;
        } else
        // if the logical block address is in bounds but the transferlength either exceeds
        // the device size or is faulty return 2
        if (transferLengthInBlocks < 0 || logicalBlockAddress + transferLengthInBlocks > getSizeInBlocks()) {
            return 2;
        } else {
            return 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSizeInBlocks() {
        return mNumberOfCluster * VIRTUAL_BLOCK_SIZE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(byte[] bytes, int bytesOffset, int length, long storageIndex) throws IOException {

        // Overwriting segments in the byte array using the writer tasks that are still in progress.
        List<Collision> collisions = mWorker.checkForCollisions(length, storageIndex);

        // Using the most recent revision
        if (bytesOffset + length > bytes.length) {
            throw new IOException();
        }
        int startIndex = (int)(storageIndex / (BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));
        int startIndexOffset = (int)(storageIndex % (BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));

        int endIndex =
            (int)((storageIndex + length) / (BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));
        int endIndexMax =
            (int)((storageIndex + length) % (BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));

        ByteArrayDataOutput output = ByteStreams.newDataOutput(length);

        for (long i = startIndex; i <= endIndex; i++) {

            Blob blob;
            byte[] val;
            if (mStore.blobExists(mContainerName, Long.toString(i))) {
                blob = mStore.getBlob(mContainerName, Long.toString(i));
                val = ByteStreams.toByteArray(blob.getPayload().getInput());
            } else {
                blob = mStore.blobBuilder(Long.toString(i)).build();
                val = new byte[IStorageModule.VIRTUAL_BLOCK_SIZE * JCloudsStorageModule.BLOCK_IN_CLUSTER];
            }

            if (i == startIndex && i == endIndex) {
                output.write(val, startIndexOffset, length);
            } else if (i == startIndex) {
                output.write(val, startIndexOffset, (BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE)
                    - startIndexOffset);
            } else if (i == endIndex) {
                output.write(val, 0, endIndexMax);
            } else {
                output.write(val);
            }

        }

        System.arraycopy(output.toByteArray(), 0, bytes, bytesOffset, length);

        for (Collision collision : collisions) {
            if (collision.getStart() != storageIndex) {
                System.arraycopy(collision.getBytes(), 0, bytes,
                    (int)(bytesOffset + (collision.getStart() - storageIndex)), collision.getBytes().length);
            } else {
                System.arraycopy(collision.getBytes(), 0, bytes, bytesOffset, collision.getBytes().length);
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] bytes, int bytesOffset, int length, long storageIndex) throws IOException {
        try {
            mWorker.newTask(new BufferedWriteTask(bytes, bytesOffset, length, storageIndex));
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {

        try {
            mWorker.newTask(new PoisonTask());
            mWriterService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
        mContext.close();
    }

    private static String[] getCredentials() {
        File userStore =
            new File(System.getProperty("user.home"), new StringBuilder(".credentials")
                .append(File.separator).append("aws.properties").toString());
        if (!userStore.exists()) {
            return new String[0];
        } else {
            Properties props = new Properties();
            try {
                props.load(new FileReader(userStore));
                return new String[] {
                    props.getProperty("access"), props.getProperty("secret")
                };

            } catch (IOException exc) {
                throw new RuntimeException(exc);
            }
        }

    }
}
