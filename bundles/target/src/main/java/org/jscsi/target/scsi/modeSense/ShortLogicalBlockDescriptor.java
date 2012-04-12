package org.jscsi.target.scsi.modeSense;

import java.nio.ByteBuffer;

import org.jscsi.target.util.ReadWrite;

/**
 * A class representing the content of SHORT LBA MODE PAREMETER LOGICAL BLOCK
 * DESCRIPTOR fields, which are part of {@link ModeParameterList} objects. This
 * short format must be used if the LONG LBA bit is not set in the {@link ModeParameterList} objects's header.
 * 
 * @see LongLogicalBlockDescriptor
 * @author Andreas Ergenzinger
 */
public final class ShortLogicalBlockDescriptor extends LogicalBlockDescriptor {

    /**
     * The serialized length in bytes of instances of this class.
     */
    static final int SIZE = 8;

    /**
     * The constructor.
     * 
     * @param numberOfLogicalBlocks
     *            the number of equal-length logical blocks into which the
     *            storage medium is divided
     * @param logicalBlockLength
     *            the length in bytes of the logical blocks
     */
    public ShortLogicalBlockDescriptor(long numberOfLogicalBlocks, int logicalBlockLength) {
        super(numberOfLogicalBlocks, logicalBlockLength);
    }

    public void serialize(ByteBuffer byteBuffer, int index) {
        // NUMBER OF LOGICAL BLOCKS
        ReadWrite.writeInt((int)numberOfLogicalBlocks,// value
            byteBuffer,// buffer
            index);// start index

        // LOGICAL BLOCK LENGTH
        ReadWrite.writeThreeByteInt(byteBuffer,// buffer
            logicalBlockLength,// value
            index + 5);// index
    }

    public int size() {
        return SIZE;
    }

}
