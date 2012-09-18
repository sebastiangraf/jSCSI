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
package org.jscsi.parser.data;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.Constants;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.TargetMessageParser;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.scsi.SCSIStatus;
import org.jscsi.utils.Utils;

/**
 * This class parses a Data-In message defined in the iSCSI Standard (RFC3720).
 * 
 * @author Volker Wildi
 */
public class DataInParser extends TargetMessageParser {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Acknowledge flag mask. */
    private static final int ACKNOWLEDGE_FLAG_MASK = 0x00400000;

    /** Status flag mask to extract. */
    private static final int STATUS_FLAG_MASK = 0x00010000;

    /** Bit mask, where the 11th, 12th and the 13th bit are set. */
    private static final int BIT_11_TO_13_FLAG_MASK = 0x00380000;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The acknowledge Flag. */
    private boolean acknowledgeFlag;

    /** The Bidirectional Read Residual Overflow Flag (o bit). */
    private boolean bidirectionalReadResidualOverflow;

    /** The Bidirectional Read Residual Underflow Flag (u bit). */
    private boolean bidirectionalReadResidualUnderflow;

    /** The Residual Overflow Flag (O bit). */
    private boolean residualOverflow;

    /** The Residual Underflow Flag (U bit). */
    private boolean residualUnderflow;

    /** The Status Flag (S bit). */
    private boolean statusFlag;

    /** The status code. */
    private SCSIStatus status;

    /** The Data Sequence Number. */
    private int dataSequenceNumber;

    /** The Buffer Offset. */
    private int bufferOffset;

    /** The Residual Count. */
    private int residualCount;

