package org.jscsi.target.scsi.modeSense;

import java.nio.ByteBuffer;

import org.jscsi.target.util.ReadWrite;

/**
 * Instances of this class represent MODE PAGEs using the SUB-PAGE format.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class SubPageFormatModePage extends ModePage {

    /**
     * The index of the SUB-PAGE CODE field in the serialized representation of
     * a MODE PAGE using the SUB-PAGE format.
     */
    private static final int SUB_PAGE_CODE_INDEX = 1;

    /**
     * The index of the PAGE LENGTH field in the serialized representation of a
     * MODE PAGE using the SUB-PAGE format.
     */
    private static final int PAGE_LENGTH_INDEX = 2;

    /**
     * Together with {@link ModePage#pageCode} this value determines the kind of
     * information contained in the MODE PAGE.
     */
    private final int subPageCode;

    /**
     * The abstract constructor.
     * 
     * @param parametersSaveable
     *            value of the PARAMETERS SAVEABLE bit
     * @param pageCode
     *            general description of the contained information
     * @param subPageCode
     *            more specific description of the contained information
     * @param pageLength
     *            the value of the PAGE LENGTH field
     */
    public SubPageFormatModePage(boolean parametersSaveable, int pageCode, final int subPageCode,
        int pageLength) {
        super(parametersSaveable, true,// subPageFormat
            pageCode, pageLength);
        this.subPageCode = subPageCode;
    }

    @Override
    protected final void serializeSubPageCode(final ByteBuffer buffer, final int index) {
        buffer.position(index + SUB_PAGE_CODE_INDEX);
        buffer.put((byte)subPageCode);
    }

    @Override
    protected final void serializePageLength(ByteBuffer buffer, int index) {
        buffer.position(index + PAGE_LENGTH_INDEX);
        ReadWrite.writeTwoByteInt(buffer,// buffer
            pageLength,// value
            index + PAGE_LENGTH_INDEX);// index
    }

}
