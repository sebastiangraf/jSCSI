package org.jscsi.target.scsi.cdb;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.inquiry.PageCode;
import org.jscsi.target.scsi.inquiry.PageCode.VitalProductDataPageName;
import org.jscsi.target.scsi.inquiry.SupportedVpdPages;
import org.jscsi.target.util.BitManip;
import org.jscsi.target.util.ReadWrite;

/**
 * This class represents Command Descriptor Blocks for the <code>INQUIRY</code> SCSI command.
 * 
 * @author Andreas Ergenzinger
 */
public class InquiryCDB extends CommandDescriptorBlock {

    /**
     * An enable vital product data (EVPD) bit set to one specifies that the
     * device server shall return the vital product data specified by the PAGE
     * CODE field.
     * <p>
     * If the EVPD bit is set to zero, the device server shall return the standard INQUIRY data. If the PAGE
     * CODE field is not set to zero when the EVPD bit is set to zero, the command shall be terminated with
     * CHECK CONDITION status, with the sense key set to ILLEGAL REQUEST, and the additional sense code set to
     * INVALID FIELD IN CDB.
     */
    private final boolean enableVitalProductData;

    /**
     * The ALLOCATION LENGTH field specifies the maximum number of bytes that an
     * application client has allocated in the Data-In Buffer. An allocation
     * length of zero specifies that no data shall be transferred. This
     * condition shall not be considered as an error. The device server shall
     * terminate transfers to the Data-In Buffer when the number of bytes
     * specified by the ALLOCATION LENGTH field have been transferred or when
     * all available data have been transferred, whichever is less.
     * <p>
     * The allocation length is used to limit the maximum amount of variable length data (e.g., mode data, log
     * data, diagnostic data) returned to an application client. If the information being transferred to the
     * Data-In Buffer includes fields containing counts of the number of bytes in some or all of the data,
     * then the contents of these fields shall not be altered to reflect the truncation, if any, that results
     * from an insufficient ALLOCATION LENGTH value, unless the standard that describes the Data-In Buffer
     * format states otherwise.
     * <p>
     * If the amount of information to be transferred exceeds the maximum value that the ALLOCATION LENGTH
     * field is capable of specifying, the device server shall transfer no data and terminate the command with
     * CHECK CONDITION status, with the sense key set to ILLEGAL REQUEST, and the additional sense code set to
     * INVALID FIELD IN CDB.
     * <p>
     * If EVPD is set to zero, the allocation length should be at least five, so that the ADDITIONAL LENGTH
     * field in the parameter data is returned. If EVPD is set to one, the allocation length should be should
     * be at least four, so that the PAGE LENGTH field in the parameter data is returned.
     */
    private final int allocationLength;

    /**
     * When the EVPD bit is set to one, the PAGE CODE field specifies which page
     * of vital product data information the device server shall return.
     */
    private final PageCode pageCode;

    public InquiryCDB(ByteBuffer buffer) {
        super(buffer);

        // EVPD
        enableVitalProductData = BitManip.getBit(buffer.get(1),// byte
            0);// bit number

        // page code
        pageCode = new PageCode(buffer.get(2));

        // allocation length
        allocationLength = ReadWrite.readTwoByteInt(buffer, 3);

        final VitalProductDataPageName vpdpn = pageCode.getVitalProductDataPageName();
        if (enableVitalProductData) {
            if (!SupportedVpdPages.vpdPageCodeSupported(vpdpn))
                addIllegalFieldPointer(2);// page code not supported
        } else {
            /*
             * If the PAGE CODE field is not set to zero when the EVPD bit is
             * set to zero, the command shall be terminated with CHECK CONDITION
             * status, with the sense key set to ILLEGAL REQUEST, and the
             * additional sense code set to INVALID FIELD IN CDB.
             */
            if (vpdpn != VitalProductDataPageName.SUPPORTED_VPD_PAGES)
                addIllegalFieldPointer(2);// value should be 0x00
        }
    }

    public boolean getEnableVitalProductData() {
        return enableVitalProductData;
    }

    public PageCode getPageCode() {
        return pageCode;
    }

    public int getAllocationLength() {
        return allocationLength;
    }
}
