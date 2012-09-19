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
package org.jscsi.parser;

import java.nio.ByteBuffer;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.utils.Utils;

/**
 * <h1>BasicHeaderSegment</h1>
 * <p>
 * This class encapsulate a Basic Header Segment (BHS), which is defined in the iSCSI Protocol (RFC3720). It
 * provides methods for serializing or deserializing such an object. The contained data can be accessed
 * seperately by the getter methods.
 * <p>
 * The BHS has a fixed size, which is stored in the variable <code>BHS_FIXED_SIZE</code>. And these must the
 * first bytes in a valid iSCSI Protocol Data Unit (PDU). org.jscsi.utils
 * 
 * @author Volker Wildi
 */
public final class BasicHeaderSegment {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The BHS has a fixed size of <code>48</code> bytes. */
    static final int BHS_FIXED_SIZE = 48;

    /** Offset of the byte <code>8</code> till <code>11</code> in the BHS. */
    static final int BYTES_8_11 = 8;

    /** Offset of the byte <code>16</code> till <code>19</code> in the BHS. */
    static final int BYTES_16_19 = 16;

    /** Offset of the byte <code>20</code> till <code>23</code> in the BHS. */
    static final int BYTES_20_23 = 20;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Bit mask to extract the immediate flag of a <code>32</code> bit number. */
    private static final int IMMEDIATE_FLAG_MASK = 0x40000000;

    /** Bit mask to extract the operation code of a <code>32</code> bit number. */
    private static final int OPERATION_CODE_MASK = 0x3F000000;

    /** Bit mask to extract the final flag of a <code>32</code> bit number. */
    private static final int FINAL_FLAG_MASK = 0x00800000;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * For request PDUs, the <code>I</code> bit set to <code>1</code> is an
     * immediate delivery marker.
     */
    private boolean immediateFlag;

    /**
     * The operation code indicates the type of iSCSI PDU the header
     * encapsulates. The operation codes are divided into two categories: <em>initiator</em> opcodes and
     * <em>target</em> opcodes. Initiator opcodes
     * are in PDUs sent by the initiator (request PDUs). Target opcodes are in
     * PDUs sent by the target (response PDUs).
     * <p>
     * Initiators MUST NOT use target opcodes and targets MUST NOT use initiator opcodes.
     */
    private OperationCode operationCode;

    /**
     * When set to <code>1</code> it indicates the final (or only) PDU of a
     * sequence.
     */
    private boolean finalFlag;

    /**
     * Total length of all AHS header segments in units of <code>four</code> byte words including padding, if
     * any.
     * <p>
     * The <code>TotalAHSLength</code> is only used in PDUs that have an AHS and MUST be <code>0</code> in all
     * other PDUs.
     */
    private byte totalAHSLength;

    /**
     * This is the data segment payload length in bytes (excluding padding). The
     * DataSegmentLength MUST be <code>0</code> whenever the PDU has no data
     * segment.
     */
    private int dataSegmentLength;

    /**
     * The initiator assigns a Task Tag to each iSCSI task it issues. While a
     * task exists, this tag MUST uniquely identify the task session-wide. SCSI
     * may also use the initiator task tag as part of the SCSI task identifier
     * when the timespan during which an iSCSI initiator task tag must be unique
     * extends over the timespan during which a SCSI task tag must be unique.
     * However, the iSCSI Initiator Task Tag must exist and be unique even for
     * untagged SCSI commands.
     */
    private int initiatorTaskTag;

