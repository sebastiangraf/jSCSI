/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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
package org.jscsi.parser.snack;

import java.util.HashMap;
import java.util.Map;

import org.jscsi.core.utils.Utils;
import org.jscsi.parser.Constants;
import org.jscsi.parser.InitiatorMessageParser;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.exception.InternetSCSIException;

/**
 * <h1>SNACKRequestParser</h1>
 * <p>
 * This class parses a SNACK Request message defined in the iSCSI Standard
 * (RFC3720).
 * <p>
 * If the implementation supports ErrorRecoveryLevel greater than zero, it MUST
 * support all SNACK types.
 * <p>
 * The SNACK is used by the initiator to request the retransmission of
 * numbered-responses, data, or R2T PDUs from the target. The SNACK request
 * indicates the numbered-responses or data "runs" whose retransmission is
 * requested by the target, where the run starts with the first StatSN, DataSN,
 * or R2TSN whose retransmission is requested and indicates the number of
 * Status, Data, or R2T PDUs requested including the first. <code>0</code> has
 * special meaning when used as a starting number and length:
 * <p>
 * <ul>
 * <li>When used in RunLength, it means all PDUs starting with the initial.</li>
 * <li>When used in both BegRun and RunLength, it means all unacknowledged PDUs.
 * </li>
 * </ul>
 * The numbered-response(s) or R2T(s), requested by a SNACK, MUST be delivered
 * as exact replicas of the ones that the target transmitted originally except
 * for the fields ExpCmdSN, MaxCmdSN, and ExpDataSN, which MUST carry the
 * current values. R2T(s)requested by SNACK MUST also carry the current value of
 * StatSN.
 * <p>
 * The numbered Data-In PDUs, requested by a Data SNACK MUST be delivered as
 * exact replicas of the ones that the target transmitted originally except for
 * the fields ExpCmdSN and MaxCmdSN, which MUST carry the current values and
 * except for resegmentation (see Section 10.16.3 Resegmentation).
 * <p>
 * Any SNACK that requests a numbered-response, Data, or R2T that was not sent
 * by the target or was already acknowledged by the initiator, MUST be rejected
 * with a reason code of "Protocol error".
 * <p>
 * <h4>Data Acknowledgement</h4> If an initiator operates at ErrorRecoveryLevel
 * <code>1</code> or higher, it MUST issue a SNACK of type DataACK after
 * receiving a Data-In PDU with the A bit set to <code>1</code>. However, if the
 * initiator has detected holes in the input sequence, it MUST postpone issuing
 * the SNACK of type DataACK until the holes are filled. An initiator MAY ignore
 * the A bit if it deems that the bit is being set aggressively by the target
 * (i.e., before the MaxBurstLength limit is reached).
 * <p>
 * The DataACK is used to free resources at the target and not to request or
 * imply data retransmission.
 * <p>
 * An initiator MUST NOT request retransmission for any data it had already
 * acknowledged.
 * <p>
 * <h4>Resegmentation</h4> If the initiator MaxRecvDataSegmentLength changed
 * between the original transmission and the time the initiator requests
 * retransmission, the initiator MUST issue a R-Data SNACK (see Section 10.16.1
 * Type). With R-Data SNACK, the initiator indicates that it discards all the
 * unacknowledged data and expects the target to resend it. It also expects
 * resegmentation. In this case, the retransmitted Data-In PDUs MAY be different
 * from the ones originally sent in order to reflect changes in
 * MaxRecvDataSegmentLength. Their DataSN starts with the BegRun of the last
 * DataACK received by the target if any was received; otherwise it starts with
 * 0 and is increased by 1 for each resent Data-In PDU.
 * <p>
 * A target that has received a R-Data SNACK MUST return a SCSI Response that
 * contains a copy of the SNACK Tag field from the R-Data SNACK in the SCSI
 * Response SNACK Tag field as its last or only Response. For example, if it has
 * already sent a response containing another value in the SNACK Tag field or
 * had the status included in the last Data-In PDU, it must send a new SCSI
 * Response PDU. If a target sends more than one SCSI Response PDU due to this
 * rule, all SCSI responses must carry the same StatSN (see Section 10.4.4 SNACK
 * Tag). If an initiator attempts to recover a lost SCSI Response (with a Status
 * SNACK, see Section 10.16.1 Type) when more than one response has been sent,
 * the target will send the SCSI Response with the latest content known to the
 * target, including the last SNACK Tag for the command.
 * <p>
 * For considerations in allegiance reassignment of a task to a connection with
 * a different MaxRecvDataSegmentLength, refer to Section 6.2.2 Allegiance
 * Reassignment.
 * <p>
 * <h4>Initiator Task Tag</h4> For Status SNACK and DataACK, the Initiator Task
 * Tag MUST be set to the reserved value <code>0xffffffff</code>. In all other
 * cases, the Initiator Task Tag field MUST be set to the Initiator Task Tag of
 * the referenced command.
 * <p>
 * 
 * @author Volker Wildi
 */
