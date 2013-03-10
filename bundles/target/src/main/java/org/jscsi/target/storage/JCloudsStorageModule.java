/**
 * 
 */
package org.jscsi.target.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.filesystem.reference.FilesystemConstants;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class JCloudsStorageModule implements IStorageModule {

    // /START DEBUG CODE
    // private final static File writeFile = new File("/Users/sebi/Desktop/writeaccess.txt");
    // private final static File readFile = new File("/Users/sebi/Desktop/readaccess.txt");
    // static final FileWriter writer;
    // static final FileWriter reader;
    // static {
    // try {
    // writeFile.delete();
    // readFile.delete();
    // writer = new FileWriter(writeFile);
    // reader = new FileWriter(readFile);
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // }
    // }

    /** Number of Blocks in one Cluster. */
    public static final int BLOCK_IN_CLUSTER = 512;

    /** Number of Bytes in Bucket. */
    public final static int SIZE_PER_BUCKET = BLOCK_IN_CLUSTER * VIRTUAL_BLOCK_SIZE;

    private final long mNumberOfCluster;

    private final String mContainerName;

    private final BlobStore mStore;

    private final BlobStoreContext mContext;

    private int lastBucketID = -1;

    private Blob lastBlobAccess;

    // private final Cache<Integer, Blob> mCache;

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
        final int bucketIndex = (int)(storageIndex / SIZE_PER_BUCKET);
        final int bucketOffset = (int)(storageIndex % SIZE_PER_BUCKET);
        //
        // // DEBUG CODE
        // reader.write(bucketIndex + "," + storageIndex + "," + bucketOffset + "," + bytes.length + "\n");
        // reader.flush();

        Blob blob = getData(bucketIndex);
        final ByteArrayDataOutput output = ByteStreams.newDataOutput(bytes.length);
        byte[] data;
        if (blob == null) {
            data = new byte[SIZE_PER_BUCKET];
        } else {
            data = ByteStreams.toByteArray(blob.getPayload().getInput());
        }
        output.write(data, bucketOffset, bucketOffset + bytes.length > SIZE_PER_BUCKET ? SIZE_PER_BUCKET
            - bucketOffset : bytes.length);

        if (bucketOffset + bytes.length > SIZE_PER_BUCKET) {
            blob = getData(bucketIndex + 1);
            if (blob == null) {
                data = new byte[SIZE_PER_BUCKET];
            } else {
                data = ByteStreams.toByteArray(blob.getPayload().getInput());
            }
            output.write(data, 0, bytes.length - (SIZE_PER_BUCKET - bucketOffset));
        }

        System.arraycopy(output.toByteArray(), 0, bytes, 0, bytes.length);

    }

    private final Blob getData(int startIndex) throws IOException {
        // Blob blob = mCache.getIfPresent(startIndex);
        // if (blob == null) {
//        if (lastBucketID != -1) {
//            mStore.putBlob(Integer.toString(lastBucketID), lastBlobAccess);
//            lastBucketID = -1;
//        }
        Blob blob = mStore.getBlob(mContainerName, Integer.toString(startIndex));
        if (blob == null) {
            return null;
        }
        // }
        return blob;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] bytes, long storageIndex) throws IOException {
        final int bucketIndex = (int)(storageIndex / SIZE_PER_BUCKET);
        final int bucketOffset = (int)(storageIndex % SIZE_PER_BUCKET);
        //
        // // DEBUG CODE
        // writer.write(bucketIndex + "," + storageIndex + "," + bucketOffset + "," + bytes.length + "\n");
        // writer.flush();

        Blob blob = getData(bucketIndex);
        byte[] bucketData;
        if (blob == null) {
            bucketData = new byte[SIZE_PER_BUCKET];
            blob = mStore.blobBuilder(Integer.toString(bucketIndex)).build();
        } else {
            bucketData = ByteStreams.toByteArray(blob.getPayload().getInput());
        }
        System.arraycopy(bytes, 0, bucketData, bucketOffset, bytes.length + bucketOffset > SIZE_PER_BUCKET
            ? SIZE_PER_BUCKET - bucketOffset : bytes.length);
        blob.setPayload(bucketData);
        // mCache.put(startIndex, blob);
         mStore.putBlob(mContainerName, blob);
//        lastBucketID = bucketIndex;
//        lastBlobAccess = blob;

        if (bucketOffset + bytes.length > SIZE_PER_BUCKET) {
            blob = getData(bucketIndex);
            if (blob == null) {
                bucketData = new byte[SIZE_PER_BUCKET];
                blob = mStore.blobBuilder(Integer.toString(bucketIndex + 1)).build();
            } else {
                bucketData = ByteStreams.toByteArray(blob.getPayload().getInput());
            }

            System.arraycopy(bytes, SIZE_PER_BUCKET - bucketOffset, bucketData, 0, bytes.length
                - (SIZE_PER_BUCKET - bucketOffset));
            blob.setPayload(bucketData);
             mStore.putBlob(mContainerName, blob);
//            lastBucketID = bucketIndex + 1;
//            lastBlobAccess = blob;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        mContext.close();
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
