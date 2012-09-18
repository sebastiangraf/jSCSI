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
package org.jscsi.parser.nop;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.Constants;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.TargetMessageParser;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.utils.Utils;

/**
 * <h1>NOPInParser</h1>
 * <p>
 * This class parses a NOP-In message defined in the iSCSI Standard (RFC3720).
 * <p>
 * NOP-In is either sent by a target as a response to a NOP-Out, as a "ping" to an initiator, or as a means to
 * carry a changed <code>ExpCmdSN</code> and/or <code>MaxCmdSN</code> if another PDU will not be available for
 * a long time (as determined by the target).
 * <p>
 * When a target receives the NOP-Out with a valid Initiator Task Tag (not the reserved value
 * <code>0xffffffff</code>), it MUST respond with a NOP-In with the same Initiator Task Tag that was provided
 * in the NOP-Out request. It MUST also duplicate up to the first <code>MaxRecvDataSegmentLength</code> bytes
 * of the initiator provided Ping Data. For such a response, the Target Transfer Tag MUST be
 * <code>0xffffffff</code>.
 * <p>
 * Otherwise, when a target sends a NOP-In that is not a response to a Nop-Out received from the initiator,
 * the Initiator Task Tag MUST be set to <code>0xffffffff</code> and the Data Segment MUST NOT contain any
 * data (DataSegmentLength MUST be <code>0</code>).
 * <p>
 * <b>This fields have these specific meanings:</b>
 * <p>
 * A LUN MUST be set to a correct value when the Target Transfer Tag is valid (not the reserved value
 * <code>0xffffffff</code>).
 * <p>
 * The StatSN field will always contain the next StatSN. However, when the Initiator Task Tag is set to
 * <code>0xffffffff</code>, StatSN for the connection is not advanced after this PDU is sent.
 * <p>
 * 
 * @author Volker Wildi
 */
public final class NOPInParser extends TargetMessageParser {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Target Transfer Tag. */
    private int targetTransferTag;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty <code>NOPInParser</code> object.
     * 
     * @param initProtocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>NOPInParser</code> subclass object.
     */
    public NOPInParser(final ProtocolDataUnit initProtocolDataUnit) {

        super(initProtocolDataUnit);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * If the target is responding to a NOP-Out, this is set to the reserved
     * value <code>0xffffffff</code>.
     * <p>
     * If the target is sending a NOP-In as a Ping (intending to receive a corresponding NOP-Out), this field
     * is set to a valid value (not the reserved <code>0xffffffff</code>).
     * <p>
     * If the target is initiating a NOP-In without wanting to receive a corresponding NOP-Out, this field
     * MUST hold the reserved value of <code>0xffffffff</code>.
     * 
     * @return The target transfer tag of this object.
     */
    public final int getTargetTransferTag() {

        return targetTransferTag;
    }

    /**
     * Sets the Target Transfer Tag of this object.
     * 
     * @param targetTransferTag
     *            the new Target Transfer Tag.
     */
    public final void setTargetTransferTag(final int targetTransferTag) {
        this.targetTransferTag = targetTransferTag;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public final String toString() {

        final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);
        Utils.printField(sb, "LUN", logicalUnitNumber, 1);
        Utils.printField(sb, "Target Transfer Tag", targetTransferTag, 1);
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

        targetTransferTag = 0x00000000;
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

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void checkIntegrity() throws InternetSCSIException {

        // do nothing...
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final int serializeBytes20to23() {

        return targetTransferTag;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