public final class SNACKRequestParser extends InitiatorMessageParser {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This enumeration defines all valid SNACK types.
     * <p>
     * <table border="1">
     * <tr>
     * <th>Type</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>Data/R2T SNACK - requesting retransmission of one or more Data- In or
     * R2T PDUs.</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>Status SNACK - requesting retransmission of one or more numbered
     * responses.</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>DataACK - positively acknowledges Data-In PDUs.</td>
     * </tr>
     * <tr>
     * <td>3</td>
     * <td>R-Data SNACK - requesting retransmission of Data-In PDUs with
     * possible resegmentation and status tagging.</td>
     * </tr>
     * </table>
     * <p>
     * All other values are reserved.
     * <p>
     * Data/R2T SNACK, Status SNACK, or R-Data SNACK for a command MUST precede
     * status acknowledgement for the given command.
     */
    public static enum SNACKType {

        /** The Data/R2T-Snack. */
        DATA_R2T_SNACK((byte) 0),

        /** The Status-SNACK. */
        STATUS_SNACK((byte) 1),

        /** The DataACK. */
        DATA_ACK((byte) 2),

        /** The R-Data SNACK. */
        R_DATA_SNACK((byte) 3);

        private final byte value;

        private static Map<Byte, SNACKType> mapping;

        static {
            SNACKType.mapping = new HashMap<Byte, SNACKType>();
            for (SNACKType s : values()) {
                SNACKType.mapping.put(s.value, s);
            }
        }

