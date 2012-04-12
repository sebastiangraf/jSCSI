package org.jscsi.target.scsi.readCapacity;

import java.nio.ByteBuffer;

import org.jscsi.target.util.ReadWrite;

/**
 * <code>READ CAPACITY (10)</code> parameter data is sent in response to a
 * successful <code> READ CAPACITY (10)</code> SCSI command.
 * 
 * @author Andreas Ergenzinger
 */
public final class ReadCapacity10ParameterData extends ReadCapacityParameterData {

    /**
     * Specifies the limit to the {@link ReadCapacityParameterData#returnedLogicalBlockAddress} field for
     * READ CAPACITY (10) parameter data.
     * <p>
     * If the value of the RETURNED LOGICAL BLOCK ADDRESS field does not fit completely into the available
     * four bytes, then this value will be inserted instead.
     */
    private static final long MAX_RETURNED_LOGICAL_BLOCK_ADDRESS = 0xffffffffL;

    /**
     * The length in bytes of serialized READ CAPACITY (16) parameter data.
     */
    private static final int SIZE = 8;

    public ReadCapacity10ParameterData(final long returnedLogicalBlockAddress,
        final int logicalBlockLengthInBytes) {
        super(returnedLogicalBlockAddress, logicalBlockLengthInBytes);
    }

    public void serialize(ByteBuffer byteBuffer, int index) {
        // returned logical block address
        // trim to size, and prevent overflow (initiator has to use READ
        // CAPACITY (16))
        final int rlba = (int)Math.min(returnedLogicalBlockAddress, MAX_RETURNED_LOGICAL_BLOCK_ADDRESS);
        ReadWrite.writeInt(rlba, byteBuffer, index);

        // logical block length in bytes
        ReadWrite.writeInt(logicalBlockLengthInBytes, byteBuffer, index + 4);
    }

    public int size() {
        return SIZE;
    }

}