    /** The used parser for the messages. */
    private AbstractMessageParser parser;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Default constructor, creates new, empty BasicHeaderSegment object. */
    BasicHeaderSegment() {

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This method serializes the informations of this BHS object to the byte
     * representation defined by the iSCSI Standard.
     * 
     * @param dst
     *            The destination array to write in.
     * @param offset
     *            The start offset in <code>dst</code>.
     * @return The length (in bytes) of the serialized form of this BHS object.
     * @throws InternetSCSIException
     *             If any violation of the iSCSI-Standard emerge.
     */
    final int serialize(final ByteBuffer dst, final int offset) throws InternetSCSIException {

        // has the destination array enough space to store this basic header
        // segment
        dst.position(offset);
        if (dst.remaining() < BHS_FIXED_SIZE) {
            throw new IllegalArgumentException("Destination array is too small.");
        }

        int line = 0;
        if (immediateFlag) {
            line |= IMMEDIATE_FLAG_MASK;
        }

        line |= operationCode.value() << Constants.THREE_BYTES_SHIFT;

        if (finalFlag) {
            line |= FINAL_FLAG_MASK;
        }
        dst.putInt(line);

        dst.putInt(dataSegmentLength | (totalAHSLength << Constants.THREE_BYTES_SHIFT));
        dst.putInt(BYTES_16_19, initiatorTaskTag);

        parser.serializeBasicHeaderSegment(dst, offset);

        return BHS_FIXED_SIZE;
    }

    /**
     * Extract from the given Protocol Data Unit the BHS. After an successful
     * extraction this methods and setreturns the right message parser object
     * for this kind of message.
     * 
     * @param protocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>BasicHeaderSegment</code> object.
     * @param src
     *            The bytes representation of a Protocol Data Unit.
     * @return The length (in bytes), which are read from <code>pdu</code>.
     * @throws InternetSCSIException
     *             If any violation of the iSCSI-Standard emerge.
     */
    final int deserialize(final ProtocolDataUnit protocolDataUnit, final ByteBuffer src)
        throws InternetSCSIException {

        if (src.remaining() < BHS_FIXED_SIZE) {
            throw new InternetSCSIException("This Protocol Data Unit does not contain"
                + " an valid Basic Header Segment.");
        }

        final int firstLine = src.getInt();
        immediateFlag = Utils.isBitSet(firstLine & IMMEDIATE_FLAG_MASK);
        final int code = (firstLine & OPERATION_CODE_MASK) >> Constants.THREE_BYTES_SHIFT;
        operationCode = OperationCode.valueOf((byte)code);
        finalFlag = Utils.isBitSet(firstLine & FINAL_FLAG_MASK);

        totalAHSLength = src.get();
        dataSegmentLength = Utils.getUnsignedInt(src.get()) << Constants.TWO_BYTES_SHIFT;
        dataSegmentLength += Utils.getUnsignedInt(src.get()) << Constants.ONE_BYTE_SHIFT;
        dataSegmentLength += Utils.getUnsignedInt(src.get());

        initiatorTaskTag = src.getInt(BYTES_16_19);

        src.rewind();

        parser = MessageParserFactory.getParser(protocolDataUnit, operationCode);
        parser.deserializeBasicHeaderSegment(src);

        return BHS_FIXED_SIZE;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * The Length of the Data Segment.
     * 
     * @return length of the data segment
     */
    public final int getDataSegmentLength() {

        return dataSegmentLength;
    }

    /**
     * When this flag is set it indicates the final (or only) PDU of a sequence.
     * In some kinds of messages (PDU) this methods has the meaning of the <code>TransitFlag</code>.
     * 
     * @return The state of the final flag.
     */
    public final boolean isFinalFlag() {

        return finalFlag;
    }

    /**
     * For request PDUs, the immediate flag can be set as an immediate delivery
     * marker.
     * 
     * @return The state of the immediate flag.
     */
    public final boolean isImmediateFlag() {

        return immediateFlag;
    }

    /**
     * The initiator assigns a Task Tag to each iSCSI task it issues. While a
     * task exists, this tag MUST uniquely identify the task session-wide. SCSI
     * may also use the initiator task tag as part of the SCSI task identifier
     * when the timespan during which an iSCSI initiator task tag must be unique
     * extends over the timespan during which a SCSI task tag must be unique.
     * However, the iSCSI Initiator Task Tag must exist and be unique even for
     * untagged SCSI commands.
     * 
     * @return the initiator task tag.
     */
    public final int getInitiatorTaskTag() {

        return initiatorTaskTag;
    }

    /**
     * The length of the Additional Header Segment.
     * 
     * @return The length of the contained AHSs
     */
    public final byte getTotalAHSLength() {

        return totalAHSLength;
    }

    /**
     * Returns the operation code, which is used in this BHS.
     * 
     * @return The operation code of this BHS.
     */
    public final OperationCode getOpCode() {

        return operationCode;
    }

    /**
     * Returns a object of the used parser of this BHS.
     * 
     * @return The parser object to use for this PDU.
     */
    public final AbstractMessageParser getParser() {

        return parser;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Set a new length for the data segment.
     * 
     * @param initDataSegmentLength
     *            The new length of this BasicHeaderSegment object.
     */
    final void setDataSegmentLength(final int initDataSegmentLength) {

        dataSegmentLength = initDataSegmentLength;
    }

    /**
     * Changes the state of the final flag.
     * 
     * @param initFinalFlag
     *            The new state of the final flag.
     */
    final void setFinal(final boolean initFinalFlag) {

        finalFlag = initFinalFlag;
    }

    /**
     * Changes the state of the immediate flag.
     * 
     * @param initImmediateFlag
     *            The new state of the immediate flag.
     */
    final void setImmediate(final boolean initImmediateFlag) {

        immediateFlag = initImmediateFlag;
    }

    /**
     * Changes the value of the initiator task tag.
     * 
     * @param initInitiatorTaskTag
     *            The new value of the initiator task tag.
     */
    public final void setInitiatorTaskTag(final int initInitiatorTaskTag) {

        // FIXME: Change to allow fixed values
        initiatorTaskTag = initInitiatorTaskTag;
    }

    /**
     * This sets the length (in units of four bytes) of the total length of the
     * given AHS.
     * 
     * @param initTotalAHSLength
     *            The new length.
     */
    final void setTotalAHSLength(final byte initTotalAHSLength) {

        totalAHSLength = initTotalAHSLength;
    }

    /**
     * Set a new operation code for this BHS object.
     * 
     * @param protocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>BasicHeaderSegment</code> object.
     * @param initOperationCode
     *            The new operation code.
     */
    final void
        setOperationCode(final ProtocolDataUnit protocolDataUnit, final OperationCode initOperationCode) {

        operationCode = initOperationCode;
        parser = MessageParserFactory.getParser(protocolDataUnit, initOperationCode);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Creates a string object with all values for easy debugging.
     * 
     * @return The string with all informations of this BHS.
     */
    public final String toString() {

        if (parser == null)
            return "Empty parser";

        final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

        Utils.printField(sb, "ParserClass", parser.getClass().getSimpleName(), 1);

        Utils.printField(sb, "ImmediateFlag", immediateFlag, 1);
        Utils.printField(sb, "OpCode", operationCode.value(), 1);
        Utils.printField(sb, "FinalFlag", finalFlag, 1);
        Utils.printField(sb, "TotalAHSLength", totalAHSLength, 1);
        Utils.printField(sb, "DataSegmentLength", dataSegmentLength, 1);
        Utils.printField(sb, "InitiatorTaskTag", initiatorTaskTag, 1);

        sb.append(parser.toString());

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    final public boolean equals(Object o) {
        if (o instanceof BasicHeaderSegment == false)
            return false;

        BasicHeaderSegment oBhs = (BasicHeaderSegment)o;

        if (oBhs.isFinalFlag() == this.isFinalFlag() && oBhs.isImmediateFlag() == this.isImmediateFlag()
            && oBhs.getParser().getClass() == this.getParser().getClass()
            && oBhs.getDataSegmentLength() == this.getDataSegmentLength()
            && oBhs.getInitiatorTaskTag() == this.getInitiatorTaskTag()
            && oBhs.getOpCode().compareTo(this.getOpCode()) == 0
            && oBhs.getTotalAHSLength() == this.getTotalAHSLength())
            return true;

        return false;
    }

    /**
     * Clears all the stored content of this BasicHeaderSegment object.
     */
    final void clear() {

        immediateFlag = false;
        operationCode = OperationCode.LOGIN_REQUEST;
        finalFlag = false;

        totalAHSLength = 0x00;
        dataSegmentLength = 0x00000000;
        initiatorTaskTag = 0x00000000;

        parser = null;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
