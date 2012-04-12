package org.jscsi.target.scsi.cdb;

/**
 * The SELF-TEST CODE field is a member of SEND DIAGNOSTIC CDBs.
 * <p>
 * A {@link SendDiagnosticCdb#selfTest} bit set to zero specifies that the device server shall perform the
 * diagnostic operation specified by the {@link SendDiagnosticCdb#selfTestCode} field.
 * <p>
 * The {@link SelfTestCode} field has a length of three bits.
 * 
 * @see SendDiagnosticCdb
 * @author Andreas Ergenzinger
 */
public enum SelfTestCode {
    /**
     * This value shall be used when the {@link SendDiagnosticCdb#selfTest} bit
     * is set to one, or when the SELFTEST bit is set to zero and the PF bit is
     * set to one.
     */
    ALL_ZEROS((byte)0),
    /**
     * The device server shall start its short self-test in the background mode.
     * The {@link SendDiagnosticCdb#parameterListLength} field shall contain
     * zero.
     */
    BACKGROUND_SHORT_SELF_TEST((byte)1),
    /**
     * The device server shall start its extended self-test (see 5.5.2) in the
     * background mode (see 5.5.3.3). The {@link SendDiagnosticCdb#parameterListLength} field shall contain
     * zero.
     */
    BACKGROUND_EXTENDED_SELF_TEST((byte)2),
    /**
     * The device server shall abort the current self-test running in background
     * mode. The {@link SendDiagnosticCdb#parameterListLength} field shall
     * contain zero. This value is only valid if a previous SEND DIAGNOSTIC
     * command specified a background self-test function and that self-test has
     * not completed. If either of these conditions is not met, the command
     * shall be terminated with CHECK CONDITION status, with the sense key set
     * to ILLEGAL REQUEST, and the additional sense code set to INVALID FIELD IN
     * CDB.
     */
    ABORT_BACKGROUND_SELF_TEST((byte)4),
    /**
     * The device server shall start its short self-test (see 5.5.2) in the
     * foreground mode. The {@link SendDiagnosticCdb#parameterListLength} field
     * shall contain zero.
     */
    FOREGROUND_SELF_TEST((byte)5),
    /**
     * The device server shall start its extended self-test in the foreground
     * mode. The {@link SendDiagnosticCdb#parameterListLength} field shall
     * contain zero.
     */
    FOREGROUND_EXTENDED_SELF_TEST((byte)6);
    // all other values (011b and 111b) are reserved

    /**
     * The serialized value of this object.
     */
    private final byte value;

    private SelfTestCode(final byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    /**
     * Returns the {@link SelfTestCode} corresponding to the passed value.
     * 
     * @param value
     *            the value of a SELF-TEST CODE field
     * @return the {@link SelfTestCode} corresponding to the passed value or <code>null</code> if none exists
     */
    public static SelfTestCode getValue(int value) {
        SelfTestCode[] values = values();
        for (int i = 0; i < values.length; ++i)
            if (value == values[i].getValue())
                return values[i];
        return null;
    }
}
