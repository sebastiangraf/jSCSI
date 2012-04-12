package org.jscsi.target.scsi.modeSense;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.ISerializable;
import org.jscsi.target.util.BitManip;

/**
 * MODE PAGEs are sent in response to successful <code>MODE SENSE</code> SCSI
 * commands. There are two different, command-specific formats for MODE PAGEs -
 * the PAGE_0 FORMAT and the SUB-PAGE FORMAT, represented by the non-abstract
 * children of this class, {@link Page_0FormatModePage} and {@link SubPageFormatModePage}, respectively.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class ModePage implements ISerializable {

    /**
     * This value plus {@link #pageLength} equals the length in bytes of a MODE
     * PAGE using the SUB-PAGE FORMAT.
     */
    private static final int SUB_PAGE_FORMAT_PAGE_LENGTH_MODIFIER = 4;

    /**
     * This value plus {@link #pageLength} equals the length in bytes of a MODE
     * PAGE using the PAGE_0 FORMAT.
     */
    private static final int PAGE_0_FORMAT_PAGE_LENGTH_MODIFIER = 2;

    /**
     * When using the MODE SENSE command, a parameters saveable (PS) bit set to
     * one indicates that the mode page may be saved by the logical unit in a
     * nonvolatile, vendor specific location. A PS bit set to zero indicates
     * that the device server is not able to save the supporte parameters. When
     * using the MODE SELECT command, the PS bit is reserved.
     */
    private final boolean parametersSaveable;

    /**
     * A SubPage Format (SPF) bit set to <code>false</code>/zero indicates that
     * the page_0 mode page format is being used. A SPF bit set to <code>true</code>/one indicates that the
     * sub_page mode page format is
     * being used.
     */
    private final boolean subPageFormat;

    /**
     * Specifies what kind of information is contained in the MODE PAGE.
     */
    private final int pageCode;

    /**
     * The number of bytes following the PAGE LENGTH field.
     */
    protected final int pageLength;

    /**
     * This value plus {@link #pageLength} equals the length in bytes of the
     * MODE PAGE.
     */
    private final int pageLengthModifier;

    /**
     * The abstract constructor.
     * 
     * @param parametersSaveable
     *            value of the PARAMETERS SAVEABLE bit
     * @param subPageFormat
     *            <code>true</code> if and only if the SUB-PAGE FORMAT is to be
     *            used
     * @param pageCode
     *            specifies the kind of information is contained in the MODE
     *            PAGE
     * @param pageLength
     *            the value of the PAGE LENGTH field
     */
    public ModePage(final boolean parametersSaveable, final boolean subPageFormat, final int pageCode,
        final int pageLength) {
        this.parametersSaveable = parametersSaveable;
        this.subPageFormat = subPageFormat;
        if (subPageFormat)
            pageLengthModifier = SUB_PAGE_FORMAT_PAGE_LENGTH_MODIFIER;
        else
            pageLengthModifier = PAGE_0_FORMAT_PAGE_LENGTH_MODIFIER;
        this.pageCode = pageCode;
        this.pageLength = pageLength;
    }

    public final void serialize(final ByteBuffer byteBuffer, final int index) {
        // serialize first byte
        // (for convenience reasons from least significant to most significant
        // field)
        byte b = (byte)pageCode;// PAGE CODE
        b = BitManip.getByteWithBitSet(b, 6, subPageFormat);// SPF
        b = BitManip.getByteWithBitSet(b, 7, parametersSaveable);// PS
        byteBuffer.position(index);
        byteBuffer.put(b);
        // serialize remaining fields
        serializeSubPageCode(byteBuffer, index);
        serializePageLength(byteBuffer, index);
        serializeModeParameters(byteBuffer, index);
    }

    /**
     * This method serializes the SUBPAGE CODE field, if the mode page format
     * says this field exists (only for the sub_page mode page format).
     * 
     * @param buffer
     *            where to insert the serialized object representation
     * @param index
     *            the position of the first byte of the serialized object in the {@link ByteBuffer}
     */
    protected abstract void serializeSubPageCode(final ByteBuffer buffer, final int index);

    /**
     * Serializes the PAGE LENGTH field.
     * 
     * @param buffer
     *            where to insert the serialized object representation
     * @param index
     *            the position of the first byte of the serialized object in the {@link ByteBuffer}
     */
    protected abstract void serializePageLength(final ByteBuffer buffer, final int index);

    /**
     * @param buffer
     *            where to insert the serialized object representation
     * @param index
     *            the position of the first byte of the serialized object in the {@link ByteBuffer}
     */
    protected abstract void serializeModeParameters(final ByteBuffer buffer, final int index);

    public final int size() {
        return pageLength + pageLengthModifier;
    }
}
