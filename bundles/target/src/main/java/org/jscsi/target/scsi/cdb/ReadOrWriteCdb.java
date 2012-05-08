package org.jscsi.target.scsi.cdb;

import java.nio.ByteBuffer;

import org.jscsi.target.storage.IStorageModule;

/**
 * This abstract class represents Command Descriptor Blocks for the <code>READ</code> and <code>WRITE</code>
 * SCSI commands. This grouping makes
 * sense, since, apart from the different values of the OPERATION CODE field,
 * Read and Write CDBs are identical.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class ReadOrWriteCdb extends CommandDescriptorBlock {

    /**
     * The logical block address of the first logical block of data, where data
     * shall be read from or written to.
     * 
     * @see #transferLength
     */
    private final long logicalBlockAddress;

    /**
     * The TRANSFER LENGTH field specifies the number of contiguous logical
     * blocks of data that shall be read/written and transferred to/from the
     * (initiator's) data-in buffer, starting with the logical block specified
     * by the {@link #logicalBlockAddress} field.
     */
    private final int transferLength;

    public ReadOrWriteCdb(ByteBuffer buffer) {
        super(buffer);
        logicalBlockAddress = deserializeLogicalBlockAddress(buffer);
        transferLength = deserializeTransferLength(buffer);
    }

    /**
     * Deserializes the value of the {@link #logicalBlockAddress} field.
     * 
     * @param buffer
     *            the {@link ByteBuffer} containing the CDB
     * @return value of the {@link #logicalBlockAddress} field
     */
    protected abstract long deserializeLogicalBlockAddress(final ByteBuffer buffer);

    /**
     * Deserializes the value of the {@link #transferLength} field.
     * 
     * @param buffer
     *            the {@link ByteBuffer} containing the CDB
     * @return value of the {@link #transferLength} field
     */
    protected abstract int deserializeTransferLength(final ByteBuffer buffer);

    /**
     * Returns the value of {@link #logicalBlockAddress}.
     * 
     * @return the value of {@link #logicalBlockAddress}
     */
    public final long getLogicalBlockAddress() {
        return logicalBlockAddress;
    }

    /**
     * Returns the value of {@link #transferLength}.
     * 
     * @return the value of {@link #transferLength}
     */
    public final int getTransferLength() {
        return transferLength;
    }

    /**
     * Returns the index position of the first byte used for encoding the {@link #logicalBlockAddress} field.
     * 
     * @return the index position of the first byte used for encoding the {@link #logicalBlockAddress} field
     */
    protected abstract int getLogicalBlockAddressFieldIndex();

    /**
     * Returns the index position of the first byte used for encoding the {@link #transferLength} field.
     * 
     * @return the index position of the first byte used for encoding the {@link #transferLength} field
     */
    protected abstract int getTransferLengthFieldIndex();

    /**
     * This method is used for signaling an illegal value of the {@link #logicalBlockAddress} variable.
     * <p>
     * This method must be called if the {@link #logicalBlockAddress} lies outside the bounds of the used
     * medium.
     * 
     * @see #getIllegalFieldPointers()
     * @see IStorageModule#checkBounds(long, int)
     */
    public final void addIllegalFieldPointerForLogicalBlockAddress() {
        addIllegalFieldPointer(getLogicalBlockAddressFieldIndex());
    }

    /**
     * This method is used for signaling an illegal value of the {@link #transferLength} variable.
     * <p>
     * This method must be called if the {@link #transferLength} field value would result in accessing of
     * out-of-bounds blocks of the storage medium.
     * 
     * @see #getIllegalFieldPointers()
     * @see IStorageModule#checkBounds(long, int)
     */
    public final void addIllegalFieldPointerForTransferLength() {
        addIllegalFieldPointer(getTransferLengthFieldIndex());
    }
}
