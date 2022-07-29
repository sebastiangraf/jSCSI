package org.jscsi.target.scsi.cdb;


import java.nio.ByteBuffer;

import org.jscsi.target.util.ReadWrite;


/**
 * This class represents Command Descriptor Blocks for the WRITE (12) SCSI command.
 *
 * <p>
 * The WRITE (12) command requests that the device server transfer the specified logical block(s) from the data-out
 * buffer and write them. Each logical block transferred includes user data and may include protection information, based on the
 * WRPROTECT field and the medium format. Each logical block written includes user data and, if the medium is formatted with
 * protection information enabled, protection information.
 *
 * @author CHEN Qingcan
 */
public class Write12Cdb extends WriteCdb {

    public Write12Cdb (ByteBuffer buffer) {
        super(buffer);// OPERATION CODE + CONTROL
    }

    @Override
    protected long deserializeLogicalBlockAddress (ByteBuffer buffer) {
        return ReadWrite.readUnsignedInt (buffer, 2);
    }

    @Override
    protected int deserializeTransferLength (ByteBuffer buffer) {
        return ReadWrite.readFourByteInt (buffer, 6);
    }

    @Override
    protected int getLogicalBlockAddressFieldIndex () {
        return 2;
    }

    @Override
    protected int getTransferLengthFieldIndex () {
        return 6;
    }

}
