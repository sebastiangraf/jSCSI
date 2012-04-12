package org.jscsi.target.scsi.sense.senseDataDescriptor;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.sense.information.EightByteInformation;
import org.jscsi.target.util.BitManip;

/**
 * The information sense data descriptor provides information that is
 * device-type or command specific and is defined in a command standard.
 * 
 * @author Andreas Ergenzinger
 */
public final class InformationSenseDataDescriptor extends SenseDataDescriptor {

    /**
     * The position of the byte containing the VALID bit.
     */
    private static final int VALID_FLAG_BYTE_INDEX = 2;

    /**
     * The position of the INFORMATION field.
     */
    private static final int INFORMATION_INDEX = 4;

    /**
     * The VALID bit shall be set to one.
     * <p>
     * In previous versions of this standard and in the fixed format sense data, the VALID bit indicates
     * whether the contents of the INFORMATION field is valid as defined by a command standard. Since the
     * contents of the INFORMATION field are valid whenever an information sense data descriptor is included
     * in the sense data, the only legal value for the VALID bit is set to one.
     */
    private final boolean valid = true;

    /**
     * This field may contain additional information (but doesn't).
     */
    private final EightByteInformation information;

    /**
     * The constructor.
     * 
     * @param information
     *            {@link EightByteInformation} that may contain useful
     *            information
     */
    public InformationSenseDataDescriptor(final EightByteInformation information) {
        super(SenseDataDescriptorType.INFORMATION, // descriptor type
            0x0a); // additional length
        this.information = information;
    }

    @Override
    protected final void serializeSpecificFields(final ByteBuffer byteBuffer, final int index) {

        byte b = 0;

        // valid bit
        if (valid)
            b = BitManip.getByteWithBitSet(b, 7, true);
        byteBuffer.put(index + VALID_FLAG_BYTE_INDEX, b);

        // information
        information.serialize(byteBuffer, index + INFORMATION_INDEX);
    }

}
