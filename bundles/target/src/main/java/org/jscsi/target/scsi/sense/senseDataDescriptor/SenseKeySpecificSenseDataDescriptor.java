package org.jscsi.target.scsi.sense.senseDataDescriptor;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.sense.SenseData;
import org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific.SenseKeySpecificData;

/**
 * The sense key specific sense data descriptor provides additional information
 * about the exception condition. The format and content of the sense-key
 * specific data depends on the value in the {@link SenseData#senseKey} field.
 * 
 * @author Andreas Ergenzinger
 */
public final class SenseKeySpecificSenseDataDescriptor extends SenseDataDescriptor {

    /**
     * The byte position of the SENSE-KEY-SPECIFIC DATA field.
     */
    private static final int SENSE_KEY_SPECIFIC_DATA_INDEX = 4;

    /**
     * Contains sense-key-specific information.
     */
    private final SenseKeySpecificData senseKeySpecificData;

    /**
     * The constructor
     * 
     * @param senseKeySpecificData
     *            provides more detailed information
     */
    public SenseKeySpecificSenseDataDescriptor(final SenseKeySpecificData senseKeySpecificData) {
        super(SenseDataDescriptorType.SENSE_KEY_SPECIFIC, 0x06);// additional
                                                                // length
        this.senseKeySpecificData = senseKeySpecificData;
    }

    @Override
    protected final void serializeSpecificFields(final ByteBuffer byteBuffer, final int index) {

        senseKeySpecificData.serialize(byteBuffer, index + SENSE_KEY_SPECIFIC_DATA_INDEX);
    }
}
