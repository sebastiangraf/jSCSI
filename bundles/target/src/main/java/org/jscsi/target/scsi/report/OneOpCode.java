package org.jscsi.target.scsi.report;


import java.nio.ByteBuffer;

import org.jscsi.target.scsi.IResponseData;
import org.jscsi.target.scsi.cdb.ScsiOperationCode;

/**
 * Supported one_command parameter data format.
 *
 * @author CHEN Qingcan
 */
public final class OneOpCode implements IResponseData {

    private final byte    reqOpCode;
    private final boolean isSupported;

    /**
     * @param reqOpCode requested operation code
     */
    public OneOpCode (int reqOpCode) {
        this.reqOpCode   = (byte) reqOpCode;
        this.isSupported = ScsiOperationCode.valueOf (this.reqOpCode) != null;
    }

    @Override
    public void serialize (ByteBuffer out, int index) {
        out.position (index);
        out.put ((byte) 0);
        out.put (isSupported ?
            (byte) 0b011 : // The device server supports the requested command in conformance with a SCSI standard.
            (byte) 0b000   // Data about the requested SCSI command is not currently available.
        );
        out.put ((byte) 0);
        out.put ((byte) 1);  // CDB size
        out.put (reqOpCode); // CDB usage data
    }

    @Override
    public int size () {
        return 5;
    }

}
