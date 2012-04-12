package org.jscsi.target.scsi.readCapacity;

import org.jscsi.target.scsi.IResponseData;

/**
 * <code>READ CAPACITY</code> parameter data is sent in response to a successful
 * <code>READ CAPACITY (10)</code> or <code>READ CAPACITY (16)</code> SCSI
 * command and is mainly used to inform the initiator about the number and
 * length of the unit's logical blocks.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class ReadCapacityParameterData implements IResponseData {

    protected final long returnedLogicalBlockAddress;

    protected final int logicalBlockLengthInBytes;

    public ReadCapacityParameterData(final long returnedLogicalBlockAddress,
        final int logicalBlockLengthInBytes) {
        this.returnedLogicalBlockAddress = returnedLogicalBlockAddress;
        this.logicalBlockLengthInBytes = logicalBlockLengthInBytes;
    }
}
