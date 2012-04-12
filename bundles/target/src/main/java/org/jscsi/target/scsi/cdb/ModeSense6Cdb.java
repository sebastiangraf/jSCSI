package org.jscsi.target.scsi.cdb;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.modeSense.ModePageCode;
import org.jscsi.target.scsi.modeSense.PageControl;
import org.jscsi.target.util.BitManip;
import org.jscsi.target.util.ReadWrite;

/**
 * This class represents Command Descriptor Blocks for the <code>MODE SENSE (6)</code> SCSI command.
 * 
 * @author Andreas Ergenzinger
 */
public final class ModeSense6Cdb extends CommandDescriptorBlock {

    /**
     * A disable block descriptors (DBD) bit set to zero specifies that the
     * device server may return zero or more block descriptors in the returned
     * MODE SENSE data (see 7.4). A DBD bit set to one specifies that the device
     * server shall not return any block descriptors in the returned MODE SENSE
     * data.
     */
    private final boolean disableBlockDescriptors;

    /**
     * The page control (PC) field specifies the type of mode parameter values
     * to be returned in the mode pages.
     */
    private final PageControl pageControl;

    /**
     * The PAGE CODE and SUBPAGE CODE fields specify which mode pages and
     * subpages to return.
     * 
     * @see #subpageCode
     */
    private final int pageCode;

    /**
     * The PAGE CODE and SUBPAGE CODE fields specify which mode pages and
     * subpages to return
     * 
     * @see #pageCode
     */
    private final int subpageCode;

    /**
     * The ALLOCATION LENGTH field specifies the maximum number of bytes that an
     * application client has allocated in the Data-In Buffer. An allocation
     * length of zero specifies that no data shall be transferred.
     */
    private final int allocationLength;

    public ModeSense6Cdb(ByteBuffer buffer) {
        super(buffer);// SCSI Operation Code + Control

        // DBD
        disableBlockDescriptors = BitManip.getBit(buffer.get(1),// byte
            3);// bit number

        // PC
        int i = (buffer.get(2) >> 6) & 3;
        pageControl = PageControl.getPageControl(i);

        // PAGE CODE
        pageCode = buffer.get(2) & 63;

        // SUBPAGE CODE
        subpageCode = ReadWrite.readOneByteInt(buffer, 3);

        // ALLOCATION LENGTH
        allocationLength = ReadWrite.readOneByteInt(buffer, 4);
    }

    public boolean getDisableBlockDescriptors() {
        return disableBlockDescriptors;
    }

    public PageControl getPageControl() {
        return pageControl;
    }

    public int getPageCode() {
        return pageCode;
    }

    public int getSubpageCode() {
        return subpageCode;
    }

    public int getAllocationLength() {
        return allocationLength;
    }

    public ModePageCode getModePage() {
        return ModePageCode.getModePage(pageCode, subpageCode);
    }
}
