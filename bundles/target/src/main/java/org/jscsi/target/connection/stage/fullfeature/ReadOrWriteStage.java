package org.jscsi.target.connection.stage.fullfeature;

import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.scsi.cdb.CommandDescriptorBlock;
import org.jscsi.target.scsi.cdb.ReadOrWriteCdb;
import org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific.FieldPointerSenseKeySpecificData;

/**
 * This is an abstract superclass for stages that handle PDUs with command
 * descriptor blocks of the {@link ReadOrWriteCdb} class.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class ReadOrWriteStage extends TargetFullFeatureStage {

    public ReadOrWriteStage(TargetFullFeaturePhase targetFullFeaturePhase) {
        super(targetFullFeaturePhase);
    }

    /**
     * Checks if the <code>LOGICAL BLOCK ADDRESS</code> and <code>TRANSFER
     * LENGTH</code> fields in the passed {@link ReadOrWriteCdb} are acceptable.
     * If illegal values are detected, an instance of {@link FieldPointerSenseKeySpecificData} describing the
     * problem will be
     * added to the {@link ReadOrWriteCdb}'s queue-
     * 
     * @param cdb
     *            a read or write command descriptor block to check
     * @see CommandDescriptorBlock#getIllegalFieldPointers()
     */
    protected void checkOverAndUnderflow(final ReadOrWriteCdb cdb) {
        // check if requested blocks are out of bounds
        final int boundsCheck =
            session.getStorageModule().checkBounds(cdb.getLogicalBlockAddress(), cdb.getTransferLength());
        // add illegal field pointer, or not
        if (boundsCheck == 1)
            cdb.addIllegalFieldPointerForLogicalBlockAddress();
        else if (boundsCheck == 2)
            cdb.addIllegalFieldPointerForTransferLength();
    }

}
