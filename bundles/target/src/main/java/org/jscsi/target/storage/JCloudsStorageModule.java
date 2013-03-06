/**
 * 
 */
package org.jscsi.target.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.jscsi.target.storage.buffering.BufferedTaskWorker;
import org.jscsi.target.storage.buffering.BufferedWriteTask;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class JCloudsStorageModule implements IStorageModule {

    /** Number of Blocks in one Cluster. */
    public static final int BLOCK_IN_CLUSTER = 512;

    /** Number of Bytes in Bucket. */
    public final static int SIZE_PER_BUCKET = BLOCK_IN_CLUSTER * VIRTUAL_BLOCK_SIZE;

    private final long mNumberOfCluster;

    private final String mContainerName;

    private final BlobStore mStore;

    private final BlobStoreContext mContext;

    // /**
    // * The service that holds the BufferedTaskWorker
    // */
    // private final ExecutorService mWriterService;

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
            mNumberOfCluster = 2097152 / BLOCK_IN_CLUSTER;
            mContainerName = "grave9283746";
            String[] credentials = getCredentials();
            if (credentials.length == 0) {
                Properties properties = new Properties();
                properties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, pFile.getAbsolutePath());
                mContext =
                    ContextBuilder.newBuilder("filesystem").overrides(properties).credentials("testUser",
                        "testPass").buildView(BlobStoreContext.class);
            } else {
                mContext =
                    ContextBuilder.newBuilder("aws-s3").credentials(getCredentials()[0], getCredentials()[1])
                        .buildView(BlobStoreContext.class);
            }

            // Create Container
            mStore = mContext.getBlobStore();
            if (!mStore.containerExists(mContainerName)) {
                mStore.createContainerInLocation(null, mContainerName);
            }
            // mWriterService = Executors.newSingleThreadExecutor();
            mWorker = new BufferedTaskWorker(mStore, mContainerName);
            // mWriterService.submit(mWorker);
            // mWriterService.shutdown();
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
    public void read(byte[] bytes, long storageIndex) throws IOException {

        // Overwriting segments in the byte array using the writer tasks that are still in progress.
        // List<Collision> collisions = mWorker.checkForCollisions(length, storageIndex);

        int startIndex = (int)(storageIndex / SIZE_PER_BUCKET);
        int startIndexOffset = (int)(storageIndex % SIZE_PER_BUCKET);

        int endIndex = (int)((storageIndex + bytes.length) / SIZE_PER_BUCKET);
        int endIndexMax = (int)((storageIndex + bytes.length) % SIZE_PER_BUCKET);

        ByteArrayDataOutput output = ByteStreams.newDataOutput(bytes.length);

        // getting the blob at the first cluster
        byte[] firstCluster = getBlobOrBuild(Long.toString(startIndex));
        output.write(firstCluster, startIndexOffset, SIZE_PER_BUCKET - startIndexOffset < bytes.length
            ? SIZE_PER_BUCKET - startIndexOffset : bytes.length);

        // getting all clusters in between
        for (long i = startIndex + 1; i < endIndex; i++) {
            byte[] nextCluster = getBlobOrBuild(Long.toString(i));
            output.write(nextCluster, 0, SIZE_PER_BUCKET);
        }

        // checking if there is a last cluster
        if (startIndex != endIndex) {
            byte[] lastCluster = getBlobOrBuild(Long.toString(endIndex));
            output.write(lastCluster, 0, endIndexMax < SIZE_PER_BUCKET ? endIndexMax : SIZE_PER_BUCKET);

        }

        System.arraycopy(output.toByteArray(), 0, bytes, 0, bytes.length);

        // for (Collision collision : collisions) {
        // if (collision.getStart() != storageIndex) {
        // System.arraycopy(collision.getBytes(), 0, bytes,
        // (int)(bytesOffset + (collision.getStart() - storageIndex)), collision.getBytes().length);
        // } else {
        // System.arraycopy(collision.getBytes(), 0, bytes, bytesOffset, collision.getBytes().length);
        // }
        // }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] bytes, long storageIndex) throws IOException {
        // try {
        mWorker.performTask(new BufferedWriteTask(bytes, storageIndex));
        // } catch (InterruptedException e) {
        // throw new IOException(e);
        // }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {

        // try {
        // mWorker.newTask(new PoisonTask());
        // mWriterService.awaitTermination(10, TimeUnit.SECONDS);
        // } catch (InterruptedException e) {
        // throw new IOException(e);
        // }
        mContext.close();
    }

    private byte[] getBlobOrBuild(String pKey) throws IOException {

        Blob blob;
        byte[] val;
        blob = mStore.getBlob(mContainerName, pKey);
        if (blob != null) {
            val = ByteStreams.toByteArray(blob.getPayload().getInput());
        } else {
            blob = mStore.blobBuilder(pKey).build();
            val = new byte[SIZE_PER_BUCKET];
        }
        return val;
    }

    private static String[] getCredentials() {
        return new String[0];
        // File userStore =
        // new File(System.getProperty("user.home"), new StringBuilder(".credentials")
        // .append(File.separator).append("aws.properties").toString());
        // if (!userStore.exists()) {
        // return new String[0];
        // } else {
        // Properties props = new Properties();
        // try {
        // props.load(new FileReader(userStore));
        // return new String[] {
        // props.getProperty("access"), props.getProperty("secret")
        // };
        //
        // } catch (IOException exc) {
        // throw new RuntimeException(exc);
        // }
        // }

    }
}
