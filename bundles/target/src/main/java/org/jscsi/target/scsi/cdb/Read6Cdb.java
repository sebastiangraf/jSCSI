package org.jscsi.target.scsi.cdb;

import java.nio.ByteBuffer;

import org.jscsi.target.util.ReadWrite;

/**
 * This class represents Command Descriptor Blocks for the <code>READ (6)
 * </code> SCSI command.
 * <p>
 * The READ (6) command requests that the device server read the specified logical block(s) and transfer them
 * to the data-in buffer. Each logical block read includes user data and, if the medium is formatted with
 * protection information enabled, protection information. Each logical block transferred includes user data
 * but does not include protection information.
 * <p>
 * Although the READ (6) command is limited to addressing up to 2,097,151 logical blocks, this command has
 * been maintained as mandatory since some system initialization routines require that the READ (6) command be
 * used. System initialization routines should migrate from the READ (6) command to the READ (10) command,
 * which is capable of addressing 4,294,947,295 logical blocks, or the READ (16) command, which is capable of
 * addressing 18,446,744,073,709,551,615 logical blocks.
 * 
 * @author Andreas Ergenzinger
 */
public class Read6Cdb extends ReadCdb {

    public Read6Cdb(ByteBuffer buffer) {
        super(buffer);
    }

    @Override
    protected long deserializeLogicalBlockAddress(ByteBuffer buffer) {
        // the first three bits of byte 1 are reserved i.e. must be 0
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
         * shall be read. Any other value specifies the number of logical blocks
         * that shall be read.
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
