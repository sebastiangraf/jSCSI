package org.jscsi.target.scsi.inquiry;

import org.jscsi.target.scsi.cdb.InquiryCDB;

/**
 * Using the <code>INQUIRY</code> SCSI command, the initiator can request the
 * target to return vital product data (VPD) pages. The specific VPD page is
 * determined by the value of the {@link InquiryCDB#pageCode} field.
 * <p>
 * To find out which page was requested, first create a new {@link PageCode} object using the value provided
 * in the command descriptor block's PAGE CODE field, and then call its {@link #getVitalProductDataPageName()}
 * method. This complicated approach is necessary, since some VPD pages are associated with more than just one
 * page code.
 * 
 * @author Andreas Ergenzinger
 */
public class PageCode {

    /**
     * The value of the PAGE CODE field.
     */
    private final int value;

    /**
     * Creates a new {@link PageCode} object.
     * 
     * @param value
     *            the value of the PAGE CODE field
     */
    public PageCode(final byte value) {
        this.value = 255 & value;
    }

    /**
     * Returns the value of the PAGE CODE field.
     * 
     * @return the value of the PAGE CODE field
     */
    public final byte getValue() {
        return (byte)value;
    }

    /**
     * Returns the VPD page name associated with the PAGE CODE {@link #value}.
     * 
     * @return the VPD page name associated with the PAGE CODE {@link #value}
     */
    public final VitalProductDataPageName getVitalProductDataPageName() {
        if (value == 0x00)
            return VitalProductDataPageName.SUPPORTED_VPD_PAGES;// mandatory
        if (0x01 <= value && value <= 0x7f)
            return VitalProductDataPageName.ASCII_INFORMATION;
        if (value == 0x80)
            return VitalProductDataPageName.UNIT_SERIAL_NUMBER;
        if (value == 0x81 || value == 0x82)
            return VitalProductDataPageName.OBSOLETE;
        if (value == 0x83)
            return VitalProductDataPageName.DEVICE_IDENTIFICATION;// mandatory
        if (value == 0x84)
            return VitalProductDataPageName.SOFTWARE_INTERFACE_IDENTIFICATION;
        if (value == 0x85)
            return VitalProductDataPageName.MANAGEMENT_NETWORK_ADDRESSES;
        if (value == 0x86)
            return VitalProductDataPageName.EXTENDED_INQUIRY_DATA;
        if (value == 0x87)
            return VitalProductDataPageName.MODE_PAGE_POLICY;
        if (value == 0x88)
            return VitalProductDataPageName.SCSI_PORTS;
        if (0x89 <= value && value <= 0xaf)
            return VitalProductDataPageName.RESERVED;
        if (0xb0 <= value && value <= 0xbf)
            return VitalProductDataPageName.DEVICE_TYPE_SPECIFIC;
        else
            return VitalProductDataPageName.VENDOR_SPECIFIC;
    }

    /**
     * An enumeration of unique identifiers for Vital Product Data Pages.
     * 
     * @author Andreas Ergenzinger
     */
    public enum VitalProductDataPageName {
        /**
         * {@link PageCode} value 0x00
         */
        SUPPORTED_VPD_PAGES, // mandatory
            /**
             * {@link PageCode} values 0x01-0x7f
             */
            ASCII_INFORMATION,
            /**
             * {@link PageCode} value 0x80
             */
            UNIT_SERIAL_NUMBER,
            /**
             * {@link PageCode} values 0x81-0x82
             */
            OBSOLETE,
            /**
             * {@link PageCode} value 0x83
             */
            DEVICE_IDENTIFICATION, // mandatory
            /**
             * {@link PageCode} value 0x84
             */
            SOFTWARE_INTERFACE_IDENTIFICATION,
            /**
             * {@link PageCode} value 0x85
             */
            MANAGEMENT_NETWORK_ADDRESSES,
            /**
             * {@link PageCode} value 0x86
             */
            EXTENDED_INQUIRY_DATA,
            /**
             * {@link PageCode} value 0x87
             */
            MODE_PAGE_POLICY,
            /**
             * {@link PageCode} value 0x88
             */
            SCSI_PORTS,
            /**
             * {@link PageCode} values 0x89-0xaf
             */
            RESERVED,
            /**
             * {@link PageCode} values 0xb0-0xbf
             */
            DEVICE_TYPE_SPECIFIC,
            /**
             * {@link PageCode} values 0xc0-0xff
             */
            VENDOR_SPECIFIC
    }

    @Override
    public String toString() {
        return "0x" + Integer.toHexString(value);
    }
}
