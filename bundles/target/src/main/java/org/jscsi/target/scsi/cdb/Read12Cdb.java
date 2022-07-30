package org.jscsi.target.scsi.cdb;


import java.nio.ByteBuffer;

import org.jscsi.target.util.ReadWrite;


/**
 * This class represents Command Descriptor Blocks for the READ (12) SCSI command.
 *
 * <p>
 * The READ (12) command requests that the device server read the specified logical block(s) and transfer them to
 * the data-in buffer. Each logical block read includes user data and, if the medium is formatted with protection information
 * enabled, protection information. Each logical block transferred includes user data and may include protection information,
 * based on the RDPROTECT field and the medium format.
 *
 * @author CHEN Qingcan
 */
public final class Read12Cdb extends ReadCdb {

    public Read12Cdb (final ByteBuffer buffer) {
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
