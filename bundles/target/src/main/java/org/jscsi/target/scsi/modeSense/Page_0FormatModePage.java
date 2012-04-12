package org.jscsi.target.scsi.modeSense;

import java.nio.ByteBuffer;

/**
 * Instances of this class represent MODE PAGEs using the PAGE_0 format.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class Page_0FormatModePage extends ModePage {

    /**
     * The index of the PAGE LENGTH field in the serialized representation of
     * MODE PAGEs using the PAGE_0 format.
     */
    private static final int PAGE_LENGTH_INDEX = 1;

    /**
     * The abstract constructor.
     * 
     * @param parametersSaveable
     *            the value of the PARAMETERS SAVEABLE bit
     * @param pageCode
     *            determines the kind of information contained in the MODE PAGE
     * @param pageLength
     *            the value of the PAGE LENGTH field
     */
    public Page_0FormatModePage(boolean parametersSaveable, int pageCode, int pageLength) {
        super(parametersSaveable, false,// subPageFormat
            pageCode, pageLength);
    }

    @Override
    protected final void serializePageLength(ByteBuffer buffer, int index) {
        buffer.position(index + PAGE_LENGTH_INDEX);
        buffer.put((byte)pageLength);
    }

    @Override
    protected final void serializeSubPageCode(ByteBuffer buffer, int index) {
        /*
         * Do nothing. This method is only relevant in SubPageFormatModePage
         * subclasses. Is only mentioned here to prevent overwriting.
         */
    }

}
