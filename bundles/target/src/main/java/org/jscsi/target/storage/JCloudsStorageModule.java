/**
 * 
 */
package org.jscsi.target.storage;


import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

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
 * JClouds-Binding to store blocks as buckets in clouds-backends. This class utilizes caching as well as multithreaded
 * writing to improve performance.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
@Beta
public class JCloudsStorageModule implements IStorageModule {

    // // START DEBUG CODE
    // private final static File writeFile = new
    // File("/Users/sebi/Desktop/writeaccess.txt");
    // private final static File readFile = new
    // File("/Users/sebi/Desktop/readaccess.txt");
    // private final static File uploadFile = new
    // File("/Users/sebi/Desktop/uploadaccess.txt");
    // private final static File downloadFile = new
    // File("/Users/sebi/Desktop/downloadaccess.txt");
    // static final FileWriter writer;
    // static final FileWriter reader;
    // static final FileWriter upload;
    // static final FileWriter download;
    // static {
    // try {
    // writer = new FileWriter(writeFile);
    // reader = new FileWriter(readFile);
    // upload = new FileWriter(uploadFile);
    // download = new FileWriter(downloadFile);
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // }

    /** Number of Blocks in one Cluster. */
    public static final int BLOCK_IN_CLUSTER = 512;

    private static final int BUCKETS_TO_PREFETCH = 3;

