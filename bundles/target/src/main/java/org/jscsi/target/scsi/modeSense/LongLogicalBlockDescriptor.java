package org.jscsi.target.scsi.modeSense;

import java.nio.ByteBuffer;

import org.jscsi.target.util.ReadWrite;

/**
 * A class representing the content of LONG LBA MODE PAREMETER LOGICAL BLOCK
 * DESCRIPTOR fields, which are part of {@link ModeParameterList} objects. This
 * long format must be used if the LONG LBA bit is set in the {@link ModeParameterList} objects's header.
 * 
 * @see ShortLogicalBlockDescriptor
 * @author Andreas Ergenzinger
 */
public class LongLogicalBlockDescriptor extends LogicalBlockDescriptor {

    /**
     * The serialized length in bytes of instances of this class.
     */
    static final int SIZE = 16;

    /**
     * The constructor.
     * 
     * @param numberOfLogicalBlocks
     *            the number of equal-length logical blocks into which the
     *            storage medium is divided
     * @param logicalBlockLength
     *            the length in bytes of the logical blocks
     */
    public LongLogicalBlockDescriptor(long numberOfLogicalBlocks, int logicalBlockLength) {
        super(numberOfLogicalBlocks, logicalBlockLength);
    }

    public void serialize(ByteBuffer byteBuffer, int index) {
        // NUMBER OF LOGICAL BLOCKS
        ReadWrite.writeLong(byteBuffer,// buffer
            numberOfLogicalBlocks,// value
            index);// index

        // LOGICAL BLOCK LENGTH
        ReadWrite.writeInt(logicalBlockLength,// value
            byteBuffer,// buffer
            index + 12);// index
    }

    public int size() {
        return SIZE;
    }
}
