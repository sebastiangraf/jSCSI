/**
 * 
 */
package org.jscsi.target.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
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
import org.jclouds.domain.Location;
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

    // START DEBUG CODE
    private final static File writeFile = new File("/Users/sebi/Desktop/writeaccess.txt");
    private final static File readFile = new File("/Users/sebi/Desktop/readaccess.txt");
    private final static File uploadFile = new File("/Users/sebi/Desktop/uploadaccess.txt");
    private final static File downloadFile = new File("/Users/sebi/Desktop/downloadaccess.txt");
    static final FileWriter writer;
    static final FileWriter reader;
    static final FileWriter upload;
    static final FileWriter download;
    static {
        try {
            writer = new FileWriter(writeFile);
            reader = new FileWriter(readFile);
            upload = new FileWriter(uploadFile);
            download = new FileWriter(downloadFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Number of Blocks in one Cluster. */
    public static final int BLOCK_IN_CLUSTER = 512;

    private static final int BUCKETS_TO_PREFETCH = 3;

    /** Number of Bytes in Bucket. */
    public final static int SIZE_PER_BUCKET = BLOCK_IN_CLUSTER * VIRTUAL_BLOCK_SIZE;

    private static final int HASH_FOR_NULL_BUCKET = Arrays.hashCode(new byte[SIZE_PER_BUCKET]);

    private final long mNumberOfCluster;

    private final String mContainerName;

    private final BlobStore mStore;

    private final BlobStoreContext mContext;

    private final Cache<Integer, byte[]> mByteCache;

    private int lastIndexWritten;
    private Blob lastBlobWritten;

    private final ExecutorService mWriterService;
    private final ExecutorService mReaderService;
    private final ConcurrentHashMap<Integer, Future<Void>> mRunningWriteTasks;
    private final ConcurrentHashMap<Integer, Future<Void>> mRunningReadTasks;

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
        mNumberOfCluster = 8388608 / BLOCK_IN_CLUSTER;
        mContainerName = "grave9283747";
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
            Location locToSet = null;
            for (Location loc : mStore.listAssignableLocations()) {
                if (loc.getId().equals("eu-west-1")) {
                    locToSet = loc;
                    break;
                }
            }
            System.out.println(locToSet);
            mStore.createContainerInLocation(locToSet, mContainerName);
        }
        mWriterService = Executors.newCachedThreadPool();
        mReaderService = Executors.newCachedThreadPool();
        mRunningWriteTasks = new ConcurrentHashMap<Integer, Future<Void>>();
        mRunningReadTasks = new ConcurrentHashMap<Integer, Future<Void>>();
        mByteCache = CacheBuilder.newBuilder().maximumSize(100).build();
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
    public synchronized void read(byte[] bytes, long storageIndex) throws IOException {

        final int bucketIndex = (int)(storageIndex / SIZE_PER_BUCKET);
        final int bucketOffset = (int)(storageIndex % SIZE_PER_BUCKET);
        try {
            storeBucket(-1, null);

            // DEBUG CODE
            reader.write(bucketIndex + "," + storageIndex + "," + bucketOffset + "," + bytes.length + "\n");
            reader.flush();

            prefetchBuckets(bucketIndex);
            byte[] data = mByteCache.getIfPresent(bucketIndex);
            if (data == null) {
                mRunningReadTasks.remove(bucketIndex).get();
                data = mByteCache.getIfPresent(bucketIndex);
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
                data = mByteCache.getIfPresent(bucketIndex + 1);
                if (data == null) {
                    mRunningReadTasks.remove(bucketIndex + 1).get();
                    data = mByteCache.getIfPresent(bucketIndex + 1);
                }
                output.write(data, 0, bytes.length - (SIZE_PER_BUCKET - bucketOffset));
            }

            System.arraycopy(output.toByteArray(), 0, bytes, 0, bytes.length);
        } catch (Exception exc) {
            throw new IOException(exc);
        }
    }

    private final void prefetchBuckets(final int pBucketStartId) throws InterruptedException,
        ExecutionException {
        for (int i = pBucketStartId; i < pBucketStartId + BUCKETS_TO_PREFETCH; i++) {
            if (mRunningReadTasks.containsKey(i)) {
                mRunningReadTasks.remove(i).get();
            }
            mRunningReadTasks.put(i, mReaderService.submit(new ReadTask(i)));
        }

    }

    private final void storeBucket(int pBucketId, Blob pBlob) throws InterruptedException, ExecutionException {
        if (lastIndexWritten != pBucketId && lastBlobWritten != null) {
            if (mRunningWriteTasks.containsKey(lastIndexWritten)) {
                mRunningWriteTasks.remove(lastIndexWritten).cancel(false);
            }
            mRunningWriteTasks.put(lastIndexWritten, mWriterService.submit(new WriteTask(lastBlobWritten,
                lastIndexWritten)));
        }
        lastIndexWritten = pBucketId;
        lastBlobWritten = pBlob;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws Exception
     */
    @Override
    public synchronized void write(byte[] bytes, long storageIndex) throws IOException {
        final int bucketIndex = (int)(storageIndex / SIZE_PER_BUCKET);
        final int bucketOffset = (int)(storageIndex % SIZE_PER_BUCKET);
        try {

            // DEBUG CODE
            writer.write(bucketIndex + "," + storageIndex + "," + bucketOffset + "," + bytes.length + "\n");
            writer.flush();

            prefetchBuckets(bucketIndex);
            byte[] data = mByteCache.getIfPresent(bucketIndex);
            if (data == null) {
                mRunningReadTasks.remove(bucketIndex).get();
                data = mByteCache.getIfPresent(bucketIndex);
            }

            Blob blob = mStore.blobBuilder(Integer.toString(bucketIndex)).build();
            System.arraycopy(bytes, 0, data, bucketOffset, bytes.length + bucketOffset > SIZE_PER_BUCKET
                ? SIZE_PER_BUCKET - bucketOffset : bytes.length);
            // if (Arrays.hashCode(data) == HASH_FOR_NULL_BUCKET) {
            blob.setPayload(data);
            storeBucket(bucketIndex, blob);
            // }
            mByteCache.put(bucketIndex, data);

            if (bucketOffset + bytes.length > SIZE_PER_BUCKET) {
                data = mByteCache.getIfPresent(bucketIndex + 1);
                if (data == null) {
                    mRunningReadTasks.remove(bucketIndex + 1).get();
                    data = mByteCache.getIfPresent(bucketIndex + 1);
                }
                blob = mStore.blobBuilder(Integer.toString(bucketIndex + 1)).build();

                System.arraycopy(bytes, SIZE_PER_BUCKET - bucketOffset, data, 0, bytes.length
                    - (SIZE_PER_BUCKET - bucketOffset));
                // if (Arrays.hashCode(data) == HASH_FOR_NULL_BUCKET) {
                blob.setPayload(data);
                storeBucket(bucketIndex + 1, blob);
                // }
                mByteCache.put(bucketIndex + 1, data);
            }
        } catch (Exception exc) {
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
    class ReadTask implements Callable<Void> {

        /**
         * Bucket ID to be read.
         */
        final int mBucketId;

        ReadTask(final int pBucketId) {
            this.mBucketId = pBucketId;
        }

        @Override
        public Void call() throws Exception {
            byte[] data = mByteCache.getIfPresent(mBucketId);
            if (data == null) {
                long time = System.currentTimeMillis();
                Blob blob = mStore.getBlob(mContainerName, Integer.toString(mBucketId));
                if (blob == null) {
                    data = new byte[SIZE_PER_BUCKET];
                    // DEBUG CODE
                    download.write(Integer.toString(mBucketId) + ", empty, "
                        + (System.currentTimeMillis() - time) + "\n");
                    download.flush();
                } else {
                    data = ByteStreams.toByteArray(blob.getPayload().getInput());
                    download.write(Integer.toString(mBucketId) + "," + data.length + " , "
                        + (System.currentTimeMillis() - time) + "\n");
                    download.flush();
                    while (data.length == 0) {
                        blob = mStore.getBlob(mContainerName, Integer.toString(mBucketId));
                        data = ByteStreams.toByteArray(blob.getPayload().getInput());
                        // DEBUG CODE
                        download.write(Integer.toString(mBucketId) + "," + data.length + " , "
                            + (System.currentTimeMillis() - time) + "\n");
                        download.flush();

                    }
                }
                mByteCache.put(mBucketId, data);
            }
            return null;
        }
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
        final int mBucketIndex;

        WriteTask(Blob pBlob, int pBucketIndex) {
            this.mBlob = pBlob;
            this.mBucketIndex = pBucketIndex;
        }

        @Override
        public Void call() throws Exception {
            boolean finished = false;

            while (!finished) {
                try {
                    long time = System.currentTimeMillis();
                    mStore.putBlob(mContainerName, mBlob);
                    // DEBUG CODE
                    upload.write(Integer.toString(mBucketIndex) + ", " + (System.currentTimeMillis() - time)
                        + "\n");
                    upload.flush();
                } catch (Exception exc) {

                }
                finished = true;
            }

            return null;
        }
    }
}
