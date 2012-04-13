package org.jscsi.target.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.jscsi.target.scsi.cdb.CommandDescriptorBlock;

/**
 * This is an abstract super class offering methods for storage and retrieval of
 * data, as well as emulating some properties of block storage devices.
 * <p>
 * All index and length parameters used by the read and write methods are referring to bytes, unlike the
 * values sent in {@link CommandDescriptorBlock} s, which are based on the value reported by
 * {@link #getBlockSizeInBytes()}.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class AbstractStorageModule {

    /**
     * A fictitious block size.
     */
    public static final int VIRTUAL_BLOCK_SIZE = 512;

    /**
     * The size of the medium in blocks.
     * 
     * @see #VIRTUAL_BLOCK_SIZE
     */
    protected long sizeInBlocks;

    /**
     * The abstract constructor that makes sure that the {@link #sizeInBlocks} variable is initialized.
     * 
     * @param sizeInBlocks
     *            the size of the medium in blocks
     */
    protected AbstractStorageModule(final long sizeInBlocks) {
        this.sizeInBlocks = sizeInBlocks;
    }

    /**
     * Returns the storage space size in bytes divided by {@link #getBlockSizeInBytes()} (rounded down).
     * 
     * @return the virtual amount of storage blocks available
     */
    public final long getSizeInBlocks() {
        return sizeInBlocks;
    }

    /**
     * This method can be used for checking if a (series of) I/O operations will
     * result in an {@link IOException} due to trying to access blocks outside
     * the medium's boundaries.
     * <p>
     * The SCSI standard requires checking for these boundary violations right after receiving a read or write
     * command, so that an appropriate error message can be returned to the initiator. Therefore this method
     * must be called prior to each read or write sequence.
     * <p>
     * The values returned by this method and their meaning with regard to the interval [0,
     * {@link #sizeInBlocks} - 1] are shown in the following table:
     * <p>
     * <table border="1">
     * <tr>
     * <th>Return Value</th>
     * <th>Meaning</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>no boundaries are violated</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>the <i>logicalBlockAddress</i> parameter lies outside of the interval</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>the interval [<i>logicalBlockAddress</i>, <i>logicalBlockAddress</i> +
     * <i>transferLengthInBlocks</i>]<br/>
     * lies outside of the interval, or <i>transferLengthInBlocks</i> is negative</td>
     * </tr>
     * </table>
     * <p>
     * Note that the parameters of this method are referring to blocks, not to byte indices.
     * 
     * @param logicalBlockAddress
     *            the index of the first block of data to be read or written
     * @param transferLengthInBlocks
     *            the total number of consecutive blocks about to be read or
     *            written
     * @return see table in description
     */
    public final int checkBounds(final long logicalBlockAddress, final int transferLengthInBlocks) {
        if (logicalBlockAddress < 0 || logicalBlockAddress >= sizeInBlocks)
            return 1;
        if (transferLengthInBlocks < 0 || logicalBlockAddress + transferLengthInBlocks > sizeInBlocks)
            return 2;
        return 0;
    }

    /**
     * Copies bytes from storage to the passed byte array.
     * 
     * @param bytes
     *            the array into which the data will be copied
     * @param bytesOffset
     *            the position of the first byte in <code>bytes</code>, which
     *            will be filled with data from storage
     * @param length
     *            the number of bytes to copy
     * @param storageIndex
     *            the position of the first byte to be copied
     * @throws IOException
     */
    public abstract void read(byte[] bytes, int bytesOffset, int length, long storageIndex)
        throws IOException;

    // /**
    // * Copies bytes from storage to the passed byte array. <code>bytes.length</code> bytes will be copied.
    // *
    // * @param bytes
    // * the array into which the data will be copied
    // * @param storageIndex
    // * he position of the first byte to be copied
    // */
    // public final void read(byte[] bytes, long storageIndex) throws IOException {
    // read(bytes, 0, bytes.length, storageIndex);
    // }

    /**
     * Saves part of the passed byte array's content.
     * 
     * @param bytes
     *            the source of the data to be stored
     * @param bytesOffset
     *            offset of the first byte to be stored
     * @param length
     *            the number of bytes to be copied
     * @param storageIndex
     *            byte offset in the storage area
     * @throws IOException
     */
    public abstract void write(byte[] bytes, int bytesOffset, int length, long storageIndex)
        throws IOException;

    public abstract void close() throws IOException;

    /**
     * The mode {@link String} parameter used during the instantiation of {@link #randomAccessFile}.
     * <p>
     * This will create a {@link RandomAccessFile} with both read and write privileges that will immediately
     * save all written data in the file.
     */
    private static final String MODE = "rwd";

    /**
     * This is the build method for creating instances of {@link RandomAccessStorageModule}. If there is no
     * file to be found at the
     * specified <code>filePath</code>, then a {@link FileNotFoundException} will be thrown.
     * 
     * @param file
     *            a path leading to the file serving as storage medium
     * @return a new instance of {@link RandomAccessStorageModule}
     * @throws FileNotFoundException
     *             if the specified file does not exist
     */
    public static synchronized final AbstractStorageModule open(final File file) throws FileNotFoundException {
        final long sizeInBlocks = file.length() / VIRTUAL_BLOCK_SIZE;
        final RandomAccessFile randomAccessFile = new RandomAccessFile(file, MODE);// throws exc. if
                                                                                   // !file.exists()

        return new SynchronizedRandomAccessStorageModule(sizeInBlocks, randomAccessFile);
    }

}
