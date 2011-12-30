package org.jscsi.target.scsi.modeSense;

import java.nio.ByteBuffer;

/**
 * A {@link ModeParameterHeader} sub-class. Instances of this class are sent in
 * response to <code>MODE SENSE (6)</code> SCSI commands and have a serialized
 * length of 4 bytes.
 * 
 * @author Andreas Ergenzinger
 */
public final class ModeParameterHeader6 extends ModeParameterHeader {

    /**
     * The length of this object when serialized.
     */
    static final int SIZE = 4;

    /**
     * The length in bytes of the MODE DATA LENGTH field.
     */
    static final int MODE_DATA_LENGTH_FIELD_SIZE = 1;

    /**
     * The constructor.
     * 
     * @param modeDataLength
     *            the total length in bytes of all MODE DATA list elements
     * @param blockDescriptorLength
     *            the total length in bytes of all BLOCK DESCRIPTOR list
     *            elements
     */
    public ModeParameterHeader6(final int modeDataLength,
            final int blockDescriptorLength) {
        super(modeDataLength, blockDescriptorLength);
    }

    public void serialize(ByteBuffer byteBuffer, int index) {
        byteBuffer.position(index);
        byteBuffer.put((byte) modeDataLength);
        byteBuffer.put(mediumType);
        byteBuffer.put(deviceSpecificParameter);
        byteBuffer.put((byte) blockDescriptorLength);
    }

    public int size() {
        return SIZE;
    }

}
