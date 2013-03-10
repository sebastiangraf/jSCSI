/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jscsi.parser.tmf;

import java.util.HashMap;
import java.util.Map;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.Constants;
import org.jscsi.parser.InitiatorMessageParser;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.utils.Utils;

/**
 * <h1>TaskManagementFunctionRequestParser</h1>
 * <p>
 * This class parses a Task Management Function Request message defined in the iSCSI Standard (RFC3720).
 * <p>
 * <h4>TotalAHSLength and DataSegmentLength</h4> For this PDU TotalAHSLength and DataSegmentLength MUST be
 * <code>0</code>.
 * <p>
 * <h4>LUN</h4> This field is required for functions that address a specific LU (ABORT TASK, CLEAR TASK SET,
 * ABORT TASK SET, CLEAR ACA, LOGICAL UNIT RESET) and is reserved in all others.
 * <p>
 * 
 * @author Volker Wildi
 */
public final class TaskManagementFunctionRequestParser extends InitiatorMessageParser {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This enumeration defines all valid function codes, which are defined in
     * the iSCSI standard (RFC3720).
     */
    public static enum FunctionCode {
        /**
         * This is an artificial funcition code, which only used to indicate a
         * not set function code.
         */
        UNSET((byte)0),
        /** Aborts the task identified by the Referenced Task Tag field. */
        ABORT_TASK((byte)1),

        /** Aborts all Tasks issued via this session on the logical unit. */
        ABORT_TASK_SET((byte)2),

        /** Clears the Auto Contingent Allegiance condition. */
        CLEAR_ACA((byte)3);

        private byte value;

        private static Map<Byte, FunctionCode> mapping;

        static {
            FunctionCode.mapping = new HashMap<Byte, FunctionCode>();
            for (FunctionCode s : values()) {
                FunctionCode.mapping.put(s.value, s);
            }
        }

        private FunctionCode(final byte newValue) {

            value = newValue;
        }

        /**
         * Returns the value of this enumeration.
         * 
         * @return The value of this enumeration.
         */
        public final byte value() {

            return value;
        }

