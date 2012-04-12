package org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific;

import java.nio.ByteBuffer;

import org.jscsi.target.util.ReadWrite;

/**
 * Progress indication sense-key-specific data is used to communicate the
 * progress of a previously issued SCSI command (e.g. <code>FORMAT UNIT</code>).
 * 
 * @author Andreas Ergenzinger
 */
public final class ProgressIndicationSenseKeySpecificData extends SenseKeySpecificData {

    /**
     * The byte position of the PROGRESS INDICATION field.
     */
    private static final int PROGRESS_INDICATION_FIELD_INDEX = 1;

    /**
     * The PROGRESS INDICATION field is a percent complete indication in which
     * the returned value is a numerator that has 65 536 (10000h) as its
     * denominator. The progress indication shall be based upon the total
     * operation.
     * <p>
     * The progress indication should be time related, however this is not an absolute requirement. (E.g.,
     * since format time varies with the number of defects encountered, etc., it is reasonable for the device
     * server to assign values to various steps within the process. The granularity of these steps should be
     * small enough to provide reasonable assurances to the application client that progress is being made.)
     */
    private final short progressIndication;

    /**
     * The consctructor.
     * 
     * @param senseKeySpecificDataValid
     *            <code>true</code> if and only if the <i>progressIndication</i>
     *            parameter is to be considered valid
     * @param progressIndication
     *            specifies the progress, the integer must lie in the interval
     *            [0, 65536]
     */
    public ProgressIndicationSenseKeySpecificData(final boolean senseKeySpecificDataValid,
        final int progressIndication) {
        super(senseKeySpecificDataValid);
        this.progressIndication = (short)progressIndication;
    }

    @Override
    protected void serializeSpecificFields(ByteBuffer byteBuffer, int index) {

        ReadWrite.writeTwoByteInt(byteBuffer, progressIndication, index + PROGRESS_INDICATION_FIELD_INDEX);
    }
}
