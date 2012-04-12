package org.jscsi.target.scsi.modeSense;

import org.jscsi.target.scsi.ISerializable;

/**
 * An abstract parent class for LOGICAL BLOCK DESCRIPTORs used by direct-access
 * block devices as part of {@link ModeParameterList} objects.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class LogicalBlockDescriptor implements ISerializable {

    /**
     * The number of equal-length logical blocks into which the storage medium
     * is divided.
     */
    protected final long numberOfLogicalBlocks;

    /**
     * The length in bytes of the logical blocks.
     */
    protected final int logicalBlockLength;

    /**
     * The constructor.
     * 
     * @param numberOfLogicalBlocks
     *            the number of equal-length logical blocks into which the
     *            storage medium is divided
     * @param logicalBlockLength
     *            the length in bytes of the logical blocks
     */
    public LogicalBlockDescriptor(final long numberOfLogicalBlocks, final int logicalBlockLength) {
        this.numberOfLogicalBlocks = numberOfLogicalBlocks;
        this.logicalBlockLength = logicalBlockLength;
    }
}
