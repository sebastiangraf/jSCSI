package org.jscsi.target.storage.buffering;

import static org.jscsi.target.storage.JCloudsStorageModule.SIZE_PER_BUCKET;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;

import com.google.common.io.ByteStreams;

/**
 * This worker periodically writes into treetank
 * using the BufferedWriteTasks first-in-first-out.
 * 
 * @author Andreas Rain
 * 
 */
public class BufferedTaskWorker implements Callable<Void> {

    // private static final Logger LOGGER = LoggerFactory.getLogger(BufferedTaskWorker.class);

    /**
     * The tasks that have to be performed.
     */
    private LinkedBlockingQueue<BufferedWriteTask> mTasks;

    /** Reference to blobstore. */
    private final BlobStore mBlobStore;

    /** Container name. */
    private final String mContainerName;

    /** Map for storing tasks again. */
    private final Map<Long, BufferedWriteTask> mElements;

    /**
     * Create a new worker.
     * 
     * @param pRtx
     * @param pBytesInCluster
     */
    public BufferedTaskWorker(BlobStore pBlobStore, String pContainerName) {
        mTasks = new LinkedBlockingQueue<BufferedWriteTask>();
        mBlobStore = pBlobStore;
        mContainerName = pContainerName;
        mElements = new ConcurrentHashMap<Long, BufferedWriteTask>();

    }

    /**
     * Add a task to the worker consisting of all the information
     * a BufferedWriteTask needs.
     * 
     * @param pBytes
     * @param pOffset
     * @param pLength
     * @param pStorageIndex
     * @throws InterruptedException
     */
    public synchronized void newTask(BufferedWriteTask task) throws InterruptedException {
        mElements.put(new Long(task.getStorageIndex()), task);
        mTasks.put(task);
    }

    @Override
    public Void call() throws Exception {

        while (true) {
            BufferedWriteTask task = mTasks.take();
            if (task.getBytes().length == 0 && task.getStorageIndex() == -1) {
                break;
            } else {
                performTask(task);
            }
        }
        return null;
    }

    /**
     * This method gets called periodically, as long
     * as there are tasks left in the queue.
     * 
     * @throws IOException
     */
    public void performTask(BufferedWriteTask currentTask) throws IOException {
        byte[] bytes = currentTask.getBytes();
        long storageIndex = currentTask.getStorageIndex();

        // LOGGER.info("Starting to write with param: \nbytes = " + Arrays.toString(bytes).substring(0, 100)
        // + "\nbytesOffset = " + bytesOffset + "\nlength = " + length + "\nstorageIndex = " + storageIndex);
        int startIndex = (int)(storageIndex / SIZE_PER_BUCKET);
        int startIndexOffset = (int)(storageIndex % SIZE_PER_BUCKET);

        int endIndex = (int)((storageIndex + bytes.length) / SIZE_PER_BUCKET);
        int endIndexMax = (int)((storageIndex + bytes.length) % SIZE_PER_BUCKET);

        // caring about first bucket
        Blob firstBucket = getBlob(Long.toString(startIndex));
        byte[] firstBytes = ByteStreams.toByteArray(firstBucket.getPayload().getInput());
        int bytesWritten =
            SIZE_PER_BUCKET - startIndexOffset < bytes.length ? SIZE_PER_BUCKET - startIndexOffset : bytes.length;
        System.arraycopy(bytes, 0, firstBytes, startIndexOffset, bytesWritten);
        firstBucket.setPayload(firstBytes);
        mBlobStore.putBlob(mContainerName, firstBucket);

        // getting all clusters in between
        for (int i = startIndex + 1; i < endIndex; i++) {
            Blob nextBucket = getBlob(Long.toString(i));
            byte[] nextBytes = ByteStreams.toByteArray(nextBucket.getPayload().getInput());
            System.arraycopy(bytes, bytesWritten, nextBytes, 0, SIZE_PER_BUCKET);
            nextBucket.setPayload(nextBytes);
            mBlobStore.putBlob(mContainerName, nextBucket);
            bytesWritten = bytesWritten + SIZE_PER_BUCKET;
        }

        // checking if there is a last cluster
        if (startIndex != endIndex) {
            Blob lastBucket = getBlob(Long.toString(endIndex));
            byte[] lastBytes = ByteStreams.toByteArray(lastBucket.getPayload().getInput());
            System.arraycopy(bytes, bytesWritten, lastBytes, 0, endIndexMax < SIZE_PER_BUCKET ? endIndexMax
                : SIZE_PER_BUCKET);
            lastBucket.setPayload(lastBytes);
            mBlobStore.putBlob(mContainerName, lastBucket);
            bytesWritten = bytesWritten + SIZE_PER_BUCKET;
        }

        mElements.remove(new Long(currentTask.getStorageIndex()));
    }

    /**
     * The returned collisions are ordered chronologically.
     * 
     * @param pLength
     * @param pStorageIndex
     * @return List<Collision> - returns a list of collisions
     */

    public List<Collision> checkForCollisions(int pLength, long pStorageIndex) {
        List<Collision> collisions = new ArrayList<Collision>();

        for (BufferedWriteTask task : mElements.values()) {
            if (overlappingIndizes(pLength, pStorageIndex, task.getBytes().length, task.getStorageIndex())) {
                // Determining where the two tasks collide
                int start = 0;
                int end = 0;
                byte[] bytes = null;

                // Determining the start point
                if (task.getStorageIndex() < pStorageIndex) {
                    start = (int)pStorageIndex;
                } else {
                    start = (int)task.getStorageIndex();
                }

                // Determining the end point
                if (task.getStorageIndex() + task.getBytes().length > pStorageIndex + pLength) {
                    end = (int)(pStorageIndex + pLength);
                } else {
                    end = (int)(task.getStorageIndex() + task.getBytes().length);
                }

                bytes = new byte[end - start];

                if (start == pStorageIndex) {
                    System.arraycopy(task.getBytes(), (int)(pStorageIndex - task.getStorageIndex()), bytes,
                        0, end - start);
                } else {
                    System.arraycopy(task.getBytes(), 0, bytes, 0, end - start);
                }

                collisions.add(new Collision(start, end, bytes));

                // LOGGER.info("Found collision from " + start + " to " + end);
            }
        }

        return collisions;
    }

    private Blob getBlob(String pKey) throws IOException {

        Blob blob;
        blob = mBlobStore.getBlob(mContainerName, pKey);
        if (blob == null) {
            blob = mBlobStore.blobBuilder(pKey).build();
            byte[] val = new byte[SIZE_PER_BUCKET];
            blob.setPayload(val);
        }
        return blob;
    }

    /**
     * Determine if indizes overlap.
     * 
     * @param srcLength
     * @param srcStorageIndex
     * @param destLength
     * @param destStorageIndex
     * @return true if indizes overlap, false otherwise
     */
    private boolean overlappingIndizes(int srcLength, long srcStorageIndex, int destLength,
        long destStorageIndex) {
        if (destLength + destStorageIndex < srcStorageIndex || destStorageIndex > srcStorageIndex + srcLength) {
            return false;
        }

        return true;
    }

}
