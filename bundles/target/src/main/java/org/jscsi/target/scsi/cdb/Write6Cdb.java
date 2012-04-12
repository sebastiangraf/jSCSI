package org.jscsi.target.scsi.cdb;

import java.nio.ByteBuffer;

import org.jscsi.target.util.ReadWrite;

/**
 * This class represents Command Descriptor Blocks for the <code>WRITE (6)</code> SCSI command.
 * <p>
 * The WRITE (6) command requests that the device server transfer the specified logical block(s) from the
 * data-out buffer and write them.
 * <p>
 * Each logical block transferred includes user data but does not include protection information. Each logical
 * block written includes user data and, if the medium is formatted with protection information enabled,
 * protection information.
 * <p>
 * A TRANSFER LENGTH field set to zero specifies that 256 logical blocks shall be written. Any other value
 * specifies the number of logical blocks that shall be written.
 * 
 * @author Andreas Ergenzinger
 */
public class Write6Cdb extends WriteCdb {

    public Write6Cdb(ByteBuffer buffer) {
        super(buffer);
    }

    @Override
    protected long deserializeLogicalBlockAddress(ByteBuffer buffer) {
        // the first three bits of byte 1 are reserved, i.e. must be 0,
        // check that
        final byte b = buffer.get(1);
        if (((b >> 5) & 7) != 0)
            addIllegalFieldPointer(1);

        // read the field's value
        return ((b & 31) << 16) | ReadWrite.readTwoByteInt(buffer, 2);
    }

    @Override
    protected int deserializeTransferLength(ByteBuffer buffer) {
        /*
         * A TRANSFER LENGTH field set to zero specifies that 256 logical blocks
         * shall be written. Any other value specifies the number of logical
         * blocks that shall be written.
         */
        final int value = ReadWrite.readOneByteInt(buffer, 4);
        if (value == 0)
            return 256;
        return value;
    }

    @Override
    protected int getLogicalBlockAddressFieldIndex() {
        return 1;
    }

    @Override
    protected int getTransferLengthFieldIndex() {
        return 4;
    }

}