    /** The Target Transfer Tag. */
    private int targetTransferTag;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty DataInParser object.
     * 
     * @param initProtocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>DataInParser</code> subclass object.
     */
    public DataInParser(final ProtocolDataUnit initProtocolDataUnit) {

        super(initProtocolDataUnit);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public String getShortInfo() {

        return super.getShortInfo() + ", dataSN: " + dataSequenceNumber + ", bufferOffset: " + bufferOffset;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

        Utils.printField(sb, "StatusFlag", statusFlag, 1);
        Utils.printField(sb, "Status", status.value(), 1);
        Utils.printField(sb, "LUN", logicalUnitNumber, 1);
        Utils.printField(sb, "Target Task tag", targetTransferTag, 1);
        Utils.printField(sb, "StatSN", statusSequenceNumber, 1);
        Utils.printField(sb, "MaxCmdSN", maximumCommandSequenceNumber, 1);
        Utils.printField(sb, "DataSN", dataSequenceNumber, 1);
        Utils.printField(sb, "Buffer Offset", bufferOffset, 1);
        Utils.printField(sb, "Residual Count", residualCount, 1);
        sb.append(super.toString());

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public DataSegmentFormat getDataSegmentFormat() {

        return DataSegmentFormat.BINARY;
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {

        super.clear();

        acknowledgeFlag = false;
        bidirectionalReadResidualOverflow = false;
        bidirectionalReadResidualUnderflow = false;
        residualOverflow = false;
        residualUnderflow = false;
        statusFlag = false;

        status = null;

        dataSequenceNumber = 0;
        bufferOffset = 0;
        residualCount = 0;
        targetTransferTag = 0;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public final boolean incrementSequenceNumber() {

        return isStatusFlag();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * The Buffer Offset field contains the offset of this PDU payload data
     * within the complete data transfer. The sum of the buffer offset and
     * length should not exceed the expected transfer length for the command.<br/>
     * The order of data PDUs within a sequence is determined by DataPDUInOrder.
     * When set to Yes, it means that PDUs have to be in increasing Buffer
     * Offset order and overlays are forbidden.<br/>
     * The ordering between sequences is determined by DataSequenceInOrder. When
     * set to Yes, it means that sequences have to be in increasing Buffer
     * Offset order and overlays are forbidden.
     * 
     * @return The buffer offset of this DataInParser object.
     */
    public int getBufferOffset() {

        return bufferOffset;
    }

    /**
     * For input (read) or bidirectional Data-In PDUs, the DataSN is the input
     * PDU number within the data transfer for the command identified by the
     * Initiator Task Tag. <br/>
     * R2T and Data-In PDUs, in the context of bidirectional commands, share the
     * numbering sequence (see Section 3.2.2.3 Data Sequencing). <br/>
     * For output (write) data PDUs, the DataSN is the Data-Out PDU number
     * within the current output sequence. The current output sequence is either
     * identified by the Initiator Task Tag (for unsolicited data) or is a data
     * sequence generated for one R2T (for data solicited through R2T).
     * 
     * @return The Data Sequence Number of this DataInParser object.
     */
    public int getDataSequenceNumber() {

        return dataSequenceNumber;
    }

    /**
     * The Residual Count field MUST be valid in the case where either the U bit
     * or the O bit is set. If neither bit is set, the Residual Count field is
     * reserved. Targets may set the residual count and initiators may use it
     * when the response code is "completed at target" (even if the status
     * returned is not GOOD). If the O bit is set, the Residual Count indicates
     * the number of bytes that were not transferred because the initiator’s
     * Expected Data Transfer Length was not sufficient. If the U bit is set,
     * the Residual Count indicates the number of bytes that were not
     * transferred out of the number of bytes expected to be transferred.
     * 
     * @return The Residual Count of this object.
     */
    public int getResidualCount() {

        return residualCount;
    }

    /**
     * On outgoing data, the Target Transfer Tag is provided to the target if
     * the transfer is honoring an R2T. In this case, the Target Transfer Tag
     * field is a replica of the Target Transfer Tag provided with the R2T. <br/>
     * On incoming data, the Target Transfer Tag and LUN MUST be provided by the
     * target if the A bit is set to <code>1</code>; otherwise they are
     * reserved. The Target Transfer Tag and LUN are copied by the initiator
     * into the SNACK of type DataACK that it issues as a result of receiving a
     * SCSI Data-In PDU with the A bit set to <code>1</code>.<br/>
     * The Target Transfer Tag values are not specified by this protocol except
     * that the value <code>0xffffffff</code> is reserved and means that the
     * Target Transfer Tag is not supplied. If the Target Transfer Tag is
     * provided, then the LUN field MUST hold a valid value and be consistent
     * with whatever was specified with the command; otherwise, the LUN field is
     * reserved.
     * 
     * @return Returns the Target Transfer Tag of this DataInParser object.
     */
    public int getTargetTaskTag() {

        return targetTransferTag;
    }

    /**
     * For sessions with ErrorRecoveryLevel <code>1</code> or higher, the target
     * sets this bit to <code>1</code> to indicate that it requests a positive
     * acknowledgement from the initiator for the data received. The target
     * should use the A bit moderately; it MAY only set the A bit to <code>1</code> once every MaxBurstLength
     * bytes, or on the last Data-In
     * PDU that concludes the entire requested read data transfer for the task
     * from the target’s perspective, and it MUST NOT do so more frequently. The
     * target MUST NOT set to <code>1</code> the <code>A</code> bit for sessions
     * with ErrorRecoveryLevel=<code>0</code>. The initiator MUST ignore the A
     * bit <br/>
     * <p>
     * On receiving a Data-In PDU with the A bit set to <code>1</code> on a session with ErrorRecoveryLevel
     * greater than <code>0</code>, if there are no holes in the read data until that Data-In PDU, the
     * initiator MUST issue a SNACK of type DataACK except when it is able to acknowledge the status for the
     * task immediately via ExpStatSN on other outbound PDUs if the status for the task is also received. In
     * the latter case (acknowledgement through ExpStatSN), sending a SNACK of type DataACK in response to the
     * A bit is OPTIONAL, but if it is done, it must not be sent after the status acknowledgement through
     * ExpStatSN. If the initiator has detected holes in the read data prior to that Data-In PDU, it MUST
     * postpone issuing the SNACK of type DataACK until the holes are filled. An initiator also MUST NOT
     * acknowledge the status for the task before those holes are filled. A status acknowledgement for a task
     * that generated the Data-In PDUs is considered by the target as an implicit acknowledgement of the
     * Data-In PDUs if such an acknowledgement was requested by the target.
     * 
     * @return Returns <code>true</code>, if the AcknowledgeBit is set. Else <code>false</code>.
     */
    public boolean isAcknowledgeFlag() {

        return acknowledgeFlag;
    }

    /**
     * In this case, the Bidirectional Read Residual Count indicates the number
     * of bytes that were not transferred to the initiator because the
     * initiator’s Expected Bidirectional Read Data Transfer Length was not
     * sufficient.
     * 
     * @return <code>True</code>, if the ReadResidualOverflow-Flag of this
     *         object is set. Else <code>false</code>.
     */
    public boolean isBidirectionalReadResidualOverflow() {

        return bidirectionalReadResidualOverflow;
    }

    /**
     * In this case, the Bidirectional Read Residual Count indicates the number
     * of bytes that were not transferred to the initiator out of the number of
     * bytes expected to be transferred.
     * 
     * @return <code>True</code>, if the ReadResidualUnderflow-Flag of this
     *         object is set. Else <code>false</code>.
     */
    public boolean isBidirectionalReadResidualUnderflow() {

        return bidirectionalReadResidualUnderflow;
    }

    /**
     * In this case, the Residual Count indicates the number of bytes that were
     * not transferred because the initiator’s Expected Data Transfer Length was
     * not sufficient. For a bidirectional operation, the Residual Count
     * contains the residual for the write operation.
     * 
     * @return <code>True</code>, if the ResidualOverflow-Flag of this object is
     *         set. Else <code>false</code>.
     */
    public boolean isResidualOverflow() {

        return residualOverflow;
    }

    /**
     * In this case, the Residual Count indicates the number of bytes that were
     * not transferred out of the number of bytes that were expected to be
     * transferred. For a bidirectional operation, the Residual Count contains
     * the residual for the write operation.
     * 
     * @return <code>True</code>, if the ResidualUnderflow-Flag of this object
     *         is set. Else <code>false</code>.
     */
    public boolean isResidualUnderflow() {

        return residualUnderflow;
    }

    /**
     * The Status field is used to report the SCSI status of the command (as
     * specified in [SAM2]) and is only valid if the Response Code is Command
     * Completed at target.
     * <p>
     * If a SCSI device error is detected while data from the initiator is still expected (the command PDU did
     * not contain all the data and the target has not received a Data PDU with the final bit Set), the target
     * MUST wait until it receives a Data PDU with the F bit set in the last expected sequence before sending
     * the Response PDU.
     * 
     * @return The status code of this object.
     * @see SCSIStatus
     */
    public SCSIStatus getStatus() {

        return status;
    }

    /**
     * Set this to indicate that the Command Status field contains status. If
     * this bit is set to <code>1</code>, the <code>F bit</code> MUST also be
     * set to <code>1</code>.<br/>
     * The fields StatSN, Status, and Residual Count only have meaningful
     * content if the S bit is set to <code>1</code> and their values are
     * defined in Section 10.4 SCSI Response.
     * 
     * @return <code>True</code>, if the Status-Flag of this object is set. Else <code>false</code>.
     */
    public boolean isStatusFlag() {

        return statusFlag;
    }

    public void setAcknowledgeFlag(boolean acknowledgeFlag) {
        this.acknowledgeFlag = acknowledgeFlag;
    }

    public void setResidualOverflowFlag(boolean residualOverflowFlag) {
        this.residualOverflow = residualOverflowFlag;
    }

    public void setResidualUnderflowFlag(boolean residualUnderflowFlag) {
        this.residualUnderflow = residualUnderflowFlag;
    }

    public void setStatusFlag(boolean statusFlag) {
        this.statusFlag = statusFlag;
    }

    public void setStatus(SCSIStatus status) {
        this.status = status;
    }

    public void setTargetTransferTag(int targetTransferTag) {
        this.targetTransferTag = targetTransferTag;
    }

    public void setDataSequenceNumber(int dataSequenceNumber) {
        this.dataSequenceNumber = dataSequenceNumber;
    }

    public void setBufferOffset(int bufferOffset) {
        this.bufferOffset = bufferOffset;
    }

    public void setResidualCount(int residualCount) {
        this.residualCount = residualCount;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected void deserializeBytes1to3(final int line) throws InternetSCSIException {

        acknowledgeFlag = Utils.isBitSet(line & ACKNOWLEDGE_FLAG_MASK);
        residualOverflow = Utils.isBitSet(line & Constants.RESIDUAL_OVERFLOW_FLAG_MASK);
        residualUnderflow = Utils.isBitSet(line & Constants.RESIDUAL_UNDERFLOW_FLAG_MASK);
        statusFlag = Utils.isBitSet(line & STATUS_FLAG_MASK);

        Utils.isReserved(line & BIT_11_TO_13_FLAG_MASK);
        Utils.isReserved(line & Constants.THIRD_BYTE_MASK);
        status = SCSIStatus.valueOf((byte)(line & Constants.FOURTH_BYTE_MASK));
    }

    /** {@inheritDoc} */
    @Override
    protected void deserializeBytes20to23(final int line) throws InternetSCSIException {

        targetTransferTag = line;
    }

    /** {@inheritDoc} */
    @Override
    protected void deserializeBytes36to39(final int line) throws InternetSCSIException {
        dataSequenceNumber = line;
    }

    /** {@inheritDoc} */
    @Override
    protected void deserializeBytes40to43(final int line) throws InternetSCSIException {

        bufferOffset = line;
    }

    /** {@inheritDoc} */
    @Override
    protected void deserializeBytes44to47(final int line) throws InternetSCSIException {

        residualCount = line;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected void checkIntegrity() throws InternetSCSIException {

        String exceptionMessage;
        do {
            if (statusFlag && !protocolDataUnit.getBasicHeaderSegment().isFinalFlag()) {
                exceptionMessage = "FinalFlag must also be set, if the StatusFlag is set.";
                break;
            }

            if (!statusFlag && (bidirectionalReadResidualOverflow || bidirectionalReadResidualUnderflow)
                && (residualOverflow || residualUnderflow)) {
                exceptionMessage = "The StatusFlag must be set, if any flags are set.";
                break;
            }

            if (acknowledgeFlag && (targetTransferTag == 0 && logicalUnitNumber == 0)) {
                exceptionMessage =
                    "If the AcknowledgeFlag is set, the TargetTaskTag"
                        + " and the LogicalUnitNumber must be unequal 0.";
                break;
            }

            if (!acknowledgeFlag && (targetTransferTag != 0 && logicalUnitNumber != 0)) {
                exceptionMessage =
                    "The TargetTransferTag and LogicalUnitNumber must"
                        + " be reserved, because the AcknowledgeFlag is not set.";
                break;
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
    protected int serializeBytes1to3() {

        int line = status.value();
        if (acknowledgeFlag) {
            line |= ACKNOWLEDGE_FLAG_MASK;
        }
        if (residualOverflow) {
            line |= Constants.READ_RESIDUAL_OVERFLOW_FLAG_MASK;
        }
        if (residualUnderflow) {
            line |= Constants.READ_RESIDUAL_UNDERFLOW_FLAG_MASK;
        }
        if (statusFlag) {
            line |= STATUS_FLAG_MASK;
        }

        return line;
    }

    /** {@inheritDoc} */
    @Override
    protected int serializeBytes20to23() {

        return targetTransferTag;
    }

    /** {@inheritDoc} */
    @Override
    protected int serializeBytes36to39() {

        return dataSequenceNumber;
    }

    /** {@inheritDoc} */
    @Override
    protected int serializeBytes40to43() {

        return bufferOffset;
    }

    /** {@inheritDoc} */
    @Override
    protected int serializeBytes44to47() {

        return residualCount;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
