package org.jscsi.target.scsi.report;


import java.nio.ByteBuffer;

import org.jscsi.target.scsi.IResponseData;

/**
 * Supported one_command parameter data format.
 *
 * @author CHEN Qingcan
 */
public final class OneOpCode implements IResponseData {

    private final int reqOpCode;

    /**
     * @param reqOpCode requested operation code
     */
    public OneOpCode (int reqOpCode) {
        this.reqOpCode  = reqOpCode;
        // TODO determine whether supported by reqOpCode
    }

    @Override
    public void serialize (ByteBuffer out, int index) {
        out.position (index);
        out.put ((byte) 0);
        out.put ((byte) 0b011); // The device server supports the requested command in conformance with a SCSI standard.
        out.put ((byte) 0);
        out.put ((byte) 1); // CDB size
        out.put ((byte) reqOpCode); // CDB usage data
    }

    @Override
    public int size () {
        return 5;
    }

}
