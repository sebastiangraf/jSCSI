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
package org.jscsi.parser.r2t;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.Constants;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.TargetMessageParser;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.utils.Utils;

/**
 * This class parses a Ready2Transfer message defined in the iSCSI Standard
 * (RFC3720).
 * 
 * @author Volker Wildi
 */
public final class Ready2TransferParser extends TargetMessageParser {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The Target Transfer Tag. */
    private int targetTransferTag;

    /**
     * The Ready2Transfer Sequence Number.
     */
    private int ready2TransferSequenceNumber;

    /** The Buffer Offset. */
    private int bufferOffset;

    /** The Desired Data Transfer Length. */
    private int desiredDataTransferLength;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty <code>Ready2TransferParser</code> object.
     * 
     * @param initProtocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>Ready2TransferParser</code> subclass
     *            object.
     */
    public Ready2TransferParser(final ProtocolDataUnit initProtocolDataUnit) {

        super(initProtocolDataUnit);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public final String toString() {

        final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

        Utils.printField(sb, "LUN", logicalUnitNumber, 1);
        Utils.printField(sb, "TargetTransferTag", targetTransferTag, 1);
        sb.append(super.toString());
        Utils.printField(sb, "R2TSN", ready2TransferSequenceNumber, 1);
        Utils.printField(sb, "Buffer Offset", bufferOffset, 1);
        Utils.printField(sb, "Desired Data Transfer Length", desiredDataTransferLength, 1);

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

        targetTransferTag = 0x00000000;
        ready2TransferSequenceNumber = 0x00000000;
        bufferOffset = 0x00000000;
        desiredDataTransferLength = 0x00000000;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public final boolean incrementSequenceNumber() {

        return false;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * The target specifies how many bytes it wants the initiator to send
     * because of this R2T PDU. The target may request the data from the
     * initiator in several chunks, not necessarily in the original order of the
     * data. The target, therefore, also specifies a Buffer Offset that
     * indicates the point at which the data transfer should begin, relative to
     * the beginning of the total data transfer.
     * 
     * @return The buffer offset of this object.
     */
    public final int getBufferOffset() {

        return bufferOffset;
    }

    /**
     * The Desired Data Transfer Length MUST NOT be <code>0</code> and MUST not
     * exceed MaxBurstLength.
     * 
     * @return The desired data transfer length of this object.
     * @see #getBufferOffset
     */
    public final int getDesiredDataTransferLength() {

        return desiredDataTransferLength;
    }

    /**
     * R2TSN is the R2T PDU input PDU number within the command identified by
     * the Initiator Task Tag. <br/>
     * For bidirectional commands R2T and Data-In PDUs share the input PDU
     * numbering sequence (see Section 3.2.2.3 Data Sequencing).
     * 
     * @return The R2T Sequence Number of this object.
     */
    public final int getReady2TransferSequenceNumber() {

        return ready2TransferSequenceNumber;
    }

    /**
     * The target assigns its own tag to each R2T request that it sends to the
     * initiator. This tag can be used by the target to easily identify the data
     * it receives. The Target Transfer Tag and LUN are copied in the outgoing
     * data PDUs and are only used by the target. There is no protocol rule
     * about the Target Transfer Tag except that the value <code>0xffffffff</code> is reserved and MUST NOT be
     * sent by a target in
     * an R2T.
     * 
     * @return The target transfer tag of this object.
     */
    public final int getTargetTransferTag() {

        return targetTransferTag;
    }

    public final void setTargetTransferTag(int targetTransferTag) {
        this.targetTransferTag = targetTransferTag;
    }

    public final void setReady2TransferSequenceNumber(int ready2TransferSequenceNumber) {
        this.ready2TransferSequenceNumber = ready2TransferSequenceNumber;
    }

    public final void setBufferOffset(int bufferOffset) {
        this.bufferOffset = bufferOffset;
    }

    public final void setDesiredDataTransferLength(int desiredDataTransferLength) {
        this.desiredDataTransferLength = desiredDataTransferLength;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes1to3(final int line) throws InternetSCSIException {

        Utils.isReserved(line & Constants.LAST_THREE_BYTES_MASK);
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes20to23(final int line) throws InternetSCSIException {

        targetTransferTag = line;
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes36to39(final int line) throws InternetSCSIException {

        ready2TransferSequenceNumber = line;
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes40to43(final int line) throws InternetSCSIException {

        bufferOffset = line;
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes44to47(final int line) throws InternetSCSIException {

        desiredDataTransferLength = line;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void checkIntegrity() throws InternetSCSIException {

        String exceptionMessage;

        do {
            Utils.isReserved(protocolDataUnit.getBasicHeaderSegment().getDataSegmentLength());
            Utils.isReserved(protocolDataUnit.getBasicHeaderSegment().getTotalAHSLength());

            if (desiredDataTransferLength == 0) {
                exceptionMessage = "The DesiredDataTransferLength must not be equal than 0.";
                break;
            }

            if (targetTransferTag == 0xFFFFFFFF) {
                exceptionMessage = "The value 0xFFFFFFFF is reserved for the TargetTransferTag.";
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
    protected final int serializeBytes20to23() {

        return targetTransferTag;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes36to39() {

        return ready2TransferSequenceNumber;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes40to43() {

        return bufferOffset;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes44to47() {

        return desiredDataTransferLength;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
