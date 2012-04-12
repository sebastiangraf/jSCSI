package org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.cdb.CommandDescriptorBlock;
import org.jscsi.target.util.BitManip;
import org.jscsi.target.util.ReadWrite;

/**
 * Field pointer sense-key-specific data is used to indicate that a certain
 * field of a received command descriptor block contained an illegal value.
 * 
 * @see CommandDescriptorBlock
 * @author Andreas Ergenzinger
 */
public final class FieldPointerSenseKeySpecificData extends SenseKeySpecificData {

    /**
     * A command data (C/D) bit set to one indicates that the illegal parameter
     * is in the CDB. A C/D bit set to zero indicates that the illegal parameter
     * is in the data parameters sent by the application client in the Data-Out
     * Buffer.
     */
    private final boolean commandData;

    /**
     * A bit pointer valid (BPV) bit set to zero indicates that the value in the
     * BIT POINTER field is not valid.
     */
    private final boolean bitPointerValid;

    /**
     * If {@link #bitPointerValid} is <code>true</code>, the BIT POINTER field
     * specifies which bit of the byte designated by the FIELD POINTER field is
     * in error. When a multiple-bit field is in error, the BIT POINTER field
     * shall point to the first bit (i.e., the left-most bit) of the field.
     */
    private final int bitPointer;

    /**
     * The FIELD POINTER field indicates which byte of the CDB or of the
     * parameter data was in error. Bytes are numbered starting from zero, as
     * shown in the tables describing the commands and parameters. When a
     * multiple-byte field is in error, the field pointer shall point to the
     * first byte (i.e., the left-most byte) of the field. If several
     * consecutive bytes are reserved, each shall be treated as a single-byte
     * field.
     */
    private final short fieldPointer;

    public FieldPointerSenseKeySpecificData(final boolean senseKeySpecificDataValid,
        final boolean commandData, final boolean bitPointerValid, final int bitPointer, final int fieldPointer) {
        super(senseKeySpecificDataValid);
        this.commandData = commandData;
        this.bitPointerValid = bitPointerValid;
        this.bitPointer = bitPointer;
        this.fieldPointer = (short)fieldPointer;
    }

    @Override
    protected void serializeSpecificFields(final ByteBuffer byteBuffer, final int index) {

        byte b = byteBuffer.get(index);// SKSV bit has already been set and has
                                       // to be preserved

        // command data
        b = BitManip.getByteWithBitSet(b, 6, commandData);

        // bit pointer valid
        b = BitManip.getByteWithBitSet(b, 3, bitPointerValid);

        // bit pointer
        b &= (bitPointer & 7);

        // store first byte
        byteBuffer.put(index, b);

        // field pointer
        ReadWrite.writeTwoByteInt(byteBuffer, fieldPointer, byteBuffer.position());
    }
}