        private SNACKType(final byte newValue) {

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
         * @return The constant defined for the given <code>value</code>. Or
         *         <code>null</code>, if this value is not defined by this
         *         enumeration.
         */
        public static final SNACKType valueOf(final byte value) {

            return SNACKType.mapping.get(value);
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Bit mask to extract the SNACK type. */
    private static final int TYPE_MASK = 0x000F0000;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The SNACK type. */
    private SNACKType type;

    /** The target transfer tag. */
    private int targetTransferTag;

    /** The first PDU number to start from with the retransmission. */
    private int begRun;

    /** The run length of requested PDUs to retransmit. */
    private int runLength;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty <code>SNACKRequestParser</code>
     * object.
     * 
     * @param initProtocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>SNACKRequestParser</code> subclass object.
     */
    public SNACKRequestParser(final ProtocolDataUnit initProtocolDataUnit) {

        super(initProtocolDataUnit);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * The <code>DataSN</code>, <code>R2TSN</code>, or <code>StatSN</code> of
     * the first PDU whose retransmission is requested (Data/R2T and Status
     * SNACK), or the next expected DataSN (DataACK SNACK).
     * <p>
     * <code>BegRun</code> <code>0</code> when used in conjunction with
     * <code>RunLength</code> <code>0</code> means resend all unacknowledged
     * Data-In, R2T or Response PDUs.
     * <p>
     * <code>BegRun</code> MUST be <code>0</code> for a R-Data SNACK.
     * 
     * @return The BegRun of this <code>SNACKRequestParser</code> obejct.
     */
    public final int getBegRun() {

        return begRun;
    }

    /**
     * The number of PDUs whose retransmission is requested.
     * <p>
     * <code>RunLength</code> <code>0</code> signals that all Data-In, R2T, or
     * Response PDUs carrying the numbers equal to or greater than BegRun have
     * to be resent.
     * <p>
     * The <code>RunLength</code> MUST also be <code>0</code> for a DataACK
     * SNACK in addition to R-Data SNACK.
     * 
     * @return The RunLength of this <code>SNACKRequestParser</code> object.
     */
    public final int getRunLength() {

        return runLength;
    }

    /**
     * For an R-Data SNACK, this field MUST contain a value that is different
     * from <code>0</code> or <code>0xffffffff</code> and is unique for the task
     * (identified by the Initiator Task Tag). This value MUST be copied by the
     * iSCSI target in the last or only SCSI Response PDU it issues for the
     * command.
     * <p>
     * For <code>DataACK</code>, the Target Transfer Tag MUST contain a copy of
     * the Target Transfer Tag and LUN provided with the SCSI Data-In PDU with
     * the <code>A</code> bit set to <code>1</code>.
     * <p>
     * In all other cases, the Target Transfer Tag field MUST be set to the
     * reserved value of <code>0xffffffff</code>.
     * 
     * @return The target transfer tag of this <code>SNACKRequestParser</code>
     *         object.
     */
    public final int getTargetTransferTag() {

        return targetTransferTag;
    }

    /**
     * Returns the SNACK Function Code of this <code>SNACKRequestParser</code>
     * object.
     * 
     * @return The SNACK Function code of this <code>SNACKRequestParser</code>
     *         object.
     * @see #SNACKType
     */
    public final SNACKType getType() {

        return type;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Sets the <code>begRun</code> variable to the given new value.
     * 
     * @param newBegRun
     *            The new value.
     */
    public final void setBegRun(final int newBegRun) {

        begRun = newBegRun;
    }

    /**
     * Sets the Run Length to the given value.
     * 
     * @param newRunLength
     *            The new value.
     */
    public final void setRunLength(final int newRunLength) {

        runLength = newRunLength;
    }

    /**
     * Sets the Target Transfer Tag to the given value.
     * 
     * @param newTargetTransferTag
     *            The new value.
     */
    public final void setTargetTransferTag(final int newTargetTransferTag) {

        targetTransferTag = newTargetTransferTag;
    }

    /**
     * Sets the type of this SNACKRequest to the given value.
     * 
     * @param newType
     *            The new value.
     */
    public final void setType(final SNACKType newType) {

        type = newType;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public final String toString() {

        final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

        Utils.printField(sb, "Type", type.value(), 1);
        Utils.printField(sb, "LUN", logicalUnitNumber, 1);
        Utils.printField(sb, "Target Transfer Tag", targetTransferTag, 1);
        sb.append(super.toString());
        Utils.printField(sb, "BegRun", begRun, 1);
        Utils.printField(sb, "Run Length", runLength, 1);

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

        type = SNACKType.DATA_ACK;
        targetTransferTag = 0x00000000;
        begRun = 0x00000000;
        runLength = 0x00000000;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes1to3(final int line)
            throws InternetSCSIException {

        type = SNACKType
                .valueOf((byte) ((line & TYPE_MASK) >> Constants.TWO_BYTES_SHIFT));
        Utils.isReserved(line & Constants.LAST_TWO_BYTES_MASK);
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes20to23(final int line)
            throws InternetSCSIException {

        targetTransferTag = line;
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes40to43(final int line)
            throws InternetSCSIException {

        begRun = line;
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes44to47(final int line)
            throws InternetSCSIException {

        runLength = line;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void checkIntegrity() throws InternetSCSIException {

        // FIXME: Sure?
        // do nothing...
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes1to3() {

        return type.value() << Constants.TWO_BYTES_SHIFT;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes20to23() {

        return targetTransferTag;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes24to27() {

        return 0;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes40to43() {

        return begRun;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes44to47() {

        return runLength;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
