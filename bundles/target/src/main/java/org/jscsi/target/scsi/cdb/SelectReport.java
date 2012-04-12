package org.jscsi.target.scsi.cdb;

import org.jscsi.target.scsi.lun.AddressMethod;

/**
 * The SELECT REPORT field is a member of the {@link ReportLunsCDB} and
 * specifies the types of logical unit addresses that shall be reported.
 * <p>
 * The field is two bits long.
 * 
 * @author Andreas Ergenzinger
 */
public enum SelectReport {

    /**
     * The list shall contain the logical units accessible to the I_T nexus with
     * the following addressing methods (see SAM):
     * <ol>
     * <li>{@link AddressMethod#LOGICAL_UNIT_ADDRESSING_METHOD},</li>
     * <li>{@link AddressMethod#PERIPHERAL_DEVICE_ADDRESSING_METHOD}, and</li>
     * <li>{@link AddressMethod#FLAT_SPACE_ADDRESSING_METHOD}.</li>
     * </ol>
     * If there are no logical units, the LUN LIST LENGTH field shall be zero.
     */
    SELECTED_ADDRESSING_METHODS((byte)0),
    /**
     * The list shall contain only well known logical units, if any. If there
     * are no well known logical units, the LUN LIST LENGTH field shall be zero.
     */
    WELL_KNOWN_LUNS_ONLY((byte)1),
    /**
     * The list shall contain all logical units accessible to the I_T nexus.
     */
    ALL((byte)2);
    // all other values are reserved

    /**
     * The serialized value of this {@link SelectReport} object.
     */
    private final byte value;

    private SelectReport(final byte value) {
        this.value = value;
    }

    /**
     * Returns the serialized value of this {@link SelectReport} object.
     * 
     * @return the serialized value of this {@link SelectReport} object
     */
    public byte getValue() {
        return value;
    }

    /**
     * Returns the {@link SelectReport} corresponding to the passed value.
     * 
     * @param value
     *            the value of a SELECT REPORT field
     * @return the {@link SelectReport} corresponding to the passed value or <code>null</code> if none exists
     */
    public final static SelectReport getValue(byte value) {
        if (0 <= value && value <= 2)
            return values()[value];
        return null;
    }
}
