package org.jscsi.target.scsi.sense.senseDataDescriptor;

import org.jscsi.target.scsi.sense.SenseData;

/**
 * The SenseDataDescriptorType is part of a {@link SenseDataDescriptor} and
 * determines the object's (non-abstract sub)class.
 * <p>
 * Vendor specific sense data descriptors types (values 0x80 to 0xff) and respective
 * {@link SenseDataDescriptor}s are not implemented!
 * 
 * @author Andreas Ergenzinger
 */
public enum SenseDataDescriptorType {

    /**
     * The information sense data descriptor provides information that is
     * device-type or command specific and is defined in a command standard.
     */
    INFORMATION((byte)0x00),
    /**
     * The command-specific information sense data descriptor provides
     * information that depends on the command on which the exception condition
     * occurred.
     */
    COMMAND_SPECIFIC_INFORMATION((byte)0x01),
    /**
     * The sense key specific sense data descriptor provides additional
     * information about the exception condition. The format and content of the
     * sense-key specific data depends on the value in the {@link SenseData#senseKey} field.
     */
    SENSE_KEY_SPECIFIC((byte)0x02),
    /**
     * The field replaceable unit sense data descriptor (see table 24) provides
     * information about a component that has failed.
     */
    FIELD_REPLACEABLE_UNIT((byte)0x03),
    /**
     * See SSC.
     */
    STREAM_COMMANDS((byte)0x04),
    /**
     * See SBC.
     */
    BLOCK_COMMANDS((byte)0x05),
    /**
     * See OSD.
     */
    OSD_OBJECT_IDENTIFICATION((byte)0x06),
    /**
     * See OSD.
     */
    OSD_RESPONSE_INTEGRITY_CHECK_VALUE((byte)0x07),
    /**
     * See OSD.
     */
    OSD_ATTRIBUTE_IDENTIFICATION((byte)0x08),
    /**
     * See SAT.
     */
    ATA_RETURN((byte)0x09);
    /*
     * 0x0a to 0x7f are reserved. 0x80 to 0xff are vendor specific
     * 
     * Vendor specific sense data descriptors are not implemented!
     */

    private final byte value;

    private SenseDataDescriptorType(byte value) {
        this.value = value;
    }

    public final byte getValue() {
        return value;
    }
}
