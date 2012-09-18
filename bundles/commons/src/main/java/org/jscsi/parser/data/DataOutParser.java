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
import org.jscsi.parser.InitiatorMessageParser;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.utils.Utils;

/**
 * This class parses a Data-Out message defined in the iSCSI Standard (RFC3720).
 * 
 * @author Volker Wildi
 */
public final class DataOutParser extends InitiatorMessageParser {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Data Sequence Number. */
    private int dataSequenceNumber;

    /** Buffer Offset. */
    private int bufferOffset;

    /** Target Transfer Tag. */
    private int targetTransferTag;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty <code>DataOutParser</code> object.
     * 
     * @param initProtocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>DataOutParser</code> subclass object.
     */
    public DataOutParser(final ProtocolDataUnit initProtocolDataUnit) {

        super(initProtocolDataUnit);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the Command Sequence Number of this <code>InitiatorMessageParser</code> object.
     * <p/>
     * This field does not have a Command Sequence Number unequal <code>0</code>.
     * 
     * @param newCommandSequenceNumber
     *            This number is ignored.
     */
    public final void setCommandSequenceNumber(final int newCommandSequenceNumber) {

        commandSequenceNumber = Constants.RESERVED_INT;
    }

    /**
     * The Buffer Offset field contains the offset of this PDU payload data
     * within the complete data transfer. The sum of the buffer offset and
     * length should not exceed the expected transfer length for the command.<br/>
     * The order of data PDUs within a sequence is determined by DataPDUInOrder.
     * When set to Yes, it means that PDUs have to be in increasing Buffer
     * Offset order and overlays are forbidden. <br/>
     * The ordering between sequences is determined by DataSequenceInOrder. When
     * set to Yes, it means that sequences have to be in increasing Buffer
     * Offset order and overlays are forbidden.
     * 
     * @return The Buffer Offset of this DataOutParser object.
     */
    public final int getBufferOffset() {

        return bufferOffset;
    }

    /**
     * For input (read) or bidirectional Data-In PDUs, the DataSN is the input
     * PDU number within the data transfer for the command identified by the
     * Initiator Task Tag.
     * <p>
     * R2T and Data-In PDUs, in the context of bidirectional commands, share the numbering sequence (see
     * Section 3.2.2.3 Data Sequencing).
     * <p>
     * For output (write) data PDUs, the DataSN is the Data-Out PDU number within the current output sequence.
     * The current output sequence is either identified by the Initiator Task Tag (for unsolicited data) or is
     * a data sequence generated for one R2T (for data solicited through R2T).
     * 
     * @return The Data Sequence Number of this DataOutParser object.
     */
    public final int getDataSequenceNumber() {

        return dataSequenceNumber;
    }

    /**
     * On outgoing data, the Target Transfer Tag is provided to the target if
     * the transfer is honoring an R2T. In this case, the Target Transfer Tag
     * field is a replica of the Target Transfer Tag provided with the R2T.
     * <p>
     * On incoming data, the Target Transfer Tag and LUN MUST be provided by the target if the A bit is set to
     * 1; otherwise they are reserved. The Target Transfer Tag and LUN are copied by the initiator into the
     * SNACK of type DataACK that it issues as a result of receiving a SCSI Data-In PDU with the A bit set to
     * <code>1</code>.
     * <p>
     * The Target Transfer Tag values are not specified by this protocol except that the value
     * <code>0xffffffff</code> is reserved and means that the Target Transfer Tag is not supplied. If the
     * Target Transfer Tag is provided, then the LUN field MUST hold a valid value and be consistent with
     * whatever was specified with the command; otherwise, the LUN field is reserved.
     * 
     * @return The Target Transfer Tag of this DataOutParser object.
     */
    public final int getTargetTransferTag() {

        return targetTransferTag;
    }

    /**
     * Sets the Buffer Offset to a new one.
     * 
     * @param newBufferOffset
     *            The new Buffer Offset.
     * @see #getBufferOffset()
     */
    public final void setBufferOffset(final int newBufferOffset) {

        bufferOffset = newBufferOffset;
    }

    /**
     * Sets the Data Sequence Number to a new one.
     * 
     * @param newDataSequenceNumber
     *            The new Data Sequence Number.
     * @see #getDataSequenceNumber()()
     */
    public final void setDataSequenceNumber(final int newDataSequenceNumber) {

        dataSequenceNumber = newDataSequenceNumber;
    }

    /**
     * Sets the Target Transfer Tag to a new one.
     * 
     * @param newTargetTransferTag
     *            The new Target Transfer Tag.
     * @see #getTargetTransferTag()
     */
    public final void setTargetTransferTag(final int newTargetTransferTag) {

        targetTransferTag = newTargetTransferTag;
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
    public final String toString() {

        final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

        Utils.printField(sb, "LUN", logicalUnitNumber, 1);
        Utils.printField(sb, "Target Transfer Tag", targetTransferTag, 1);
        Utils.printField(sb, "ExpStatSN", expectedStatusSequenceNumber, 1);
        Utils.printField(sb, "DataSN", dataSequenceNumber, 1);
        Utils.printField(sb, "Buffer Offset", bufferOffset, 1);
        sb.append(super.toString());

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

        dataSequenceNumber = 0x00000000;
        bufferOffset = 0x00000000;
        targetTransferTag = 0x00000000;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public boolean incrementSequenceNumber() {

        return false;
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

        dataSequenceNumber = line;
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes40to43(final int line) throws InternetSCSIException {

        bufferOffset = line;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void checkIntegrity() throws InternetSCSIException {

        Utils.isReserved(commandSequenceNumber);
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
    protected final int serializeBytes24to27() {

        return 0;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes36to39() {

        return dataSequenceNumber;
    }

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes40to43() {

        return bufferOffset;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
