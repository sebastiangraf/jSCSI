package org.jscsi.target.scsi.cdb;

import java.nio.ByteBuffer;

/**
 * This is an abstract super-class for command descriptor blocks that that are
 * used for requesting information about the capacity of the storage device.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class ReadCapacityCdb extends CommandDescriptorBlock {

    /**
     * The LOGICAL BLOCK ADDRESS field specifies the LBA of the first logical
     * block accessed by this command.
     * <p>
     * If the specified LBA exceeds the capacity of the medium, then the device server shall terminate the
     * command with CHECK CONDITION status with the sense key set to ILLEGAL REQUEST and the additional sense
     * code set to LOGICAL BLOCK ADDRESS OUT OF RANGE.
     * 
     * @see #partialMediumIndicator
     */
    protected final long logicalBlockAddress;

    /**
     * A partial medium indicator (PMI) bit set to zero specifies that the
     * device server return information on the last logical block on the
     * direct-access block device.
     * <p>
     * A PMI bit set to one specifies that the device server return information on the last logical block
     * after that specified in the LOGICAL BLOCK ADDRESS field before a substantial vendor specific delay in
     * data transfer may be encountered.
     * <p>
     * The LOGICAL BLOCK ADDRESS field shall be set to zero if the PMI bit is set to zero. If the PMI bit is
     * set to zero and the LOGICAL BLOCK ADDRESS field is not set to zero, then the device server shall
     * terminate the command with CHECK CONDITION status with the sense key set to ILLEGAL REQUEST and the
     * additional sense code set to INVALID FIELD IN CDB.
     * 
     * @see #logicalBlockAddress
     */
    protected final boolean partialMediumIndicator;

    public ReadCapacityCdb(final ByteBuffer buffer) {
        super(buffer);
        logicalBlockAddress = deserializeLogicalBlockAddress(buffer);
        partialMediumIndicator = deserializePartialMediumIndicator(buffer);
        // check constraint
        if (!partialMediumIndicator && logicalBlockAddress > 0)
            addIllegalFieldPointer(2);
    }

    protected abstract long deserializeLogicalBlockAddress(ByteBuffer buffer);

    protected abstract boolean deserializePartialMediumIndicator(ByteBuffer buffer);

    public final long getLogicalBlockAddress() {
        return logicalBlockAddress;
    }

    public final boolean getPartialMediumIndicator() {
        return partialMediumIndicator;
    }
}
