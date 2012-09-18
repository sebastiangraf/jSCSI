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
import org.jscsi.parser.InitiatorMessageParser;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.utils.Utils;

/**
 * <h1>TextRequestParser</h1>
 * <p>
 * This class parses a Text Request message defined in the iSCSI Standard (RFC3720).
 * 
 * @author Volker Wildi
 */
public final class TextRequestParser extends InitiatorMessageParser {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Continue Flag. */
    private boolean continueFlag;

    /** Target Transfer Tag. */
    private int targetTransferTag;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty <code>TextRequestParser</code> object.
     * 
     * @param initProtocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>TextRequestParser</code> subclass object.
     */
    public TextRequestParser(final ProtocolDataUnit initProtocolDataUnit) {

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

    /**
     * When set to <code>1</code>, indicates that the text (set of key=value
     * pairs) in this Text Request is not complete (it will be continued on
     * subsequent Text Requests); otherwise, it indicates that this Text Request
     * ends a set of key=value pairs. A Text Request with the <code>C</code> bit
     * set to <code>1</code> MUST have the F bit set to <code>0</code>.
     * 
     * @return <code>True</code>, if the Continue Flag is set. Else <code>false</code>.
     */
    public final boolean isContinueFlag() {

        return continueFlag;
    }

    /**
     * When the <em>Target Transfer Tag</em> is set to the reserved value <code>0xffffffff</code>, it tells
     * the target that this is a new request
     * and the target resets any internal state associated with the Initiator
     * Task Tag (resets the current negotiation state). The target sets the
     * Target Transfer Tag in a text response to a value other than the reserved
     * value <code>0xffffffff</code> whenever it indicates that it has more data
     * to send or more operations to perform that are associated with the
     * specified Initiator Task Tag. It MUST do so whenever it sets the <code>F</code> bit to <code>0</code>
     * in the response. By copying the
     * Target Transfer Tag from the response to the next Text Request, the
     * initiator tells the target to continue the operation for the specific
     * Initiator Task Tag. The initiator MUST ignore the Target Transfer Tag in
     * the Text Response when the <code>F</code> bit is set to <code>1</code>.
     * This mechanism allows the initiator and target to transfer a large amount
     * of textual data over a sequence of text-command/text-response exchanges,
     * or to perform extended negotiation sequences. If the Target Transfer Tag
     * is not <code>0xffffffff</code>, the <code>LUN</code> field MUST be sent
     * by the target in the Text Response.
     * <p>
     * A target MAY reset its internal negotiation state if an exchange is stalled by the initiator for a long
     * time or if it is running out of resources. Long text responses are handled as in the following example:
     * I->T Text SendTargets=All (F=1,TTT=0xffffffff) T->I Text &lt;part 1&gt; (F=0,TTT=0x12345678) I->T Text
     * &lt;empty&gt; (F=1, TTT=0x12345678) T->I Text &lt;part 2&gt; (F=0, TTT=0x12345678) I->T Text
     * &lt;empty&gt; (F=1, TTT=0x12345678) ... T->I Text &lt;part n&gt; (F=1, TTT=0xffffffff)
     * 
     * @return The <em>Target Transfer Tag</em> of this <code>TextRequestParser</code> object.
     */
    public final int getTargetTransferTag() {

        return targetTransferTag;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes1to3(final int line) throws InternetSCSIException {

        continueFlag = Utils.isBitSet(line & Constants.CONTINUE_FLAG_MASK);
        Utils.isReserved(line & ~Constants.CONTINUE_FLAG_MASK);
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
            line |= Constants.CONTINUE_FLAG_MASK;
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
