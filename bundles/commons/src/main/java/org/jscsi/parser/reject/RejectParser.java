/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
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
package org.jscsi.parser.reject;

import java.util.HashMap;
import java.util.Map;

import org.jscsi.parser.Constants;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.TargetMessageParser;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.utils.Utils;

/**
 * <h1>RejectParser</h1>
 * <p>
 * This class parses a Reject message defined in the iSCSI Standard (RFC3720).
 * 
 * @author Volker Wildi
 */
public final class RejectParser extends TargetMessageParser {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This enumeration defines all valid reasonCode code, which are defined in
     * the iSCSI Standard (RFC 3720).
     * <p>
     * <table border="1">
     * <tr>
     * <th>Code (hex)</th>
     * <th>Explanation</th>
     * <th>Can the original PDU be re-sent?</th>
     * </tr>
     * <tr>
     * <td>0x01</td>
     * <td>Reserved</td>
     * <td>no</td>
     * </tr>
     * <tr>
     * <td>0x02</td>
     * <td>Data (payload) Digest Error</td>
     * <td>yes (Note 1)</td>
     * </tr>
     * <tr>
     * <td>0x03</td>
     * <td>SNACK Reject</td>
     * <td>yes</td>
     * </tr>
     * <tr>
     * <td>0x04</td>
     * <td>Protocol Error (e.g., SNACK request for a status that was already acknowledged)</td>
     * <td>no</td>
     * </tr>
     * <tr>
     * <td>0x05</td>
     * <td>Command not supported</td>
     * <td>no</td>
     * </tr>
     * <tr>
     * <td>0x06</td>
     * <td>Immediate Command Reject - too many immediate commands</td>
     * <td>yes</td>
     * </tr>
     * <tr>
     * <td>0x07</td>
     * <td>Task in progress</td>
     * <td>no</td>
     * </tr>
     * <tr>
     * <td>0x08</td>
     * <td>Invalid Data ACK</td>
     * <td>no</td>
     * </tr>
     * <tr>
     * <td>0x09</td>
     * <td>Invalid PDU field</td>
     * <td>no (Note 2)</td>
     * </tr>
     * <tr>
     * <td>0x0a</td>
     * <td>Long Operation Reject - Can't generate Target Transfer Tag - out of resources</td>
     * <td>yes</td>
     * </tr>
     * <tr>
     * <td>0x0b</td>
     * <td>Negotiation Reset</td>
     * <td>no</td>
     * </tr>
     * <tr>
     * <td>0x0c</td>
     * <td>Waiting for Logout</td>
     * <td>no</td>
     * </tr>
     * </table>
     * <p>
     * Note 1: For iSCSI, Data-Out PDU retransmission is only done if the target requests retransmission with
     * a recovery R2T. However, if this is the data digest error on immediate data, the initiator may choose
     * to retransmit the whole PDU including the immediate data.
     * <p>
     * Note 2: A target should use this reasonCode code for all invalid values of PDU fields that are meant to
     * describe a task, a response, or a data transfer. Some examples are invalid TTT/ITT, buffer offset, LUN
     * qualifying a TTT, and an invalid sequence number in a SNACK.
     * <p>
     * All other values for Reason are reserved.
     */
    public static enum ReasonCode {
        /** Reserved. */
        RESERVED((byte)0x01),
        /** Data (payload) Digest Error. */
        DATA_DIGEST_ERROR((byte)0x02),
        /** SNACK Reject. */
        SNACK_REJECT((byte)0x03),
        /**
         * Protocol Error (e.g., SNACK request for a status that was already
         * acknowledged).
         */
        PROTOCOL_ERROR((byte)0x04),
        /** Command not supported. */
        COMMAND_NOT_SUPPORTED((byte)0x05),
        /** Immediate Command Reject - too many immediate commands. */
        IMMEDIATE_COMMAND_REJECT((byte)0x06),
        /** Task in progress. */
        TASK_IN_PROGRESS((byte)0x07),
        /** Invalid Data ACK. */
        INVALID_DATA_ACK((byte)0x08),
        /** Invalid PDU field. */
        INVALID_PDU_FIELD((byte)0x09),
        /**
         * Long Operation Reject - Can't generate Target Transfer Tag - out of
         * resources.
         */
        LONG_OPERATION_REJECT((byte)0x0A),
        /** Negotiation Reset. */
        NEGOTIATION_RESET((byte)0x0B),
        /** Waiting for Logout. */
        WAITING_FOR_LOGOUT((byte)0x0C);

        private final byte value;

        private static Map<Byte, ReasonCode> mapping;

        static {
            ReasonCode.mapping = new HashMap<Byte, ReasonCode>();
            for (ReasonCode s : values()) {
                ReasonCode.mapping.put(s.value, s);
            }
        }

        private ReasonCode(final byte newValue) {

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
        public static final ReasonCode valueOf(final byte value) {

            return ReasonCode.mapping.get(value);
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The Reason Code. */
    private ReasonCode reasonCode;

    /** The Data Sequence Number. */
    private int dataSequenceNumber;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty <code>RejectParser</code> object.
     * 
     * @param initProtocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>RejectParser</code> subclass object.
     */
    public RejectParser(final ProtocolDataUnit initProtocolDataUnit) {

        super(initProtocolDataUnit);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This field is only valid if the rejected PDU is a Data/R2T SNACK and the
     * Reject reasonCode code is "Protocol error" (see Section 10.16 SNACK
     * Request). The DataSN/R2TSN is the next Data/R2T sequence number that the
     * target would send for the task, if any.
     * 
     * @return The data sequence number of this <code>RejectParser</code> object.
     */
    public final int getDataSequenceNumber() {

        return dataSequenceNumber;
    }

    /**
     * Returns the reject reasonCode code of this <code>RejectParser</code> object.
     * 
     * @return The reasonCode code of this <code>RejectParser</code> object.
     */
    public final ReasonCode getReasonCode() {

        return reasonCode;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public final String toString() {

        final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

        Utils.printField(sb, "Reason", reasonCode.value, 1);
        sb.append(super.toString());
        Utils.printField(sb, "Data SN", dataSequenceNumber, 1);

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public final DataSegmentFormat getDataSegmentFormat() {

        return DataSegmentFormat.BINARY;
    }

    /** {@inheritDoc} */
    @Override
    public final void clear() {

        super.clear();

        reasonCode = null;
        dataSequenceNumber = 0x00000000;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes1to3(final int line) throws InternetSCSIException {

        Utils.isReserved(line & Constants.SECOND_BYTE_MASK);
        reasonCode =
            ReasonCode.valueOf((byte)((line & Constants.THIRD_BYTE_MASK) >> Constants.ONE_BYTE_SHIFT));
        Utils.isReserved(line & Constants.FOURTH_BYTE_MASK);

    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes36to39(final int line) throws InternetSCSIException {

        dataSequenceNumber = line;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void checkIntegrity() throws InternetSCSIException {

        String exceptionMessage;

        do {
            if (reasonCode != ReasonCode.PROTOCOL_ERROR && dataSequenceNumber != 0) {
                exceptionMessage =
                    "The DataSN/R2TSN is only valid, if the reason code is a 'Protocol Error'.";
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
    protected final int serializeBytes1to3() {

        return reasonCode.value << Constants.ONE_BYTE_SHIFT;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes36to39() {

        return dataSequenceNumber;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
