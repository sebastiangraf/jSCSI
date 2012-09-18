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
import org.jscsi.parser.InitiatorMessageParser;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.utils.Utils;

/**
 * <h1>NOPOutParser</h1>
 * <p>
 * This class parses a NOP-Out message defined in the iSCSI Standard (RFC3720).
 * <p>
 * A NOP-Out may be used by an initiator as a "ping request" to verify that a connection/session is still
 * active and all its components are operational. The NOP-In response is the "ping echo".
 * <p>
 * A NOP-Out is also sent by an initiator in response to a NOP-In.
 * <p>
 * A NOP-Out may also be used to confirm a changed <code>ExpStatSN</code> if another PDU will not be available
 * for a long time.
 * <p>
 * Upon receipt of a NOP-In with the Target Transfer Tag set to a valid value (not the reserved
 * <code>0xffffffff</code>), the initiator MUST respond with a NOP-Out. In this case, the NOP-Out Target
 * Transfer Tag MUST contain a copy of the NOP-In Target Transfer Tag.
 * <p>
 * These fields have these specific meanings: <blockquote> <b>Initiator Task Tag:</b><br/>
 * The NOP-Out MUST have the Initiator Task Tag set to a valid value only if a response in the form of NOP-In
 * is requested (i.e., the NOP-Out is used as a ping request). Otherwise, the Initiator Task Tag MUST be set
 * to <code>0xffffffff</code>.
 * <p>
 * When a target receives the NOP-Out with a valid Initiator Task Tag, it MUST respond with a Nop-In Response
 * (see Section 10.19 NOP-In).
 * <p>
 * If the Initiator Task Tag contains <code>0xffffffff</code>, the <code>I bit</code> MUST be set to
 * <code>1</code> and the <code>CmdSN</code> is not advanced after this PDU is sent.
 * <p>
 * <b>Ping Data</b><br/>
 * Ping data are reflected in the NOP-In Response. The length of the reflected data are limited to
 * <code>MaxRecvDataSegmentLength</code>. The length of ping data are indicated by the
 * <code>DataSegmentLength</code>. <code>0</code> is a valid value for the <code>DataSegmentLength</code> and
 * indicates the absence of ping data. </blockquote>
 * 
 * @author Volker Wildi
 */
public final class NOPOutParser extends InitiatorMessageParser {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The TargetTransferTag. */
    protected int targetTransferTag;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty <code>NOPOutParser</code> object.
     * 
     * @param initProtocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>NOPOutParser</code> subclass object.
     */
    public NOPOutParser(final ProtocolDataUnit initProtocolDataUnit) {

        super(initProtocolDataUnit);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * A target assigned identifier for the operation.
     * <p>
     * The NOP-Out MUST only have the Target Transfer Tag set if it is issued in response to a NOP-In with a
     * valid Target Transfer Tag. In this case, it copies the Target Transfer Tag from the NOP-In PDU.
     * Otherwise, the Target Transfer Tag MUST be set to <code>0xffffffff</code>.
     * <p>
     * When the Target Transfer Tag is set to a value other than <code>0xffffffff</code>, the LUN field MUST
     * also be copied from the NOP-In.
     * 
     * @return The target transfer tag of this object.
     */
    public final int getTargetTransferTag() {

        return targetTransferTag;
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
