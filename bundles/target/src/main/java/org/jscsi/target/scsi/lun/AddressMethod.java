package org.jscsi.target.scsi.lun;

/**
 * The ADDRESS METHOD field of a LOGICAL UNIT NUMBER's ADDRESSING field (from
 * FIRST LEVEL ADDRESSING to FOURTH LEVEL ADDRESSING) defines the contents of
 * the ADDRESS METHOD SPECIFIC field.
 * <p>
 * This field is two bits long.
 * 
 * @author Andreas Ergenzinger
 */
public enum AddressMethod {
    /**
     * The SCSI target device should relay the received command or task
     * management function to the addressed dependent logical unit.
     */
    PERIPHERAL_DEVICE_ADDRESSING_METHOD((byte)0),
    /**
     * The flat space addressing method specifies a logical unit at the current
     * level. The contents of all hierarchical structure addressing fields
     * following a flat space addressing method addressing field shall be
     * ignored.
     */
    FLAT_SPACE_ADDRESSING_METHOD((byte)1),
    /**
     * The SCSI device should relay the received command or task management
     * function to the addressed dependent logical unit.
     */
    LOGICAL_UNIT_ADDRESSING_METHOD((byte)2),
    /**
     * Extended logical unit addressing specifies a logical unit at the current
     * level.
     * <p>
     * Extended logical unit addressing builds on the formats defined for dependent logical units (see
     * 4.6.19.4) but may be used by SCSI devices having single level logical unit structure. In dependent
     * logical unit addressing, the logical unit information at each level fits in exactly two bytes. Extended
     * logical unit addresses have sizes of two bytes, four bytes, six bytes, or eight bytes.
     */
    EXTENDED_LOGICAL_UNIT__ADDRESSING_METHOD((byte)3);

    /**
     * The serialized value of the object.
     */
    private final byte value;

    private AddressMethod(final byte value) {
        this.value = value;
    }

    /**
     * Returns the serialized value of the object.
     * 
     * @return the serialized value of the object
     */
    public byte getValue() {
        return value;
    }

    /**
     * Returns the {@link AddressMethod} with a matching {@link #value} or <code>null</code> if no such
     * {@link AddressMethod} exists.
     * 
     * @param value
     *            the value from a serialized ADDRESS METHOD field
     * @return the {@link AddressMethod} with a matching {@link #value} or or <code>null</code>
     */
    public static AddressMethod getValue(int value) {
        if (0 <= value && value <= 3)
            return values()[value];
        return null;
    }
}
