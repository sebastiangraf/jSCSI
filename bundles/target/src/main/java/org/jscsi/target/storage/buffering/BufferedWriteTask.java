package org.jscsi.target.storage.buffering;


/**
 * BufferedWriteTasks are used to
 * keep the information of a write request
 * for later storaging in TreeTank.
 * 
 * @author Andreas Rain
 * 
 */
public class BufferedWriteTask {

    /**
     * The bytes to buffer.
     */
    private final byte[] mBytes;

    /**
     * The offset of the bytes.
     */
    private final int mOffset;

    /**
     * How many bytes to write.
     */
    private final int mLength;

    /**
     * Where to start writing in the storage.
     */
    private final long mStorageIndex;

    /**
     * All these parameters are passed to the {@link TreetankStorageModule} write method
     * and are being buffered using this class and its constructor.
     * 
     * @param pBytes
     * @param pOffset
     * @param pLength
     * @param pStorageIndex
     */
    public BufferedWriteTask(byte[] pBytes, int pOffset, int pLength, long pStorageIndex) {
        super();
        this.mBytes = pBytes;
        this.mOffset = pOffset;
        this.mLength = pLength;
        this.mStorageIndex = pStorageIndex;
    }

    /**
     * 
     * @return byte[] - the bytes buffered in this task.
     */
    public byte[] getBytes() {
        return mBytes;
    }

    /**
     * 
     * @return int - the offset
     */
    public int getOffset() {
        return mOffset;
    }

    /**
     * 
     * @return int - the length
     */
    public int getLength() {
        return mLength;
    }

    /**
     * 
     * @return long - the storageindex
     */
    public long getStorageIndex() {
        return mStorageIndex;
    }

}
