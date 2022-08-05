/**
 *
 */
package org.jscsi.target.storage;


import java.io.Closeable;
import java.io.IOException;

import org.jscsi.target.scsi.cdb.CommandDescriptorBlock;


/**
 * This is an abstract super class offering methods for storage and retrieval of data, as well as emulating some
 * properties of block storage devices.
 * <p>
 * All index and length parameters used by the read and write methods are referring to bytes, unlike the values sent in
 * {@link CommandDescriptorBlock} s.
 *
 * @author Andreas Ergenzinger
 * @author CHEN Qingcan
 */
public interface IStorageModule extends Closeable {
    /**
     * This method can be used for checking if a (series of) I/O operations will result in an {@link IOException} due to
     * trying to access blocks outside the medium's boundaries.
     * <p>
     * The SCSI standard requires checking for these boundary violations right after receiving a read or write command,
     * so that an appropriate error message can be returned to the initiator. Therefore this method must be called prior
     * to each read or write sequence.
     * <p>
     * The values returned by this method and their meaning with regard to the interval [0, {@link #getSizeInBlocks()} -
     * 1] are shown in the following table:
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
     * <td>the interval [<i>logicalBlockAddress</i>, <i>logicalBlockAddress</i> + <i>transferLengthInBlocks</i>]<br/>
     * lies outside of the interval, or <i>transferLengthInBlocks</i> is negative</td>
     * </tr>
     * </table>
     * <p>
     * Note that the parameters of this method are referring to blocks, not to byte indices.
     *
     * @param logicalBlockAddress the index of the first block of data to be read or written
     * @param transferLengthInBlocks the total number of consecutive blocks about to be read or written
     * @return see table in description
     */
    int checkBounds (final long logicalBlockAddress, final int transferLengthInBlocks);

    /**
     * Returns the storage space size in bytes divided by the block size in bytes (rounded down).
     *
     * @return the virtual amount of storage blocks available
     */
    long getSizeInBlocks ();

    /**
     * Copies bytes from storage to the passed byte array.
     *
     * @param bytes the array into which the data will be copied will be filled with data from storage
     * @param storageIndex the position of the first byte to be copied
     * @throws IOException
     */
    void read (byte[] bytes, long storageIndex) throws IOException;

    /**
     * Saves part of the passed byte array's content.
     *
     * @param bytes the source of the data to be stored
     * @param storageIndex byte offset in the storage area
     * @throws IOException
     */
    void write (byte[] bytes, long storageIndex) throws IOException;

    /**
     * Closing the storage.
     *
     * @throws IOException to be closed
     */
    @Override
    void close () throws IOException;

    /**
     * Get block size of underlying storage medium.
     *
     * @return block size.
     */
    int getBlockSize();

    /**
     * Requests that the implements ensure that the specified logical blocks
     * have their most recent data values recorded in non-volatile cache and/or on the medium.
     * <p>
     * Implements may keep the default empty method if no synchronization is needed.
     *
     * @param syncLogicalBlockAddress   LOGICAL BLOCK ADDRESS field
     * @param syncNumberOfBlocks        NUMBER OF BLOCKS field
     * @throws IOException
     */
    default void syncCahe (long syncLogicalBlockAddress, int syncNumberOfBlocks)
    throws IOException {}

}
