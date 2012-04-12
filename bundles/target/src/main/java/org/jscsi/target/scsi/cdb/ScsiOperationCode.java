package org.jscsi.target.scsi.cdb;

/**
 * The OPERATION CODE of a Command Descriptor Block specifies the type of
 * command the initiator wants the target to perform.
 * <p>
 * The OPERATION CODE of the {@link CommandDescriptorBlock} has a GROUP CODE field and a COMMAND CODE field.
 * The three-bit GROUP CODE field provides for eight groups of command codes. The five-bit COMMAND CODE field
 * provides for thirty-two command codes in each group. A total of 256 possible operation codes exist.
 * <p>
 * The value of the GROUP CODE field specifies the {@link CommandDescriptorBlock}'s length.
 * 
 * @see CdbType
 * @author Andreas Ergenzinger
 */
public enum ScsiOperationCode {
    TEST_UNIT_READY((byte)0x00), REQUEST_SENSE((byte)0x03), FORMAT_UNIT((byte)0x04), READ_6((byte)0x08),
        WRITE_6((byte)0x0a), INQUIRY((byte)0x12), MODE_SELECT_6((byte)0x15), MODE_SENSE_6((byte)0x1a),
        SEND_DIAGNOSTIC((byte)0x1d), READ_CAPACITY_10((byte)0x25), READ_10((byte)0x28), WRITE_10((byte)0x2a),
        READ_CAPACITY_16((byte)0x9e), REPORT_LUNS((byte)0xa0);

    /**
     * The serialized value of the operation code.
     */
    private final byte value;

    /**
     * Maps byte values/index positions to {@link ScsiOperationCode} constants.
     */
    private static final ScsiOperationCode[] mapping = new ScsiOperationCode[256];
    static {// initialize mapping
        final ScsiOperationCode[] values = values();
        int index;
        for (ScsiOperationCode v : values) {
            index = (v.value & 255);
            mapping[index] = v;
        }
    }

    /**
     * Returns the {@link ScsiOperationCode} corresponding to the passed byte
     * value.
     * 
     * @param value
     *            the serialized value of a SCSI operation code
     * @return the corresponding {@link ScsiOperationCode} or <code>null</code> if the passed value is not
     *         known by the jSCSI Target
     */
    public static final ScsiOperationCode valueOf(final byte value) {
        return mapping[value & 255];
    }

    private ScsiOperationCode(final byte value) {
        this.value = value;
    }

    /**
     * Returns the serialized value of the operation code.
     * 
     * @return the serialized value of the operation code
     */
    public final byte value() {
        return value;
    }

    /**
     * The three-bit GROUP CODE field provides for eight groups of command
     * codes.
     * 
     * @return the three-bit GROUP CODE field
     */
    public int getGroupCode() {
        return (value >>> 5) & 7;
    }

    /**
     * Returns the five-bit COMMAND CODE field.
     * 
     * @return the five-bit COMMAND CODE field
     */
    public int getCommandCode() {
        return value & 31;
    }

    /**
     * Returns the {@link CdbType} for this operation code.
     * 
     * @return the {@link CdbType} for this operation code
     */
    public CdbType getCdbType() {
        return CdbType.getCdbType(this);
    }
}
