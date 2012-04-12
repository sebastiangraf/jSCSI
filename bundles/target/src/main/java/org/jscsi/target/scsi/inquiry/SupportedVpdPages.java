package org.jscsi.target.scsi.inquiry;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.IResponseData;
import org.jscsi.target.scsi.inquiry.PageCode.VitalProductDataPageName;

/**
 * This Vital Product Data page contains a list of the VPD page codes supported
 * by the logical unit.
 * <p>
 * This class uses the singleton pattern since the list of supported Vital Product Data page requests will
 * never change.
 * 
 * @author Andreas Ergenzinger
 */
public final class SupportedVpdPages implements IResponseData {

    /**
     * The total length of all fields that are not part of the Supported VPD
     * page list.
     */
    private static final int HEADER_SIZE = 4;

    /**
     * The singleton.
     */
    private static SupportedVpdPages instance;

    /*
     * determine which pages to support must be in ascending order see
     * PAGECode.VitalProductDataPageName
     */
    public static final byte[] SUPPORTED_VPD_PAGES = new byte[] {
        (byte)0x00,// SUPPORTED_VPD_PAGES,
                   // mandatory
        (byte)0x83,// DECIVE_IDENTIFICATION, mandatory
    };

    /**
     * Returns the singleton.
     * 
     * @return the singleton
     */
    public static SupportedVpdPages getInstance() {
        if (instance == null)
            instance = new SupportedVpdPages();
        return instance;
    }

    private SupportedVpdPages() {
        // private due to singleton pattern
    }

    public void serialize(ByteBuffer byteBuffer, int index) {

        // *** byte 0 ***
        /*
         * Peripheral Qualifier (bits 7 - 5):
         * 
         * 000b
         * 
         * A peripheral device having the specified peripheral device type is
         * connected to this logical unit. If the device server is unable to
         * determine whether or not a peripheral device is connected, it also
         * shall use this peripheral qualifier. This peripheral qualifier does
         * not mean that the peripheral device connected to the logical unit is
         * ready for access.
         * 
         * 
         * Peripheral Device Type (bits 4 - 0):
         * 
         * 00000b
         * 
         * direct access block device
         */
        byteBuffer.position(index);
        byteBuffer.put((byte)0);

        // *** byte 1 ***
        /*
         * Page Code:
         * 
         * 0x00
         * 
         * supported VPD pages
         */
        byteBuffer.put((byte)0);

        // *** byte 2 ***
        // RESERVED
        byteBuffer.put((byte)0);

        // *** byte 3 ***
        /*
         * Page Length:
         * 
         * n - 3 = 5 - 3 = 2 (for now)
         */
        byteBuffer.put((byte)SUPPORTED_VPD_PAGES.length);

        // *** bytes 4 and 5 - Supported VPD Pages ***
        for (int i = 0; i < SUPPORTED_VPD_PAGES.length; ++i)
            byteBuffer.put(SUPPORTED_VPD_PAGES[i]);
    }

    public int size() {
        return HEADER_SIZE + SUPPORTED_VPD_PAGES.length;
    }

    /**
     * Returns <code>true</code> for those and only for those VPD Page Codes
     * which are supported by the jSCSI Target.
     * 
     * @param vitalProductDataPageName
     *            VPD Page Name whose support is inquired
     * @return <code>true</code> for those and only for those VPD Page Codes
     *         which are supported by the jSCSI Target
     */
    public static boolean vpdPageCodeSupported(final VitalProductDataPageName vitalProductDataPageName) {
        for (int i = 0; i < SUPPORTED_VPD_PAGES.length; ++i) {
            PageCode pageCode = new PageCode(SUPPORTED_VPD_PAGES[i]);
            if (pageCode.getVitalProductDataPageName() == vitalProductDataPageName)
                return true;
        }
        return false;
    }
}
