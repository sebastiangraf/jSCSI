package org.jscsi.target.storage.buffering;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jscsi.target.storage.IStorageModule;
import org.jscsi.target.storage.JCloudsStorageModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

/**
 * This worker periodically writes into treetank
 * using the BufferedWriteTasks first-in-first-out.
 * 
 * @author Andreas Rain
 * 
 */
public class BufferedTaskWorker implements Callable<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BufferedTaskWorker.class);

    /**
     * The tasks that have to be performed.
     */
    private ConcurrentLinkedQueue<BufferedWriteTask> mTasks;

    /**
     * Whether or not this worker has been disposed.
     */
    private boolean mDisposed;

    /** Reference to blobstore. */
    private final BlobStore mBlobStore;

    /** Container name. */
    private final String mContainerName;

    /**
     * Create a new worker.
     * 
     * @param pRtx
     * @param pBytesInCluster
     */
    public BufferedTaskWorker(BlobStore pBlobStore, String pContainerName) {
        mTasks = new ConcurrentLinkedQueue<>();
        mDisposed = false;
        mBlobStore = pBlobStore;
        mContainerName = pContainerName;

    }

    /**
     * Add a task to the worker consisting of all the information
     * a BufferedWriteTask needs.
     * 
     * @param pBytes
     * @param pOffset
     * @param pLength
     * @param pStorageIndex
     */
    public synchronized void newTask(byte[] pBytes, int pOffset, int pLength, long pStorageIndex) {
        mTasks.add(new BufferedWriteTask(pBytes, pOffset, pLength, pStorageIndex));
        this.notify();
    }

    @Override
    public Void call() throws Exception {

        while (!mDisposed) {
            if (mTasks.isEmpty()) {
                this.wait();
            }

            performTask();
        }

        return null;
    }

    /**
     * This method gets called periodically, as long
     * as there are tasks left in the queue.
     * 
     * @throws IOException
     */
    private void performTask() throws IOException {

        BufferedWriteTask currentTask = mTasks.poll();
        byte[] bytes = currentTask.getBytes();
        int bytesOffset = currentTask.getOffset();
        int length = currentTask.getLength();
        long storageIndex = currentTask.getStorageIndex();

        LOGGER.info("Starting to write with param: \nbytes = " + Arrays.toString(bytes).substring(0, 100)
            + "\nbytesOffset = " + bytesOffset + "\nlength = " + length + "\nstorageIndex = " + storageIndex);
        // Using the most recent revision
        if (bytesOffset + length > bytes.length) {
            throw new IOException();
        }
        int startIndex =
            (int)(storageIndex / (JCloudsStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));
        int startIndexOffset =
            (int)(storageIndex % (JCloudsStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));

        int endIndex =
            (int)((storageIndex + length) / (JCloudsStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));
        int endIndexMax =
            (int)((storageIndex + length) % (JCloudsStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));

        for (int i = startIndex; i <= endIndex; i++) {
            Blob blob;
            byte[] val;
            if (mBlobStore.blobExists(mContainerName, Long.toString(i))) {
                blob = mBlobStore.getBlob(mContainerName, Long.toString(i));
                val = ByteStreams.toByteArray(blob.getPayload().getInput());
            } else {
                blob = mBlobStore.blobBuilder(Long.toString(i)).build();
                val = new byte[IStorageModule.VIRTUAL_BLOCK_SIZE * JCloudsStorageModule.BLOCK_IN_CLUSTER];
            }

            if (i == startIndex && i == endIndex) {
                System.arraycopy(bytes, bytesOffset, val, startIndexOffset, endIndexMax);
            } else if (i == startIndex) {
                System.arraycopy(bytes, bytesOffset, val, startIndexOffset,
                    (JCloudsStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE)
                        - startIndexOffset);
            } else if (i == endIndex) {
                System
                    .arraycopy(
                        bytes,
                        bytesOffset
                            + ((JCloudsStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE) * (i - startIndex)),
                        val, 0, endIndexMax);
            } else {
                System
                    .arraycopy(
                        bytes,
                        bytesOffset
                            + ((JCloudsStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE) * (i - startIndex)),
                        val, 0, (JCloudsStorageModule.BLOCK_IN_CLUSTER * IStorageModule.VIRTUAL_BLOCK_SIZE));
            }

            mBlobStore.putBlob(mContainerName, blob);
        }

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

        for (BufferedWriteTask task : mTasks) {
            if (overlappingIndizes(pLength, pStorageIndex, task.getLength(), task.getStorageIndex())) {
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
                if (task.getStorageIndex() + task.getLength() > pStorageIndex + pLength) {
                    end = (int)(pStorageIndex + pLength);
                } else {
                    end = (int)(task.getStorageIndex() + task.getLength());
                }

                bytes = new byte[end - start];

                if (start == pStorageIndex) {
                    System.arraycopy(task.getBytes(), (int)(task.getOffset() + (pStorageIndex - task
                        .getStorageIndex())), bytes, 0, end - start);
                } else {
                    System.arraycopy(task.getBytes(), task.getOffset(), bytes, 0, end - start);
                }

                collisions.add(new Collision(start, end, bytes));

                LOGGER.info("Found collision from " + start + " to " + end);
            }
        }

        return collisions;
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

    /**
     * Dispose this worker so it stops working.
     */
    public void dispose() {
        mDisposed = true;
    }

}
