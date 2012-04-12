package org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.sense.SenseData;
import org.jscsi.target.scsi.sense.SenseKey;
import org.jscsi.target.scsi.sense.senseDataDescriptor.CommandSpecificSenseDataDescriptor;
import org.jscsi.target.util.BitManip;
import org.jscsi.target.util.ReadWrite;

/**
 * Segment pointer sense-key-specific data is to be used, when the sense key of
 * the enclosing {@link SenseData} has the value {@link SenseKey#COPY_ABORTED}.
 * 
 * @author Andreas Ergenzinger
 */
public class SegmentPointerSenseKeySpecificData extends SenseKeySpecificData {

    /**
     * The segment descriptor (SD) bit indicates whether the field pointer is
     * relative to the start of the parameter list or to the start of a segment
     * descriptor. An SD bit set to zero indicates that the field pointer is
     * relative to the start of the parameter list. An SD bit set to one
     * indicates that the field pointer is relative to the start of the segment
     * descriptor indicated by the third and fourth bytes of the
     * {@link CommandSpecificSenseDataDescriptor#commandSpecificInformation} field.
     */
    private final boolean segmentDescriptor;

    /**
     * A bit pointer valid (BPV) bit set to zero indicates that the value in the {@link #bitPointer} field is
     * not valid. A BPV bit set to one indicates
     * that the {@link #bitPointer} field specifies which bit of the byte
     * designated by the {@link #fieldPointer} field is in error. When a
     * multiple-bit field is in error, the {@link #bitPointerValid} field shall
     * point to the most-significant (i.e., left-most) bit of the field.
     */
    private final boolean bitPointerValid;

    /**
     * The FIELD POINTER field indicates which byte of the parameter list or
     * segment descriptor was in error.
     * 
     * @see #bitPointerValid
     */
    private final short fieldPointer;

    /**
     * Points to the leftmost bit of the field in error.
     * 
     * @see #bitPointerValid
     */
    private final int bitPointer;

    /**
     * The constructor.
     * <p>
     * All parameters are used to initialize the member variables with the same name.
     * 
     * @param senseKeySpecificDataValid
     * @param segmentDescriptor
     * @param bitPointerValid
     * @param bitPointer
     * @param fieldPointer
     */
    public SegmentPointerSenseKeySpecificData(final boolean senseKeySpecificDataValid,
        final boolean segmentDescriptor, final boolean bitPointerValid, final int bitPointer,
        final int fieldPointer) {
        super(senseKeySpecificDataValid);
        this.segmentDescriptor = segmentDescriptor;
        this.bitPointerValid = bitPointerValid;
        this.bitPointer = bitPointer;
        this.fieldPointer = (short)fieldPointer;
    }

    @Override
    protected void serializeSpecificFields(final ByteBuffer byteBuffer, final int index) {

        byte b = byteBuffer.get(index);// SKSV bit has already been set and has
                                       // to be preserved

        // segment descriptor
        b = BitManip.getByteWithBitSet(b, 5, segmentDescriptor);

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
