package org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.ISerializable;
import org.jscsi.target.scsi.sense.SenseData;
import org.jscsi.target.scsi.sense.SenseKey;
import org.jscsi.target.util.BitManip;

/**
 * SENSE-KEY-SPECIFIC DATA further defines the reason for a CHECK CONDITION SCSI
 * response status.
 * <p>
 * The definition of the SENSE KEY SPECIFIC field is determined by the value of the enclosing sense data's
 * {@link SenseData#senseKey} field.
 * 
 * <table border="1">
 * <tr>
 * <th>Sense Key</th>
 * <th>Sense Key Specific Field Definition</th>
 * </tr>
 * <tr>
 * <td>{@link SenseKey#ILLEGAL_REQUEST}</td>
 * <td>Field Pointer (see {@link FieldPointerSenseKeySpecificData})</td>
 * </tr>
 * <tr>
 * <td>{@link SenseKey#HARDWARE_ERROR},<br>
 * {@link SenseKey#MEDIUM_ERROR}, or<br>
 * {@link SenseKey#RECOVERED_ERROR}</td>
 * <td>Actual Retry Count (see {@link ActualRetryCountSenseKeySpecificData})</td>
 * </tr>
 * <tr>
 * <td>{@link SenseKey#NO_SENSE} or<br>
 * {@link SenseKey#NOT_READY}</td>
 * <td>Progress Indication (see {@link ProgressIndicationSenseKeySpecificData})</td>
 * </tr>
 * <tr>
 * <td>{@link SenseKey#COPY_ABORTED}</td>
 * <td>Segment Pointer (see {@link SegmentPointerSenseKeySpecificData})</td>
 * </tr>
 * <tr>
 * <td>All other Sense Keys</td>
 * <td>The sense key specific sense data descriptor shall not<br>
 * appear in the descriptor format sense data and the<br>
 * SKSV (Sense Key Specific Field Valid) bit shall be set to<br>
 * zero in the fixed format sense data.</td>
 * </tr>
 * </table>
 * 
 * @author Andreas Ergenzinger
 */
public abstract class SenseKeySpecificData implements ISerializable {

    /**
     * The serialized length in bytes of SENSE-KEY-SPECIFIC DATA.
     */
    public static final int SIZE = 3;

    /**
     * <code>true</code> if and only if the information fields of this data
     * object are valid.
     */
    protected final boolean senseKeySpecificDataValid;

    /**
     * The absctract constructor.
     * 
     * @param senseKeySpecificDataValid
     *            <code>true</code> if and only if the information fields of
     *            this data object are valid
     */
    public SenseKeySpecificData(final boolean senseKeySpecificDataValid) {
        this.senseKeySpecificDataValid = senseKeySpecificDataValid;
    }

    /**
     * Serializes the fields common to all sense-key-specific data.
     * 
     * @param byteBuffer
     *            where the serialized fields will be stored
     * @param index
     *            the position of the first byte of the sense data descriptor in
     *            the {@link ByteBuffer}
     */
    private final void serializeCommonFields(final ByteBuffer byteBuffer, final int index) {
        byteBuffer.position(index);
        byte b = 0;
        if (senseKeySpecificDataValid)
            b = BitManip.getByteWithBitSet(b, 7, true);// set MSB to 1
        byteBuffer.put(b);
    }

    /**
     * Serializes all fields which are not common to all sense-key-specific
     * data, which means those that are sub-type-specific.
     * 
     * @param byteBuffer
     *            where the serialized fields will be stored
     * @param index
     *            the position of the first byte of the sense data descriptor in
     *            the {@link ByteBuffer}
     */
    protected abstract void serializeSpecificFields(ByteBuffer byteBuffer, final int index);

    public void serialize(ByteBuffer byteBuffer, int index) {
        serializeCommonFields(byteBuffer, index);
        serializeSpecificFields(byteBuffer, index);
    }

    public int size() {
        return SIZE;
    }
}
