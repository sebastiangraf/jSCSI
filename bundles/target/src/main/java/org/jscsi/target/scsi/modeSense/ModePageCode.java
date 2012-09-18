package org.jscsi.target.scsi.modeSense;

import java.util.HashMap;
import java.util.Map;

/**
 * Mode Pages for direct-access block devices, as defined in SBC-3.
 * <p>
 * Instances of this enumeration are used to identify a requested or a returned {@link ModePage}.
 * 
 * @author Andreas Ergenzinger
 */
public enum ModePageCode {
        APPLICATION_TAG_MODE_PAGE(0x0a, 0x02),
        BACKGROUND_CONTROL_MODE_PAGE(0x1c, 0x01),
        CACHING_MODE_PAGE(0x08, 0x00),
        CONTROL_EXTENSION_MODE_PAGE(0x0a, 0x01),
        CONTROL_MODE_PAGE(0x0a, 0x00),
        DISCONNECT_RECONNECT_MODE_PAGE(0x02, 0x00),
        ENCLOSURE_SERVICES_MANAGEMENT_MODE_PAGE(0x14, 0x00), // do
                                                             // not
                                                             // support,
                                                             // since
                                                             // ENCSERV
                                                             // bit
                                                             // in
                                                             // Inquiry
                                                             // data
                                                             // is
                                                             // 0
        INFORMATIONAL_EXCEPTIONS_CONTROL_MODE_PAGE(0x1c, 0x00),
        LOGICAL_BLOCK_PROVISIONING_MODE_PAGE(0x1c, 0x02),
        POWER_CONDITION_MODE_PAGE(0x1a, 0x00),
        PROTOCOL_SPECIFIC_LUN_MODE_PAGE(0x18, 0x00),
        PROTOCOL_SPECIFIC_PORT_MODE_PAGE(0x19, 0x00),
        READ_WRITE_ERROR_RECOVERY_MODE_PAGE(0x01, 0x00),
        RETURN_ALL_MODE_PAGES_AND_SUBPAGES(0x3f, 0xff),
        RETURN_ALL_MODE_PAGES_ONLY(0x3f, 0x00),
        VERIFY_ERROR_RECOVERY_MODE_PAGE(0x07, 0x00),
        XOR_CONTROL_MODE_PAGE(0x10, 0x00),
        // for returning subpages of the above mode pages (subpageCode = 0xff)
        APPLICATION_TAG_MODE_PAGE_SUBPAGES(0x0a, 0xff),
        BACKGROUND_CONTROL_MODE_PAGE_SUBPAGES(0x1c, 0xff),
        CACHING_MODE_PAGE_SUBPAGES(0x08, 0xff),
        CONTROL_EXTENSION_MODE_PAGE_SUBPAGES(0x0a, 0xff),
        CONTROL_MODE_PAGE_SUBPAGES(0x0a, 0xff),
        DISCONNECT_RECONNECT_MODE_PAGE_SUBPAGES(0x02, 0xff),
        ENCLOSURE_SERVICES_MANAGEMENT_MODE_PAGE_SUBPAGES(0x14, 0xff), // do not support, since ENCSERV bit in
                                                                      // Inquiry data is 0
        INFORMATIONAL_EXCEPTIONS_CONTROL_MODE_PAGE_SUBPAGES(0x1c, 0xff),
        LOGICAL_BLOCK_PROVISIONING_MODE_PAGE_SUBPAGES(0x1c, 0xff), POWER_CONDITION_MODE_PAGE_SUBPAGES(0x1a,
            0xff), PROTOCOL_SPECIFIC_LUN_MODE_PAGE_SUBPAGES(0x18, 0xff),
        PROTOCOL_SPECIFIC_PORT_MODE_PAGE_SUBPAGES(0x19, 0xff), READ_WRITE_ERROR_RECOVERY_MODE_PAGE_SUBPAGES(
            0x01, 0xff), VERIFY_ERROR_RECOVERY_MODE_PAGE_SUBPAGES(0x07, 0xff),
        XOR_CONTROL_MODE_PAGE_SUBPAGES(0x10, 0xff);

    /**
     * Allows the retrieval of {@link ModePageCode} instances based on merged
     * PAGE CODE and SUBPAGE CODE field values.
     * 
     * @see #joinCodes(int, int)
     */
    private static Map<Integer, ModePageCode> map = new HashMap<Integer, ModePageCode>();
    static {// initialize map
        final ModePageCode[] modePages = values();
        for (ModePageCode mp : modePages)
            map.put(joinCodes(mp.pageCode, mp.subpageCode),// key
                mp);// value
    }

    /**
     * Returns the {@link ModePageCode} instance identified by the passed
     * parameters.
     * 
     * @param pageCode
     *            determines the general category of a {@link ModePage}
     * @param subpageCode
     *            determines the sub-category of a {@link ModePage}
     * @return the corresponding {@link ModePageCode} instance of <code>
     * null</code> if there is no corresponding object
     */
    public static ModePageCode getModePage(final int pageCode, final int subpageCode) {
        return map.get(joinCodes(pageCode, subpageCode));
    }

    /**
     * Determines the general category of the MODE PAGE.
     */
    private final int pageCode;

    /**
     * Determines the sub-category of the MODE PAGE, if applicable.
     */
    private final int subpageCode;

    /**
     * The constructor.
     * 
     * @param pageCode
     *            determines the general category of a {@link ModePage}
     * @param subpageCode
     *            determines the sub-category of a {@link ModePage}
     */
    private ModePageCode(final int pageCode, final int subpageCode) {
        this.pageCode = pageCode;
        this.subpageCode = subpageCode;
    }

    /**
     * Merges the the passed PAGE CODE and SUBPAGE CODE field values to a single
     * value
     * 
     * @param pageCode
     *            the value of a PAGE CODE field
     * @param subpageCode
     *            the value of a SUBPAGE CODE field
     * @return a merged value containing the relevant bits from both parameters
     */
    private static int joinCodes(final int pageCode, final int subpageCode) {
        return (pageCode << 8) | subpageCode;
    }
}
