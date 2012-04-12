package org.jscsi.target.scsi.cdb;

import java.nio.ByteBuffer;

import org.jscsi.target.util.BitManip;
import org.jscsi.target.util.ReadWrite;

/**
 * This class represents Command Descriptor Blocks for the <code>SEND DIAGNOSTIC</code> SCSI command.
 * 
 * @author Andreas Ergenzinger
 */
public final class SendDiagnosticCdb extends CommandDescriptorBlock {

    /**
     * If the {@link #selfTest} bit is set to one, the {@link #selfTestCode} field shall contain 000b (
     * {@link SelfTestCode#ALL_ZEROS}. If the SELFTEST
     * bit is set to zero, the contents of SELF-TEST CODE specifies which
     * diagnostic operation the device server shall perform.
     * 
     * @see SelfTestCode
     */
    private final SelfTestCode selfTestCode;

    /**
     * A self-test (SELFTEST) bit set to one specifies that the device server
     * shall perform the logical unit default self-test.
     * <p>
     * A SELFTEST bit set to zero specifies that the device server shall perform the diagnostic operation
     * specified by the {@link #selfTestCode} field or in the parameter list.
     * <p>
     * Only support for the default self-test feature, as required by SPC-3, is implemented. Request for other
     * types of self-test operations will be declined.
     */
    private final boolean selfTest;

    /**
     * A page format (PF) bit set to one specifies that the SEND DIAGNOSTIC
     * parameters and any parameters returned by a following RECEIVE DIAGNOSTIC
     * RESULTS command with the PCV bit set to zero shall contain a single
     * diagnostic page.
     * <p>
     * A PF bit set to zero specifies that all SEND DIAGNOSTIC parameters are vendor specific. If the
     * PARAMETER LIST LENGTH field is set to zero and the SEND DIAGNOSTIC command is not going to be followed
     * by a corresponding RECEIVE DIAGNOSTIC RESULTS command with the PCV bit set to zero, then the
     * application client shall set the PF bit to zero.
     * <p>
     * The implementation of the PF bit is optional and therefore not supported by the jSCSI Target.
     */
    private final boolean pageFormat;

    /**
     * A unit offline (UNITOFFL) bit set to one specifies that the device server
     * may perform diagnostic operations that may affect the user accessible
     * medium on the logical unit (e.g., write operations to the user accessible
     * medium, or repositioning of the medium on sequential access devices). The
     * device server may ignore the UNITOFFL bit. A UNITOFFL bit set to zero
     * prohibits any diagnostic operations that may be detected by subsequent
     * tasks. When the {@link #selfTest} bit is set to zero, the UNITOFFL bit
     * shall be ignored.
     */
    private final boolean unitOffline;

    /**
     * A SCSI target device offline (DEVOFFL) bit set to one grants permission
     * to the device server to perform diagnostic operations that may affect all
     * the logical units in the SCSI target device (e.g., alteration of
     * reservations, log parameters, or sense data). The device server may
     * ignore the DEVOFFL bit. A DEVOFFL bit set to zero prohibits diagnostic
     * operations that may be detected by subsequent tasks. When the {@link #selfTest} bit is set to zero, the
     * DEVOFFL bit shall be ignored.
     */
    private final boolean deviceOffline;

    /**
     * The PARAMETER LIST LENGTH field specifies the length in bytes of the
     * parameter list that shall be transferred from the application client
     * Data-Out Buffer to the device server. A parameter list length of zero
     * specifies that no data shall be transferred. This condition shall not be
     * considered an error. If PF bit is set to one and the specified parameter
     * list length results in the truncation of the diagnostic page (e.g., the
     * parameter list length does not match the page length specified in the
     * diagnostic page), then the command shall be terminated with CHECK
     * CONDITION status, with the sense key set to ILLEGAL REQUEST, and the
     * additional sense code set to INVALID FIELD IN CDB.
     */
    private final short parameterListLength;

    public SendDiagnosticCdb(ByteBuffer buffer) {
        super(buffer);

        // deserialize specific fields
        final byte b = buffer.get(1);

        // self test
        selfTest = BitManip.getBit(b, 2);
        if (!selfTest)
            addIllegalFieldPointer(1, 2);// only the default self-test feature
                                         // is supported

        // self test code
        selfTestCode = SelfTestCode.getValue(b >>> 5);
        if (selfTest && selfTestCode != SelfTestCode.ALL_ZEROS)
            addIllegalFieldPointer(1, 7);

        // page format
        pageFormat = BitManip.getBit(b, 4);
        if (pageFormat)
            addIllegalFieldPointer(1, 4);// The implementation of the PF bit is
                                         // optional.

        // device offline
        deviceOffline = BitManip.getBit(b, 1);

        // unit offline
        unitOffline = BitManip.getBit(b, 0);

        // parameter list length
        parameterListLength = (short)ReadWrite.readTwoByteInt(buffer, 3);
    }

    public final boolean getSelfTest() {
        return selfTest;
    }

    public final SelfTestCode getSelfTestCode() {
        return selfTestCode;
    }

    public final boolean getPageFormat() {
        return pageFormat;
    }

    public final boolean getUnitOffline() {
        return unitOffline;
    }

    public final boolean getDeviceOffline() {
        return deviceOffline;
    }

    public final short getParameterListLength() {
        return parameterListLength;
    }

}
