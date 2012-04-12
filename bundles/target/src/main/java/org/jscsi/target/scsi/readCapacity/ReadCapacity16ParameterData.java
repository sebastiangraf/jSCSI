package org.jscsi.target.scsi.readCapacity;

import java.nio.ByteBuffer;

import org.jscsi.target.util.ReadWrite;

/**
 * <code>READ CAPACITY (16)</code> parameter data is sent in response to a
 * successful <code> READ CAPACITY (16)</code> SCSI command.
 * <p>
 * Only the fields common to all {@link ReadCapacityParameterData} can be set in the constructor. All other
 * fields and flags are <code>zero</code>. This means that the initiator is told that the device does not
 * support protection information (protection type 0), maps each logical block directly to one physical block,
 * beginning with the first one.
 * 
 * @author Andreas Ergenzinger
 */
public final class ReadCapacity16ParameterData extends ReadCapacityParameterData {

    /**
     * The length in bytes of serialized READ CAPACITY (16) parameter data.
     */
    private static final int SIZE = 32;

    public ReadCapacity16ParameterData(final long returnedLogicalBlockAddress, int logicalBlockLengthInBytes) {
        super(returnedLogicalBlockAddress, logicalBlockLengthInBytes);
    }

    public void serialize(ByteBuffer byteBuffer, int index) {
        // returned logical block address
        ReadWrite.writeLong(byteBuffer, returnedLogicalBlockAddress, index);

        // logical block length in bytes
        ReadWrite.writeInt(logicalBlockLengthInBytes, byteBuffer, index + 8);
    }

    public int size() {
        return SIZE;
    }

}