        /**
         * Returns the constant defined for the given <code>value</code>.
         * 
         * @param value
         *            The value to search for.
         * @return The constant defined for the given <code>value</code>. Or <code>null</code>, if this value
         *         is not defined by this
         *         enumeration.
         */
        public static final FunctionCode valueOf(final byte value) {

            return FunctionCode.mapping.get(value);
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The function code. */
    private FunctionCode functionCode;

    /** The referenced task tag. */
    private int referencedTaskTag;

    /** The referenced command sequence number. */
    private int refCmdSN;

    /** The expected data sequence number. */
    private int expDataSN;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty <code>TaskManagementFunctionRequestParser</code> object.
     * 
     * @param initProtocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>TaskManagementFunctionRequestParser</code> subclass object.
     */
    public TaskManagementFunctionRequestParser(final ProtocolDataUnit initProtocolDataUnit) {

        super(initProtocolDataUnit);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public final String toString() {

        final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

//        Utils.printField(sb, "Function", functionCode.value(), 1);
        Utils.printField(sb, "LUN", logicalUnitNumber, 1);
        Utils.printField(sb, "Referenced Task Tag", referencedTaskTag, 1);
        sb.append(super.toString());
        Utils.printField(sb, "RefCmdSN", refCmdSN, 1);
        Utils.printField(sb, "ExpDataSN", expDataSN, 1);

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public final DataSegmentFormat getDataSegmentFormat() {

        return DataSegmentFormat.NONE;
    }

    /** {@inheritDoc} */
    @Override
    public final void clear() {

        super.clear();

        functionCode = FunctionCode.UNSET;
        referencedTaskTag = 0x00000000;
        refCmdSN = 0x00000000;
        expDataSN = 0x00000000;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * For recovery purposes, the iSCSI target and initiator maintain a data
     * acknowledgement reference number - the first input DataSN number
     * unacknowledged by the initiator. When issuing a new command, this number
     * is set to <code>0</code>. If the function is TASK REASSIGN, which
     * establishes a new connection allegiance for a previously issued Read or
     * Bidirectional command, <code>ExpDataSN</code> will contain an updated
     * data acknowledgement reference number or the value <code>0</code>; the
     * latter indicating that the data acknowledgement reference number is
     * unchanged. The initiator MUST discard any data PDUs from the previous
     * execution that it did not acknowledge and the target MUST transmit all
     * Data-In PDUs (if any) starting with the data acknowledgement reference
     * number. The number of retransmitted PDUs may or may not be the same as
     * the original transmission depending on if there was a change in
     * MaxRecvDataSegmentLength in the reassignment. The target MAY also send no
     * more Data-In PDUs if all data has been acknowledged.
     * <p/>
     * The value of <code>ExpDataSN</code> MUST be <code>0</code> or higher than the <code>DataSN</code> of
     * the last acknowledged Data-In PDU, but not larger than <code>DataSN+1</code> of the last Data-In PDU
     * sent by the target. Any other value MUST be ignored by the target.
     * <p/>
     * For other functions this field is reserved.
     * <p/>
     * 
     * @return The expected data sequence number of this <code>TaskManagementFunctionRequestParser</code>
     *         object.
     */
    public final int getExpDataSN() {

        return expDataSN;
    }

    /**
     * The Task Management functions provide an initiator with a way to
     * explicitly control the execution of one or more Tasks (SCSI and iSCSI
     * tasks). The Task Management function codes are listed below. For a more
     * detailed description of SCSI task management, see [SAM2].
     * <p>
     * <table border="1">
     * <tr>
     * <th>Function Code</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>ABORT TASK - aborts the task identified by the Referenced Task Tag field.</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>ABORT TASK SET - aborts all Tasks issued via this session on the logical unit.</td>
     * </tr>
     * <tr>
     * <td>3</td>
     * <td>CLEAR ACA - clears the Auto Contingent Allegiance condition.</td>
     * </tr>
     * <tr>
     * <td>4</td>
     * <td>CLEAR TASK SET - aborts all Tasks in the appropriate task set as defined by the TST field in the
     * Control mode page (see [SPC3]).</td>
     * </tr>
     * <tr>
     * <td>5</td>
     * <td>LOGICAL UNIT RESET</td>
     * </tr>
     * <tr>
     * <td>6</td>
     * <td>TARGET WARM RESET</td>
     * </tr>
     * <tr>
     * <td>7</td>
     * <td>TARGET COLD RESET</td>
     * </tr>
     * <tr>
     * <td>8</td>
     * <td>TASK REASSIGN - reassigns connection allegiance for the task identified by the Referenced Task Tag
     * field to this connection, thus resuming the iSCSI exchanges for the task.</td>
     * </tr>
     * </table>
     * <p>
     * For all these functions, the Task Management function response MUST be returned as detailed in Section
     * 10.6 Task Management Function Response. All these functions apply to the referenced tasks regardless of
     * whether they are proper SCSI tasks or tagged iSCSI operations. Task management requests must act on all
     * the commands from the same session having a CmdSN lower than the task management CmdSN. LOGICAL UNIT
     * RESET, TARGET WARM RESET and TARGET COLD RESET may affect commands from other sessions or commands from
     * the same session with CmdSN equal or exceeding CmdSN.
     * <p>
     * If the task management request is marked for immediate delivery, it must be considered immediately for
     * execution, but the operations involved (all or part of them) may be postponed to allow the target to
     * receive all relevant tasks. According to [SAM2], for all the tasks covered by the Task Management
     * response (i.e., with CmdSN lower than the task management command CmdSN) but except the Task Management
     * response to a TASK REASSIGN, additional responses MUST NOT be delivered to the SCSI layer after the
     * Task Management response. The iSCSI initiator MAY deliver to the SCSI layer all responses received
     * before the Task Management response (i.e., it is a matter of implementation if the SCSI responses,
     * received before the Task Management response but after the task management request was issued, are
     * delivered to the SCSI layer by the iSCSI layer in the initiator). The iSCSI target MUST ensure that no
     * responses for the tasks covered by a task management function are delivered to the iSCSI initiator
     * after the Task Management response except for a task covered by a TASK REASSIGN.
     * <p>
     * For ABORT TASK SET and CLEAR TASK SET, the issuing initiator MUST continue to respond to all valid
     * target transfer tags (received via R2T, Text Response, NOP-In, or SCSI Data-In PDUs) related to the
     * affected task set, even after issuing the task management request. The issuing initiator SHOULD however
     * terminate (i.e., by setting the F-bit to 1) these response sequences as quickly as possible. The target
     * on its part MUST wait for responses on all affected target transfer tags before acting on either of
     * these two task management requests. In case all or part of the response sequence is not received (due
     * to digest errors) for a valid TTT, the target MAY treat it as a case of within-command error recovery
     * class (see Section 6.1.4.1 Recovery Within-command) if it is supporting ErrorRecoveryLevel >= 1, or
     * alternatively may drop the connection to complete the requested task set function.
     * <p>
     * If an ABORT TASK is issued for a task created by an immediate command then RefCmdSN MUST be that of the
     * Task Management request itself (i.e., CmdSN and RefCmdSN are equal); otherwise RefCmdSN MUST be set to
     * the CmdSN of the task to be aborted (lower than CmdSN).
     * <p>
     * If the connection is still active (it is not undergoing an implicit or explicit logout), ABORT TASK
     * MUST be issued on the same connection to which the task to be aborted is allegiant at the time the Task
     * Management Request is issued. If the connection is implicitly or explicitly logged out (i.e., no other
     * request will be issued on the failing connection and no other response will be received on the failing
     * connection), then an ABORT TASK function request may be issued on another connection. This Task
     * Management request will then establish a new allegiance for the command to be aborted as well as abort
     * it (i.e., the task to be aborted will not have to be retried or reassigned, and its status, if issued
     * but not acknowledged, will be reissued followed by the Task Management response).
     * <p>
     * At the target an ABORT TASK function MUST NOT be executed on a Task Management request; such a request
     * MUST result in Task Management response of "Function rejected".
     * <p>
     * For the LOGICAL UNIT RESET function, the target MUST behave as dictated by the Logical Unit Reset
     * function in [SAM2].
     * <p>
     * The implementation of the TARGET WARM RESET function and the TARGET COLD RESET function is OPTIONAL and
     * when implemented, should act as described below. The TARGET WARM RESET is also subject to SCSI access
     * controls on the requesting initiator as defined in [SPC3]. When authorization fails at the target, the
     * appropriate response as described in Section 10.6 Task Management Function Response MUST be returned by
     * the target. The TARGET COLD RESET function is not subject to SCSI access controls, but its execution
     * privileges may be managed by iSCSI mechanisms such as login authentication.
     * <p>
     * When executing the TARGET WARM RESET and TARGET COLD RESET functions, the target cancels all pending
     * operations on all Logical Units known by the issuing initiator. Both functions are equivalent to the
     * Target Reset function specified by [SAM2]. They can affect many other initiators logged in with the
     * servicing SCSI target port.
     * <p>
     * The target MUST treat the TARGET COLD RESET function additionally as a power on event, thus terminating
     * all of its TCP connections to all initiators (all sessions are terminated). For this reason, the
     * Service Response (defined by [SAM2]) for this SCSI task management function may not be reliably
     * delivered to the issuing initiator port. For the TASK REASSIGN function, the target should reassign the
     * connection allegiance to this new connection (and thus resume iSCSI exchanges for the task). TASK
     * REASSIGN MUST ONLY be received by the target after the connection on which the command was previously
     * executing has been successfully logged-out. The Task Management response MUST be issued before the
     * reassignment becomes effective. For additional usage semantics see Section 6.2 Retry and Reassign in
     * Recovery.
     * <p>
     * At the target a TASK REASSIGN function request MUST NOT be executed to reassign the connection
     * allegiance of a Task Management function request, an active text negotiation task, or a Logout task;
     * such a request MUST result in Task Management response of "Function rejected".
     * <p>
     * TASK REASSIGN MUST be issued as an immediate command.
     * 
     * @return The function code of this <code>TaskManagementFunctionRequestParser</code> object.
     */
    public final FunctionCode getFunction() {

        return functionCode;
    }

    /**
     * If an ABORT TASK is issued for a task created by an immediate command
     * then RefCmdSN MUST be that of the Task Management request itself (i.e., <code>CmdSN</code> and
     * <code>RefCmdSN</code> are equal).
     * <p>
     * For an ABORT TASK of a task created by non-immediate command <code>RefCmdSN</code> MUST be set to the
     * <code>CmdSN</code> of the task identified by the Referenced Task Tag field. Targets must use this field
     * as described in section 10.6.1 when the task identified by the Referenced Task Tag field is not with
     * the target.
     * <p>
     * Otherwise, this field is reserved.
     * 
     * @return The referenced command sequence number of this <code>TaskManagementFunctionRequestParser</code>
     *         object.
     */
    public final int getRefCmdSN() {

        return refCmdSN;
    }

    /**
     * The Initiator Task Tag of the task to be aborted for the ABORT TASK
     * function or reassigned for the TASK REASSIGN function. For all the other
     * functions this field MUST be set to the reserved value <code>0xffffffff</code>.
     * 
     * @return The referenced task tag of this <code>TaskManagementFunctionRequestParser</code> object.
     */
    public final int getReferencedTaskTag() {

        return referencedTaskTag;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes1to3(final int line) throws InternetSCSIException {

        functionCode = FunctionCode.valueOf((byte)(line & Constants.SECOND_BYTE_MASK));
        Utils.isReserved(line & Constants.LAST_TWO_BYTES_MASK);
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes20to23(final int line) throws InternetSCSIException {

        referencedTaskTag = line;
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes32to35(final int line) throws InternetSCSIException {

        refCmdSN = line;
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes36to39(final int line) throws InternetSCSIException {

        expDataSN = line;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void checkIntegrity() throws InternetSCSIException {

        String exceptionMessage;

        do {
            BasicHeaderSegment bhs = protocolDataUnit.getBasicHeaderSegment();
            if (bhs.getTotalAHSLength() != 0) {
                exceptionMessage = "TotalAHSLength must be 0!";
                break;
            }

            if (bhs.getDataSegmentLength() != 0) {
                exceptionMessage = "DataSegmentLength must be 0!";
                break;
            }

            if (functionCode != FunctionCode.ABORT_TASK && functionCode != FunctionCode.ABORT_TASK_SET
                && functionCode != FunctionCode.CLEAR_ACA) {
                Utils.isReserved(logicalUnitNumber);
            }

            // message is checked correctly
            return;
        } while (false);

        throw new InternetSCSIException(exceptionMessage);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes1to3() {

        return functionCode.value() << Constants.TWO_BYTES_SHIFT;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes20to23() {

        return referencedTaskTag;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes32to35() {

        return refCmdSN;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes36to39() {

        return expDataSN;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
