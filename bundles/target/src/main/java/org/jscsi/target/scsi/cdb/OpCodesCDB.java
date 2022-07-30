package org.jscsi.target.scsi.cdb;


import java.nio.ByteBuffer;

import org.jscsi.target.util.ReadWrite;


/**
 * This class represents Command Descriptor Blocks for the REPORT SUPPORTED OPERATION CODES SCSI command.
 *
 * @author CHEN Qingcan
 */
public class OpCodesCDB extends CommandDescriptorBlock {

    private final int inReportingOptions;
    private final int inAllocationLength;

    public OpCodesCDB (ByteBuffer buffer) {
        super (buffer);
        inReportingOptions = ReadWrite.readOneByteInt  (buffer, 2) & 7;
        inAllocationLength = ReadWrite.readFourByteInt (buffer, 6);
    }

    /** REPORTING OPTIONS field */
    public int getReportingOptions () {
        return inReportingOptions;
    }

    /** ALLOCATION LENGTH field */
    public int getAllocationLength () {
        return inAllocationLength;
    }

}
