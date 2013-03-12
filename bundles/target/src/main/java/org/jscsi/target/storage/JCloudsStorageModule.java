/**
 * 
 */
package org.jscsi.target.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
import org.jclouds.blobstore.domain.MutableBlobMetadata;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.jclouds.io.Payload;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
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
        mCache = CacheBuilder.newBuilder().maximumSize(200).build();
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

            // DEBUG CODE
            reader.write(bucketIndex + "," + storageIndex + "," + bucketOffset + "," + bytes.length + "\n");
            reader.flush();

            byte[] data = mCache.getIfPresent(bucketIndex);
            if (data == null) {
                Blob blob = getData(bucketIndex);
                if (blob instanceof NullBlob) {
                    data = new byte[SIZE_PER_BUCKET];
                } else {
                    data = ByteStreams.toByteArray(blob.getPayload().getInput());
                    while (data.length == 0) {
                        blob = mStore.getBlob(mContainerName, Integer.toString(bucketIndex));
                        data = ByteStreams.toByteArray(blob.getPayload().getInput());
                        System.out.println("StorageIndex: " + storageIndex);
                        System.out.println("BucketIndex: " + bucketIndex);
                        System.out.println("BucketOffset: " + bucketOffset);
                    }
                }
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
                Blob blob = getData(bucketIndex + 1);
                if (blob == null) {
                    data = new byte[SIZE_PER_BUCKET];
                } else {
                    data = ByteStreams.toByteArray(blob.getPayload().getInput());
                    while (data.length == 0) {
                        blob = mStore.getBlob(mContainerName, Integer.toString(bucketIndex));
                        data = ByteStreams.toByteArray(blob.getPayload().getInput());
                        System.out.println("StorageIndex: " + storageIndex);
                        System.out.println("BucketIndex: " + bucketIndex);
                        System.out.println("BucketOffset: " + bucketOffset);
                    }
                }
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
                mContainerName, lastIndexWritten)));
        }
        lastIndexWritten = pBucketId;
        lastBlobWritten = pBlob;
    }

    private final Blob getData(int pBucketId) throws IOException, InterruptedException, ExecutionException {
        Blob blob = null;
        if (mRunningTasks.containsKey(pBucketId)) {
            mRunningTasks.remove(pBucketId).get();
        }
        blob = mStore.getBlob(mContainerName, Integer.toString(pBucketId));
        // DEBUG CODE
        download.write(Integer.toString(pBucketId) + "\n");
        download.flush();
        if (blob == null) {
            blob = new NullBlob();
        }
        return blob;
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
            // DEBUG CODE
            writer.write(bucketIndex + "," + storageIndex + "," + bucketOffset + "," + bytes.length + "\n");
            writer.flush();

            byte[] data;
            Blob blob;
            if (bucketIndex != lastIndexWritten) {
                blob = getData(bucketIndex);
            } else {
                blob = lastBlobWritten;
            }

            if (blob instanceof NullBlob) {
                data = new byte[SIZE_PER_BUCKET];
                blob = mStore.blobBuilder(Integer.toString(bucketIndex)).build();
            } else {
                data = ByteStreams.toByteArray(blob.getPayload().getInput());
            }

            System.arraycopy(bytes, 0, data, bucketOffset, bytes.length + bucketOffset > SIZE_PER_BUCKET
                ? SIZE_PER_BUCKET - bucketOffset : bytes.length);
            blob.setPayload(data);
            storeBucket(bucketIndex, blob);
            mCache.invalidate(bucketIndex);

            if (bucketOffset + bytes.length > SIZE_PER_BUCKET) {
                if (bucketIndex + 1 != lastIndexWritten) {
                    blob = getData(bucketIndex + 1);
                } else {
                    blob = lastBlobWritten;
                }
                blob = getData(bucketIndex + 1);
                if (blob instanceof NullBlob) {
                    data = new byte[SIZE_PER_BUCKET];
                    blob = mStore.blobBuilder(Integer.toString(bucketIndex + 1)).build();
                } else {
                    data = ByteStreams.toByteArray(blob.getPayload().getInput());
                    while (data.length == 0) {
                        blob = mStore.getBlob(mContainerName, Integer.toString(bucketIndex));
                        data = ByteStreams.toByteArray(blob.getPayload().getInput());
                        System.out.println("StorageIndex: " + storageIndex);
                        System.out.println("BucketIndex: " + bucketIndex);
                        System.out.println("BucketOffset: " + bucketOffset);
                    }
                }

                System.arraycopy(bytes, SIZE_PER_BUCKET - bucketOffset, data, 0, bytes.length
                    - (SIZE_PER_BUCKET - bucketOffset));
                blob.setPayload(data);
                storeBucket(bucketIndex + 1, blob);
                mCache.invalidate(bucketIndex + 1);
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

    private static String[] getCredentials() {
        // return new String[0];
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

        final int mIndex;

        WriteTask(Blob pBlob, final BlobStore pBlobStore, final String pContainer, final int index) {
            this.mBlob = pBlob;
            this.mBlobStore = pBlobStore;
            this.mContainer = pContainer;
            this.mIndex = index;
        }

        @Override
        public Void call() throws Exception {
            boolean finished = false;

            while (!finished) {
                try { // DEBUG CODE
                    upload.write(Integer.toString(mIndex) + "\n");
                    upload.flush();
                    mStore.putBlob(mContainer, mBlob);
                } catch (IOException exc) {

                }
                finished = true;
            }

            return null;
        }
    }

    class NullBlob implements Blob {

        /**
         * {@inheritDoc}
         */
        @Override
        public void setPayload(Payload data) {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setPayload(File data) {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setPayload(byte[] data) {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setPayload(InputStream data) {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setPayload(String data) {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Payload getPayload() {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(Blob o) {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MutableBlobMetadata getMetadata() {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Multimap<String, String> getAllHeaders() {
            throw new UnsupportedOperationException();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setAllHeaders(Multimap<String, String> allHeaders) {
            throw new UnsupportedOperationException();
        }

    }
}
