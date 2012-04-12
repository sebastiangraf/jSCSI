package org.jscsi.target.scsi.sense;

import org.jscsi.target.scsi.ISerializable;

/**
 * Sense data shall be returned in the same I_T_L_Q nexus transaction as a CHECK
 * CONDITION status and as parameter data in response to the REQUEST SENSE
 * command.
 * <p>
 * Sense data returned in the same I_T_L_Q nexus transaction as a CHECK CONDITION status shall be either fixed
 * or descriptor format sense data format based on the value of the D_SENSE bit in the Control mode page.
 * <p>
 * The REQUEST SENSE command may be used to request either the fixed format sense data or the descriptor
 * format sense data.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class SenseData implements ISerializable {

    /**
     * The first byte of all sense data contains the RESPONSE CODE field that
     * indicates the error type and format of the sense data.
     * 
     * @see #errorType
     * @see #senseDataFormat
     */
    protected final int responseCode;

    /**
     * The error type of the sense data as determined by the {@link #responseCode} variable.
     */
    protected final ErrorType errorType;

    /**
     * The format of the sense data as determined by the {@link #responseCode} variable.
     */
    protected final SenseDataFormat senseDataFormat;

    /**
     * The {@link #senseKey} and {@link #additionalSenseCodeAndQualifier} fields
     * provide a hierarchy of information. The hierarchy provides a top-down
     * approach for an application client to determine information relating to
     * the error and exception conditions.
     */
    protected final SenseKey senseKey;

    /**
     * The {@link #senseKey} and {@link #additionalSenseCodeAndQualifier} fields
     * provide a hierarchy of information. The hierarchy provides a top-down
     * approach for an application client to determine information relating to
     * the error and exception conditions.
     */
    protected final AdditionalSenseCodeAndQualifier additionalSenseCodeAndQualifier;

    /**
     * The sense data constructor.
     * 
     * @param errorType
     *            the error type of the sense data
     * @param senseDataFormat
     *            the format of the sense data
     * @param senseKey
     *            describes the general category of the error requiring the
     *            sending of sense data
     * @param additionalSenseCodeAndQualifier
     *            a more specific description of the error
     */
    public SenseData(final ErrorType errorType, final SenseDataFormat senseDataFormat,
        final SenseKey senseKey, final AdditionalSenseCodeAndQualifier additionalSenseCodeAndQualifier) {
        this.errorType = errorType;
        this.senseDataFormat = senseDataFormat;
        responseCode = getReponseCodeFor(errorType, senseDataFormat);
        this.senseKey = senseKey;
        this.additionalSenseCodeAndQualifier = additionalSenseCodeAndQualifier;
    }

    /**
     * Returns the proper response code for the given error type and sense data
     * format.
     * 
     * @param errorType
     *            a sense data error type
     * @param senseDataFormat
     *            a sense data format
     * @return the proper response code
     */
    public static final int
        getReponseCodeFor(final ErrorType errorType, final SenseDataFormat senseDataFormat) {
        if (senseDataFormat == SenseDataFormat.FIXED) {
            if (errorType == ErrorType.CURRENT)
                return 0x70;
            else
                // errorType == DEFERRED
                return 0x71;
        } else {// senseDataFormat == DESCRIPTOR
            if (errorType == ErrorType.CURRENT)
                return 0x72;
            else
                // errorType == DEFERRED
                return 0x73;
        }
        /*
         * Response codes 0x74 to 0x7E are reserved. Response code 0x7f is
         * vendor specific.
         */
    }
}
