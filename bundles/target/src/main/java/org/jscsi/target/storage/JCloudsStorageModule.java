/**
 * 
 */
package org.jscsi.target.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.filesystem.reference.FilesystemConstants;

import com.google.common.annotations.Beta;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * JClouds-Binding to store blocks as buckets in clouds-backends.
 * This class utilizes caching as well as multithreaded writing to improve performance.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Beta
public class JCloudsStorageModule implements IStorageModule {

    /** Number of Blocks in one Cluster. */
    public static final int BLOCK_IN_CLUSTER = 512;

    /** Number of Bytes in Bucket. */
    public final static int SIZE_PER_BUCKET = BLOCK_IN_CLUSTER * VIRTUAL_BLOCK_SIZE;

    private final long mNumberOfCluster;

    private final String mContainerName;

    private final BlobStore mStore;

    private final BlobStoreContext mContext;

    private final Cache<Integer, byte[]> mCache;

    private int lastIndexWritten;
    private Blob lastBlobWritten;

    private final ExecutorService mWriterService;
    private final ConcurrentHashMap<Integer, Future<Void>> mRunningTasks;

    /**
     * Creates a new {@link JCloudsStorageModule} backed by the specified
     * file. If no such file exists, a {@link FileNotFoundException} will be
     * thrown.
     * 
     * @param pSizeInBlocks
     *            blocksize for this module
     * @param pFile
     *            local storage, not used over here
     * 
     */
    public JCloudsStorageModule(final long pSizeInBlocks, final File pFile) {
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
        mWriterService = Executors.newCachedThreadPool();
        mRunningTasks = new ConcurrentHashMap<Integer, Future<Void>>();
        mCache = CacheBuilder.newBuilder().maximumSize(2000).build();
        lastIndexWritten = -1;
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

        final int bucketIndex = (int)(storageIndex / SIZE_PER_BUCKET);
        final int bucketOffset = (int)(storageIndex % SIZE_PER_BUCKET);
        try {
            storeBucket(-1, null);

            byte[] data = mCache.getIfPresent(bucketIndex);
            if (data == null) {
                data = getData(bucketIndex);
                mCache.put(bucketIndex, data);
            }

            final ByteArrayDataOutput output = ByteStreams.newDataOutput(bytes.length);
            int length = -1;
            if (bucketOffset + bytes.length > SIZE_PER_BUCKET) {
                length = SIZE_PER_BUCKET - bucketOffset;
            } else {
                length = bytes.length;
            }
            output.write(data, bucketOffset, length);

            if (bucketOffset + bytes.length > SIZE_PER_BUCKET) {
                data = getData(bucketIndex);
                mCache.put(bucketIndex + 1, data);
                output.write(data, 0, bytes.length - (SIZE_PER_BUCKET - bucketOffset));
            }

            System.arraycopy(output.toByteArray(), 0, bytes, 0, bytes.length);
        } catch (IOException | InterruptedException | ExecutionException exc) {
            throw new IOException(exc);
        }
    }

    private final void storeBucket(int pBucketId, Blob pBlob) throws InterruptedException, ExecutionException {
        if (lastIndexWritten != pBucketId && lastBlobWritten != null) {
            if (mRunningTasks.containsKey(lastIndexWritten)) {
                mRunningTasks.remove(lastIndexWritten).get();
            }
            mRunningTasks.put(lastIndexWritten, mWriterService.submit(new WriteTask(lastBlobWritten, mStore,
                mContainerName)));
        }
        lastIndexWritten = pBucketId;
        lastBlobWritten = pBlob;
    }

    private final byte[] getData(int pBucketId) throws IOException, InterruptedException, ExecutionException {
        if (mRunningTasks.containsKey(pBucketId)) {
            mRunningTasks.remove(pBucketId).get();
        }
        byte[] data;
        Blob blob = mStore.getBlob(mContainerName, Integer.toString(pBucketId));

        if (blob == null) {
            data = new byte[SIZE_PER_BUCKET];
        } else {
            data = ByteStreams.toByteArray(blob.getPayload().getInput());
            while (data.length == 0) {
                blob = mStore.getBlob(mContainerName, Integer.toString(pBucketId));
                data = ByteStreams.toByteArray(blob.getPayload().getInput());
            }
        }
        return data;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws Exception
     */
    @Override
    public void write(byte[] bytes, long storageIndex) throws IOException {
        final int bucketIndex = (int)(storageIndex / SIZE_PER_BUCKET);
        final int bucketOffset = (int)(storageIndex % SIZE_PER_BUCKET);
        try {
            byte[] data = mCache.getIfPresent(bucketIndex);
            if (data == null) {
                data = getData(bucketIndex);
            }

            Blob blob = mStore.blobBuilder(Integer.toString(bucketIndex)).build();
            System.arraycopy(bytes, 0, data, bucketOffset, bytes.length + bucketOffset > SIZE_PER_BUCKET
                ? SIZE_PER_BUCKET - bucketOffset : bytes.length);
            blob.setPayload(data);
            storeBucket(bucketIndex, blob);
            mCache.put(bucketIndex, data);

            if (bucketOffset + bytes.length > SIZE_PER_BUCKET) {
                data = mCache.getIfPresent(bucketIndex + 1);
                if (data == null) {
                    data = getData(bucketIndex + 1);
                }
                blob = mStore.blobBuilder(Integer.toString(bucketIndex + 1)).build();

                System.arraycopy(bytes, SIZE_PER_BUCKET - bucketOffset, data, 0, bytes.length
                    - (SIZE_PER_BUCKET - bucketOffset));
                blob.setPayload(data);
                storeBucket(bucketIndex + 1, blob);
                mCache.put(bucketIndex + 1, data);
            }
        } catch (IOException | InterruptedException | ExecutionException exc) {
            throw new IOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        mWriterService.shutdown();
        mContext.close();
    }

    /**
     * Getting credentials for aws from homedir/.credentials
     * 
     * @return a two-dimensional String[] with login and password
     */
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

    /**
     * Single task to write data to the cloud.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    class WriteTask implements Callable<Void> {
        /**
         * The bytes to buffer.
         */
        final Blob mBlob;

        /**
         * BlobStore to store to.
         */
        final BlobStore mBlobStore;

        /**
         * Container name to write to.
         */
        final String mContainer;

        WriteTask(Blob pBlob, final BlobStore pBlobStore, final String pContainer) {
            this.mBlob = pBlob;
            this.mBlobStore = pBlobStore;
            this.mContainer = pContainer;
        }

        @Override
        public Void call() throws Exception {
            boolean finished = false;

            while (!finished) {
                try {
                    mStore.putBlob(mContainer, mBlob);
                } catch (Exception exc) {

                }
                finished = true;
            }

            return null;
        }
    }
}