    private static final boolean ENCRYPT = false;
    private static final String ALGO = "AES";
    private static byte[] keyValue = new byte[] { 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k', 'k' };
    private static final Key KEY = new SecretKeySpec(keyValue, "AES");

    /** Number of Bytes in Bucket. */
    public final static int SIZE_PER_BUCKET = BLOCK_IN_CLUSTER * VIRTUAL_BLOCK_SIZE;

    public final static String CONTAINERNAME = "bench53473ResourcegraveISCSI9284";

    private final long mNumberOfCluster;

    private final String mContainerName;

    private final BlobStore mStore;

    private final BlobStoreContext mContext;

    private final Cache<Integer , byte[]> mByteCache;

    private int lastIndexWritten;
    private byte[] lastBlobWritten;

    private final CompletionService<Integer> mWriterService;
    private final CompletionService<Map.Entry<Integer , byte[]>> mReaderService;
    private final ConcurrentHashMap<Integer , Future<Integer>> mRunningWriteTasks;
    private final ConcurrentHashMap<Integer , Future<Map.Entry<Integer , byte[]>>> mRunningReadTasks;

    /**
     * Creates a new {@link JCloudsStorageModule} backed by the specified file. If no such file exists, a
     * {@link FileNotFoundException} will be thrown.
     * 
     * @param pSizeInBlocks blocksize for this module
     * @param pFile local storage, not used over here
     * 
     */
    public JCloudsStorageModule (final long pSizeInBlocks, final File pFile) {
        // number * 512 = size in bytes
        // 4gig, bench for iozone and bonnie++
        // mNumberOfCluster = 8388608 / BLOCK_IN_CLUSTER;
        // 512m, bench for fio
        mNumberOfCluster = 1048576 / BLOCK_IN_CLUSTER;
        mContainerName = CONTAINERNAME;
        String[] credentials = getCredentials();
        if (credentials.length == 0) {
            Properties properties = new Properties();
            properties.setProperty(FilesystemConstants.PROPERTY_BASEDIR, pFile.getAbsolutePath());
            mContext = ContextBuilder.newBuilder("filesystem").overrides(properties).credentials("testUser", "testPass").buildView(BlobStoreContext.class);
        } else {
            mContext = ContextBuilder.newBuilder("aws-s3").credentials(getCredentials()[0], getCredentials()[1]).buildView(BlobStoreContext.class);
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
            // System.out.println(locToSet);
            mStore.createContainerInLocation(locToSet, mContainerName);
        }

        final ExecutorService writerService = Executors.newFixedThreadPool(20);
        final ExecutorService readerService = Executors.newFixedThreadPool(20);
        mRunningWriteTasks = new ConcurrentHashMap<Integer , Future<Integer>>();
        mRunningReadTasks = new ConcurrentHashMap<Integer , Future<Map.Entry<Integer , byte[]>>>();

        mReaderService = new ExecutorCompletionService<Map.Entry<Integer , byte[]>>(readerService);
        final Thread readHashmapCleaner = new Thread(new ReadFutureCleaner());
        readHashmapCleaner.setDaemon(true);
        readHashmapCleaner.start();

        mWriterService = new ExecutorCompletionService<Integer>(writerService);
        final Thread writeHashmapCleaner = new Thread(new WriteFutureCleaner());
        writeHashmapCleaner.setDaemon(true);
        writeHashmapCleaner.start();

        mByteCache = CacheBuilder.newBuilder().maximumSize(100).build();
        lastIndexWritten = -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int checkBounds (long logicalBlockAddress, int transferLengthInBlocks) {
        if (logicalBlockAddress < 0 || logicalBlockAddress >= getSizeInBlocks()) {
            return 1;
        } else
        // if the logical block address is in bounds but the transferlength
        // either exceeds
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
    public long getSizeInBlocks () {
        return mNumberOfCluster * BLOCK_IN_CLUSTER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void read (byte[] bytes, long storageIndex) throws IOException {

        final int bucketIndex = (int) (storageIndex / SIZE_PER_BUCKET);
        final int bucketOffset = (int) (storageIndex % SIZE_PER_BUCKET);
        try {
            storeBucket(-1, null);

            // // DEBUG CODE
            // reader.write(bucketIndex + "," + storageIndex + "," +
            // bucketOffset + "," + bytes.length +
            // "\n");
            // reader.flush();

            byte[] data = mByteCache.getIfPresent(bucketIndex);
            if (data == null) {
                data = getAndprefetchBuckets(bucketIndex);
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
                    data = getAndprefetchBuckets(bucketIndex + 1);
                }

                output.write(data, 0, bytes.length - (SIZE_PER_BUCKET - bucketOffset));
            }

            System.arraycopy(output.toByteArray(), 0, bytes, 0, bytes.length);
        } catch (ExecutionException | InterruptedException exc) {
            throw new IOException(exc);
        }
    }

    private final byte[] getAndprefetchBuckets (final int pBucketStartId) throws InterruptedException , ExecutionException {
        byte[] returnval = null;
        Future<Map.Entry<Integer , byte[]>> startTask = null;
        for (int i = pBucketStartId; i < pBucketStartId + BUCKETS_TO_PREFETCH; i++) {
            Future<Map.Entry<Integer , byte[]>> currentTask = mRunningReadTasks.remove(i);
            if (currentTask == null) {
                currentTask = mReaderService.submit(new ReadTask(i));
                mRunningReadTasks.put(i, currentTask);
            }
            if (i == pBucketStartId) {
                startTask = currentTask;
            }
        }
        returnval = startTask.get().getValue();
        return returnval;

    }

    private final void storeBucket (int pBucketId, byte[] pData) throws InterruptedException , ExecutionException {
        if (lastIndexWritten != pBucketId && lastBlobWritten != null) {
            Future<Integer> writeTask = mRunningWriteTasks.remove(lastIndexWritten);
            if (writeTask != null) {
                writeTask.cancel(false);
            }
            mRunningWriteTasks.put(lastIndexWritten, mWriterService.submit(new WriteTask(lastBlobWritten, lastIndexWritten)));
        }
        lastIndexWritten = pBucketId;
        lastBlobWritten = pData;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws Exception
     */
    @Override
    public synchronized void write (byte[] bytes, long storageIndex) throws IOException {
        final int bucketIndex = (int) (storageIndex / SIZE_PER_BUCKET);
        final int bucketOffset = (int) (storageIndex % SIZE_PER_BUCKET);
        try {

            // // DEBUG CODE
            // writer.write(bucketIndex + "," + storageIndex + "," +
            // bucketOffset + "," + bytes.length +
            // "\n");
            // writer.flush();

            byte[] data = mByteCache.getIfPresent(bucketIndex);
            if (data == null) {
                data = getAndprefetchBuckets(bucketIndex);
            }

            System.arraycopy(bytes, 0, data, bucketOffset, bytes.length + bucketOffset > SIZE_PER_BUCKET ? SIZE_PER_BUCKET - bucketOffset
                    : bytes.length);
            storeBucket(bucketIndex, data);
            mByteCache.put(bucketIndex, data);

            if (bucketOffset + bytes.length > SIZE_PER_BUCKET) {
                data = mByteCache.getIfPresent(bucketIndex + 1);
                if (data == null) {
                    data = getAndprefetchBuckets(bucketIndex + 1);
                }

                System.arraycopy(bytes, SIZE_PER_BUCKET - bucketOffset, data, 0, bytes.length - (SIZE_PER_BUCKET - bucketOffset));
                storeBucket(bucketIndex + 1, data);
                mByteCache.put(bucketIndex + 1, data);
            }
        } catch (ExecutionException | InterruptedException exc) {
            throw new IOException(exc);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close () throws IOException {
        mContext.close();
    }

    /**
     * Getting credentials for aws from homedir/.credentials
     * 
     * @return a two-dimensional String[] with login and password
     */
    private static String[] getCredentials () {
        return new String[0];
        // File userStore = new File(System.getProperty("user.home"),
        // new StringBuilder(".credentials").append(File.separator)
        // .append("aws.properties").toString());
        // if (!userStore.exists()) {
        // return new String[0];
        // } else {
        // Properties props = new Properties();
        // try {
        // props.load(new FileReader(userStore));
        // return new String[] { props.getProperty("access"),
        // props.getProperty("secret") };
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
    class ReadTask implements Callable<Map.Entry<Integer , byte[]>> {

        final Cipher mCipher;

        /**
         * Bucket ID to be read.
         */
        final int mBucketId;

        ReadTask (final int pBucketId) {
            if (ENCRYPT) {
                try {
                    mCipher = Cipher.getInstance(ALGO);
                    mCipher.init(Cipher.DECRYPT_MODE, KEY);
                } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            } else {
                mCipher = null;
            }
            this.mBucketId = pBucketId;
        }

        @Override
        public Map.Entry<Integer , byte[]> call () throws Exception {
            byte[] data = mByteCache.getIfPresent(mBucketId);
            if (data == null) {
                // long time = System.currentTimeMillis();
                Blob blob = mStore.getBlob(mContainerName, Integer.toString(mBucketId));
                if (blob == null) {
                    data = new byte[SIZE_PER_BUCKET];
                    // // DEBUG CODE
                    // download.write(Integer.toString(mBucketId) + ", empty, "
                    // + (System.currentTimeMillis() - time) + "\n");
                    // download.flush();
                } else {
                    try (InputStream is = blob.getPayload().getInput()) {
                       data = ByteStreams.toByteArray(is);
                    }
                    // download.write(Integer.toString(mBucketId) + "," +
                    // data.length + " , "
                    // + (System.currentTimeMillis() - time) + "\n");
                    // download.flush();
                    while (data.length < SIZE_PER_BUCKET) {
                        blob = mStore.getBlob(mContainerName, Integer.toString(mBucketId));
                        try (InputStream is = blob.getPayload().getInput()) {
                            data = ByteStreams.toByteArray(is);
                        }
                        // // DEBUG CODE
                        // download.write(Integer.toString(mBucketId) + "," +
                        // data.length + " , "
                        // + (System.currentTimeMillis() - time) + "\n");
                        // download.flush();
                    }
                    if (ENCRYPT) {
                        data = mCipher.doFinal(data);
                    }
                }
            }
            if (data.length < SIZE_PER_BUCKET) {
                System.out.println(data.length);
                // throw new IllegalStateEception("Bucket " + mBucketId
                // +" invalid");
            }
            mByteCache.put(mBucketId, data);
            final byte[] finalizedData = data;
            return new Map.Entry<Integer , byte[]>() {
                @Override
                public byte[] setValue (byte[] value) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public byte[] getValue () {
                    return finalizedData;
                }

                @Override
                public Integer getKey () {
                    return mBucketId;
                }
            };
        }
    }

    /**
     * Single task to write data to the cloud.
     * 
     * @author Sebastian Graf, University of Konstanz
     * 
     */
    class WriteTask implements Callable<Integer> {
        /**
         * The bytes to buffer.
         */
        final byte[] mData;
        final int mBucketIndex;
        final Cipher mCipher;

        WriteTask (byte[] pData, int pBucketIndex) {
            checkState(pData.length == SIZE_PER_BUCKET);
            if (ENCRYPT) {
                try {
                    mCipher = Cipher.getInstance(ALGO);
                    mCipher.init(Cipher.ENCRYPT_MODE, KEY);
                } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            } else {
                mCipher = null;
            }
            this.mData = pData;
            this.mBucketIndex = pBucketIndex;
        }

        @Override
        public Integer call () throws Exception {
            boolean finished = false;

            while (!finished) {
                try {
                    // long time = System.currentTimeMillis();
                    byte[] data = mData;
                    if (ENCRYPT) {
                        data = mCipher.doFinal(mData);
                    }
                    Blob blob = mStore.blobBuilder(Integer.toString(mBucketIndex)).build();
                    blob.setPayload(data);
                    mStore.putBlob(mContainerName, blob);
                    // // DEBUG CODE
                    // upload.write(Integer.toString(mBucketIndex) + ", " +
                    // (System.currentTimeMillis() -
                    // time)
                    // + "\n");
                    // upload.flush();
                } catch (Exception exc) {

                }
                finished = true;
            }

            return mBucketIndex;
        }
    }

    class ReadFutureCleaner extends Thread {

        public void run () {
            while (true) {
                try {
                    Future<Map.Entry<Integer , byte[]>> element = mReaderService.take();
                    if (!element.isCancelled()) {
                        mRunningReadTasks.remove(element.get().getKey());
                    }
                } catch (Exception exc) {
                    throw new RuntimeException(exc);
                }
            }
        }
    }

    class WriteFutureCleaner extends Thread {

        public void run () {
            while (true) {
                try {
                    Future<Integer> element = mWriterService.take();
                    if (!element.isCancelled()) {
                        mRunningWriteTasks.remove(element.get());
                    }
                } catch (Exception exc) {
                    throw new RuntimeException(exc);
                }
            }

        }
    }

}
