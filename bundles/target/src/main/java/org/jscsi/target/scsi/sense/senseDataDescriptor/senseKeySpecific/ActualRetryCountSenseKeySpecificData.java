package org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific;

import java.nio.ByteBuffer;

import org.jscsi.target.util.ReadWrite;

/**
 * Actual retry count sense-key-specific data is used to communicate the number
 * of retries of the recovery algorithm used in attempting to recover an error
 * or exception condition.
 * 
 * @author Andreas Ergenzinger
 */
public final class ActualRetryCountSenseKeySpecificData extends SenseKeySpecificData {

    /**
     * The byte position of the ACTUAL RETRY COUNT field.
     */
    private static final int ACTUAL_RETRY_COUNT_FIELD_INDEX = 1;

    /**
     * The ACTUAL RETRY COUNT field returns vendor specific information on the
     * number of retries of the recovery algorithm used in attempting to recover
     * an error or exception condition.
     * <p>
     * This field should be computed in the same way as the retry count fields within the Read-Write Error
     * Recovery mode page.
     */
    private final short actualRetryCount;

    /**
     * The constructor.
     * 
     * @param senseKeySpecificDataValid
     *            <code>true</code> if and only if the second
     *            <i>actualRetryCount</i> parameter is valid
     * @param actualRetryCount
     *            the number of performed recovery attempts
     */
    public ActualRetryCountSenseKeySpecificData(final boolean senseKeySpecificDataValid,
        final int actualRetryCount) {
        super(senseKeySpecificDataValid);
        this.actualRetryCount = (short)actualRetryCount;
    }

    @Override
    protected void serializeSpecificFields(final ByteBuffer byteBuffer, final int index) {
        ReadWrite.writeTwoByteInt(byteBuffer, actualRetryCount, index + ACTUAL_RETRY_COUNT_FIELD_INDEX);
    }

}
