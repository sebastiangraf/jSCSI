package org.jscsi.target.scsi.cdb;


import java.nio.ByteBuffer;

import org.jscsi.target.util.ReadWrite;


/**
 * This class represents Command Descriptor Blocks for the <code>REPORT SUPPORTED OPERATION CODES</code> SCSI command.
 *
 * @author CHEN Qingcan
 */
public class OpCodesCDB extends CommandDescriptorBlock {

    private final int inReportingOptions;
    private final int inRequestedOperationCode;
    private final int inRequestedServiceAction;
    private final int inAllocationLength;

    public OpCodesCDB (ByteBuffer buffer) {
        super (buffer);
        inReportingOptions       = ReadWrite.readOneByteInt  (buffer, 2) & 0b111;
        inRequestedOperationCode = ReadWrite.readOneByteInt  (buffer, 3);
        inRequestedServiceAction = ReadWrite.readTwoByteInt  (buffer, 4);
        inAllocationLength       = ReadWrite.readFourByteInt (buffer, 6);
    }

    /** REPORTING OPTIONS field */
    public int getReportingOptions () {
        return inReportingOptions;
    }

    /** REQUESTED OPERATION CODE field */
    public int getRequestedOperationCode () {
        return inRequestedOperationCode;
    }

    /** REQUESTED SERVICE ACTION field */
    public int getRequestedServiceAction () {
        return inRequestedServiceAction;
    }

    /** ALLOCATION LENGTH field */
    public int getAllocationLength () {
        return inAllocationLength;
    }

}
