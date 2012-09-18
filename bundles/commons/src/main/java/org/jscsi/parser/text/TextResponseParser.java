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
package org.jscsi.parser.text;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.Constants;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.TargetMessageParser;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.utils.Utils;

/**
 * <h1>TextResponseParser</h1>
 * <p>
 * This class parses a Text Response message defined in the iSCSI Standard (RFC3720).
 * 
 * @author Volker Wildi
 */
public final class TextResponseParser extends TargetMessageParser {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Continue Flag. */
    private boolean continueFlag;

    /** Target Transfer Tag. */
    private int targetTransferTag;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty <code>TextResponseParser</code> object.
     * 
     * @param initProtocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>TextResponseParser</code> subclass object.
     */
    public TextResponseParser(final ProtocolDataUnit initProtocolDataUnit) {

        super(initProtocolDataUnit);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public final String toString() {

        final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

        Utils.printField(sb, "Continue Flag", continueFlag, 1);
        Utils.printField(sb, "LUN", logicalUnitNumber, 1);
        Utils.printField(sb, "Target Transfer Tag", targetTransferTag, 1);
        sb.append(super.toString());

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public final DataSegmentFormat getDataSegmentFormat() {

        return DataSegmentFormat.TEXT;
    }

    /** {@inheritDoc} */
    @Override
    public final void clear() {

        super.clear();

        continueFlag = false;

        targetTransferTag = 0x00000000;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * When set to <code>1</code>, indicates that the text (set of key=value
     * pairs) in this Text Response is not complete (it will be continued on
     * subsequent Text Responses); otherwise, it indicates that this Text
     * Response ends a set of key=value pairs. A Text Response with the <code>C</code> bit set to
     * <code>1</code> MUST have the <code>F</code> bit
     * set to <code>0</code>.
     * 
     * @return <code>True</code>, if the Continue Flag is set. Else <code>false</code>.
     */
    public final boolean isContinueFlag() {

        return continueFlag;
    }

    /**
     * When a target has more work to do (e.g., cannot transfer all the
     * remaining text data in a single Text Response or has to continue the
     * negotiation) and has enough resources to proceed, it MUST set the Target
     * Transfer Tag to a value other than the reserved value of <code>0xffffffff</code>.
     * <p>
     * Otherwise, the Target Transfer Tag MUST be set to <code>0xffffffff</code> . When the Target Transfer
     * Tag is not <code>0xffffffff</code>, the <code>LUN</code> field may be significant. The initiator MUST
     * copy the Target Transfer Tag and <code>LUN</code> in its next request to indicate that it wants the
     * rest of the data. When the target receives a Text Request with the Target Transfer Tag set to the
     * reserved value of <code>0xffffffff</code>, it resets its internal information (resets state) associated
     * with the given Initiator Task Tag (restarts the negotiation). When a target cannot finish the operation
     * in a single Text Response, and does not have enough resources to continue, it rejects the Text Request
     * with the appropriate Reject code.
     * 
     * @return The Target Transfer Tag of this <code>TextResponseParser</code> object.
     */
    public final int getTargetTransferTag() {

        return targetTransferTag;
    }

    public final void setContinueFlag(boolean continueFlag) {
        this.continueFlag = continueFlag;
    }

    public final void setTargetTransferTag(int targetTransferTag) {
        this.targetTransferTag = targetTransferTag;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes1to3(final int line) throws InternetSCSIException {

        continueFlag = Utils.isBitSet(line & Constants.CONTINUE_FLAG_MASK);

        // all bits are reserved, except the continue flag bit
        Utils.isReserved(line & (Constants.LAST_THREE_BYTES_MASK ^ Constants.CONTINUE_FLAG_MASK));
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

        String exceptionMessage;

        do {
            if (protocolDataUnit.getBasicHeaderSegment().isFinalFlag() && continueFlag) {
                exceptionMessage = "Final and Continue Flag cannot be set at the same time.";
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

        int line = 0;

        if (continueFlag) {
            line = Constants.CONTINUE_FLAG_MASK;
        }

        return line;
    }

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
