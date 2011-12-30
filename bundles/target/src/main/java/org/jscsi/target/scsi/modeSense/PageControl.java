package org.jscsi.target.scsi.modeSense;

import org.jscsi.target.scsi.cdb.ModeSense6Cdb;

/**
 * The page control (PC) field specifies the type of mode parameter values to be
 * returned in the mode pages.
 * 
 * @see ModeSense6Cdb
 * @author Andreas Ergenzinger
 */
public enum PageControl {
    /**
     * The currently valid values shall be returned.
     */
    CURRENT_VALUES(0x00),
    /**
     * Only changeable values shall be returned.
     */
    CHANGEABLE_VALUES(0x01),
    /**
     * The default values shall be returned.
     */
    DEFAULT_VALUES(0x02),
    /**
     * The last values that have been saved persistently shall be returned.
     */
    SAVED_VALUES(0x03);

    private final int value;

    private PageControl(final int value) {
        this.value = value;
    }

    public static PageControl getPageControl(final int value) {
        final PageControl[] vals = values();
        for (PageControl p : vals)
            if (p.value == value)
                return p;
        return null;
    }
}
