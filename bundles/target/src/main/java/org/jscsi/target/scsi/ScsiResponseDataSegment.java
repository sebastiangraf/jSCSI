package org.jscsi.target.scsi;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jscsi.target.connection.TargetPduFactory;
import org.jscsi.target.scsi.sense.SenseData;
import org.jscsi.target.util.Debug;
import org.jscsi.target.util.ReadWrite;

/**
 * Instances of this class represent data that may be sent in a SCSI Command
 * Response PDU.
 * <p>
 * It may contain either {@link SenseData}, {@link IResponseData}, or (rarely)
 * both. Some SCSI commands require that no response data is sent (i.e. data
 * segment length = 0) - in these cases the {@link #EMPTY_DATA_SEGMENT} shall be
 * used.
 * <p>
 * Since the target must honor the buffer size the initiator has allocated for
 * data returned in the data segment, the {@link #serialize()} method will only
 * write as many bytes to the passed {@link ByteBuffer} as specified during
 * initialization.
 * 
 * @see TargetPduFactory
 * @author Andreas Ergenzinger
 */
public final class ScsiResponseDataSegment {

    private static final Logger LOGGER = Logger
            .getLogger(ScsiResponseDataSegment.class);

    /**
     * A {@link ScsiResponseDataSegment} of length zero.
     */
    public static final ScsiResponseDataSegment EMPTY_DATA_SEGMENT = new ScsiResponseDataSegment();

    /**
     * The length in bytes of the SENSE LENGTH field.
     */
    private static final int SENSE_LENGTH_FIELD_LENGTH = 2;

    /**
     * The contained {@link SenseData}.
     */
    private final SenseData senseData;

    /**
     * The contained {@link IResponseData}.
     */
    private final IResponseData responseData;

    /**
     * The number of bytes the initiator has allocated for data sent in the data
     * segment, i.e. the maximum number of bytes of this object that may be
     * transmitted.
     */
    private final int allocationLength;

    /**
     * The length of the {@link ScsiResponseDataSegment}, without considering
     * any limitations due to {@link #allocationLength}, or <code>-1</code>.
     * 
     * @see #uncroppedSize()
     */
    private int uncroppedSize = -1;

    /**
     * Constructor for creating an empty {@link ScsiResponseDataSegment}.
     * 
     * @see ScsiResponseDataSegment#EMPTY_DATA_SEGMENT
     */
    private ScsiResponseDataSegment() {
        this(null, null, 0);
    }

    /**
     * Creates a {@link ScsiResponseDataSegment} with {@link SenseData}.
     * 
     * @param senseData
     *            the sense data sent as the result of SCSI error
     * @param allocationLength
     *            number of bytes the initiator has allocated for data sent in
     *            the data segment
     */
    public ScsiResponseDataSegment(final SenseData senseData,
            final int allocationLength) {
        this(senseData, null, allocationLength);
    }

    /**
     * Creates a {@link ScsiResponseDataSegment} with {@link IResponseData}.
     * 
     * @param responseData
     *            the data requested by the initiator
     * @param allocationLength
     *            number of bytes the initiator has allocated for data sent in
     *            the data segment
     */
    public ScsiResponseDataSegment(final IResponseData responseData,
            final int allocationLength) {
        this(null, responseData, allocationLength);
    }

    /**
     * Creates a {@link ScsiResponseDataSegment} with both {@link SenseData} and
     * {@link IResponseData}.
     * 
     * @param senseData
     *            senseData the sense data sent as the result of SCSI error
     * @param responseData
     *            data requested by the initiator
     * @param allocationLength
     *            number of bytes the initiator has allocated for data sent in
     *            the data segment
     */
    private ScsiResponseDataSegment(final SenseData senseData,
            final IResponseData responseData, final int allocationLength) {
        this.senseData = senseData;
        this.responseData = responseData;
        this.allocationLength = allocationLength;
    }

    /**
     * Returns a {@link ByteBuffer} containing a serialized representation of
     * this object.
     * 
     * @return a {@link ByteBuffer} containing a serialized representation of
     *         this object
     */
    public ByteBuffer serialize() {

        final int size = uncroppedSize();
        if (size == 0)
            return ByteBuffer.allocate(0);// empty data segment

        // calculate sense length
        int senseLength;
        if (senseData == null)
            senseLength = 0;
        else
            senseLength = senseData.size();

        // allocate buffer of appropriate size
        final ByteBuffer buffer = ByteBuffer.allocate(size);

        // write sense length field
        ReadWrite.writeTwoByteInt(buffer,// buffer
                senseLength,// value
                0);// index

        // write sense data
        if (senseData != null)
            senseData.serialize(buffer, SENSE_LENGTH_FIELD_LENGTH);

        // write and SCSI response data
        if (responseData != null)
            responseData.serialize(buffer, SENSE_LENGTH_FIELD_LENGTH
                    + senseLength);

        if (allocationLength > 0 && buffer.capacity() > allocationLength) {
            /*
             * If the allocation length variable is valid and the response data
             * segment is larger than the data-in buffer allocated by the
             * initiator, limit the number of bytes returned.
             */
            buffer.position(0);
            buffer.limit(allocationLength);
            final ByteBuffer croppedBuffer = ByteBuffer
                    .allocate(allocationLength);
            croppedBuffer.put(buffer);
            return croppedBuffer;
        }

        if (LOGGER.isDebugEnabled())
            LOGGER.debug("SCSI Response Data Segment:\n"
                    + Debug.byteBufferToString(buffer));

        return buffer;
    }

    /**
     * The length of the {@link ScsiResponseDataSegment}, without considering
     * any limitations due to {@link #allocationLength}.
     * <p>
     * The returned value is calculated just once, and then stored in
     * {@link #uncroppedSize}. This summation is performed only if
     * {@link #uncroppedSize} is negative.
     * 
     * @return length of the {@link ScsiResponseDataSegment}, without
     *         considering any limitations due to {@link #allocationLength}
     */
    public int uncroppedSize() {
        if (uncroppedSize < 0) {
            if (senseData == null && responseData == null)
                return 0;
            int size = SENSE_LENGTH_FIELD_LENGTH;
            if (senseData != null)
                size += senseData.size();
            if (responseData != null)
                size += responseData.size();
            uncroppedSize = size;
        }
        return uncroppedSize;
    }

    /**
     * Indicates if any bytes have to be cropped during {@link #serialize()}
     * calls. The method returns <code>true</code> if and only if the serialized
     * length of this objects exceeds {@link #allocationLength}.
     * 
     * @return <code>true</code> if and only if the serialized length of this
     *         objects exceeds {@link #allocationLength}
     */
    public boolean getResidualOverflow() {
        if (uncroppedSize() > allocationLength)
            return true;
        return false;
    }

    /**
     * Returns the number of bytes that had to be cropped due to the total
     * length of all contained fields exceeding the {@link #allocationLength}.
     * 
     * @return the total number of bytes that have to be cropped during
     *         serialization
     * @see #serialize()
     */
    public int getResidualCount() {
        if (getResidualOverflow())
            return uncroppedSize() - allocationLength;
        return 0;
    }
}
