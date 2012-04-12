package org.jscsi.target.scsi.modeSense;

import java.nio.ByteBuffer;

import org.jscsi.target.util.BitManip;
import org.jscsi.target.util.ReadWrite;

/**
 * INFORMATION EXCEPTIONS CONTROL MODE PAGEs carry information about recent
 * INFORMATION EXCEPTION SCSI errors.
 * 
 * @author Andreas Ergenzinger
 */
public class InformationExceptionsControlModePage extends Page_0FormatModePage {

    /**
     * If the performance (PERF) bit is set to zero, informational exception
     * operations that are the cause of delays are acceptable. If the PERF bit
     * is set to one, the device server shall not cause delays while doing
     * informational exception operations. A PERF bit set to one may cause the
     * device server to disable some or all of the informational exceptions
     * operations, thereby limiting the reporting of informational exception
     * conditions.
     */
    private final boolean performance;

    /**
     * If background functions are supported and the Enable Background Function
     * (EBF) bit is set to one, then the device server shall enable background
     * functions. If the EBF bit is set to zero, the device server shall disable
     * the functions.
     * <p>
     * For the purposes of the EBF bit, background functions are defined as idle time functions that may
     * impact performance that are performed by a device server operating without errors but do not impact the
     * reliability of the logical unit (e.g., read scan).
     */
    private final boolean enableBackgroundFunction;

    /**
     * If the enable warning (EWASC) bit is set to zero, the device server shall
     * disable reporting of the warning. The MRIE field is ignored when DEXCPT
     * is set to one and EWASC is set to zero. If the EWASC bit is set to one,
     * warning reporting shall be enabled. The method for reporting the warning
     * when the EWASC bit is set to one is determined from the MRIE field.
     */
    private final boolean enableWarning;

    /**
     * A disable exception control (DEXCPT) bit set to zero indicates the
     * failure prediction threshold exceeded reporting shall be enabled. The
     * method for reporting the failure prediction threshold exceeded when the
     * DEXCPT bit is set to zero is determined from the MRIE field. A DEXCPT bit
     * set to one indicates the device server shall disable reporting of the
     * failure prediction threshold exceeded. The MRIE field is ignored when
     * DEXCPT is set to one and EWASC is set to zero.
     */
    private final boolean disableExceptionControl;

    /**
     * A TEST bit set to one shall create a test device failure at the next
     * interval time, as specified by the INTERVAL TIMER field, if the DEXCPT
     * bit is set to zero. When the TEST bit is set to one, the MRIE and REPORT
     * COUNT fields shall apply as if the TEST bit were zero. The test device
     * failure shall be reported with the additional sense code set to FAILURE
     * PREDICTION THRESHOLD EXCEEDED (FALSE). If both the TEST bit and the
     * DEXCPT bit are one, the MODE SELECT command shall be terminated with
     * CHECK CONDITION status, with the sense key set to ILLEGAL REQUEST, and
     * the additional sense code set to INVALID FIELD IN PARAMETER LIST. A TEST
     * bit set to zero shall instruct the device server not to generate any test
     * device failure notifications.
     */
    private final boolean test;

    /**
     * If the log errors (LOGERR) bit is set to zero, the logging of
     * informational exception conditions by a device server is vendor specific.
     * If the LOGERR bit is set to one, the device server shall log
     * informational exception conditions.
     */
    private final boolean logErrors;

    /**
     * The value in the method of reporting informational exceptions (MRIE)
     * field defines the method that shall be used by the device server to
     * report informational exception conditions (see SPC3R23 table 257,
     * p.294f). The priority of reporting multiple information exceptions is
     * vendor specific.
     */
    private final int methodOfReportingInformationalExceptionConditions;

    /**
     * The value in the INTERVAL TIMER field is the period in 100 millisecond
     * increments for reporting that an informational exception condition has
     * occurred. The device server shall not report informational exception
     * conditions more frequently than the time specified by the INTERVAL TIMER
     * field and shall report them after the time specified by INTERVAL TIMER
     * field has elapsed. After the informational exception condition has been
     * reported the interval timer shall be restarted.
     * <p>
     * A value of zero or FFFF FFFFh in the INTERVAL TIMER field indicates that the period for reporting an
     * informational exception condition is vendor specific.
     */
    private final int intervalTimer;

    /**
     * The value in the REPORT COUNT field is the number of times to report an
     * informational exception condition to the application client. A value of
     * zero in the REPORT COUNT field indicates there is no limit on the number
     * of times the device server reports an informational exception condition.
     */
    private final int reportCount;

    public InformationExceptionsControlModePage(boolean parametersSaveable, final boolean performance,
        final boolean enableBackgroundFunction, final boolean enableWarning,
        final boolean disableExceptionControl, final boolean test, final boolean logErrors,
        final int methodOfReportingInformationalExceptionConditions, final int intervalTimer,
        final int reportCount) {
        super(parametersSaveable,// PS
            0x1c,// page code
            0x0a);// page length
        this.performance = performance;
        this.enableBackgroundFunction = enableBackgroundFunction;
        this.enableWarning = enableWarning;
        this.disableExceptionControl = disableExceptionControl;
        this.test = test;
        this.logErrors = logErrors;
        this.methodOfReportingInformationalExceptionConditions =
            methodOfReportingInformationalExceptionConditions;
        this.intervalTimer = intervalTimer;
        this.reportCount = reportCount;

    }

    @Override
    protected void serializeModeParameters(ByteBuffer buffer, int index) {

        // byte 2 flags
        buffer.position(index + 2);
        byte b = 0;
        b = BitManip.getByteWithBitSet(b, 7, performance);// PERF
        b = BitManip.getByteWithBitSet(b, 5, enableBackgroundFunction);// PERF
        b = BitManip.getByteWithBitSet(b, 4, enableWarning);// EWASC
        b = BitManip.getByteWithBitSet(b, 3, disableExceptionControl);// DEXCPT
        b = BitManip.getByteWithBitSet(b, 2, test);// TEST
        b = BitManip.getByteWithBitSet(b, 0, logErrors);// LOGERR
        buffer.put(b);

        // byte 3
        b = (byte)(methodOfReportingInformationalExceptionConditions & 31);// MRIE
        buffer.put(b);

        // INTERVAL TIMER and REPORT COUNT
        ReadWrite.writeInt(intervalTimer,// value
            buffer,// buffer
            index + 4);// start index
        ReadWrite.writeInt(reportCount,// value
            buffer,// buffer
            index + 8);// start index
    }

}
