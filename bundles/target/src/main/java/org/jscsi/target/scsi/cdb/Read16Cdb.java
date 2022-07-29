package org.jscsi.target.scsi.cdb;


import java.nio.ByteBuffer;

import org.jscsi.target.util.ReadWrite;


/**
 * This class represents Command Descriptor Blocks for the READ (16) SCSI command.
 *
 * <p>
 * The READ (16) command requests that the device server read the specified logical block(s) and transfer them to
 * the data-in buffer. Each logical block read includes user data and, if the medium is formatted with protection information
 * enabled, protection information. Each logical block transferred includes user data and may include protection information,
 * based on the RDPROTECT field and the medium format.
 *
 * @author CHEN Qingcan
 */
public final class Read16Cdb extends ReadCdb {

    public Read16Cdb (final ByteBuffer buffer) {
        super(buffer);// OPERATION CODE + CONTROL
    }

    @Override
    protected long deserializeLogicalBlockAddress (ByteBuffer buffer) {
        return ReadWrite.readEightByteInt (buffer, 2);
    }

    @Override
    protected int deserializeTransferLength (ByteBuffer buffer) {
        return ReadWrite.readFourByteInt (buffer, 10);
    }

    @Override
    protected int getLogicalBlockAddressFieldIndex () {
        return 2;
    }

    @Override
    protected int getTransferLengthFieldIndex () {
        return 10;
    }

}
