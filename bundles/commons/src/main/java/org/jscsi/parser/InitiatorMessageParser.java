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

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.utils.Utils;

/**
 * <h1>InitiatorMessageParser</h1>
 * <p>
 * This abstract class is the base class of all initiator message parsers defined in the iSCSI Protocol
 * (RFC3720). This class defines some methods, which are common in all parsers to simplify the parsing
 * process.
 * 
 * @author Volker Wildi
 */
public abstract class InitiatorMessageParser extends AbstractMessageParser {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Command Sequence Number.
     * <p>
     * Enables ordered delivery across multiple connections in a single session.
     */
    protected int commandSequenceNumber;

    /**
     * Expected Status Sequence Number.
     * <p>
     * Command responses up to <code>expectedStatusSequenceNumber - 1 (mod 2**32)</code> have been received
     * (acknowledges status) on the connection.
     */
    protected int expectedStatusSequenceNumber;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty LogoutRequestParser object.
     * 
     * @param initProtocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>InitiatorMessageParser</code> object.
     */
    public InitiatorMessageParser(final ProtocolDataUnit initProtocolDataUnit) {

        super(initProtocolDataUnit);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public String getShortInfo() {

        return "-> " + getClass().getSimpleName() + ": cmdSN: " + getCommandSequenceNumber()
            + ", expStatSN: " + getExpectedStatusSequenceNumber();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

        Utils.printField(sb, "CommandSequenceNumber", commandSequenceNumber, 1);
        Utils.printField(sb, "ExpectedStatusSequenceNumber", expectedStatusSequenceNumber, 1);

        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public void clear() {

        super.clear();

        commandSequenceNumber = 0;
        expectedStatusSequenceNumber = 0;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This <code>AbstractMessageParser</code> instance affects the
     * incrementation of the <code>Command Sequence Number</code>.
     * 
     * @return <code>true</code>, if the counter has to be incremented. Else <code>false</code>.
     */
    @Override
    public boolean incrementSequenceNumber() {

        return !protocolDataUnit.getBasicHeaderSegment().isImmediateFlag();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the Command Sequence Number of this <code>InitiatorMessageParser</code> object.
     * 
     * @return The Command Sequence Number.
     */
    public final int getCommandSequenceNumber() {

        return commandSequenceNumber;
    }

    /**
     * Returns the Expected Status Sequence Number of this <code>InitiatorMessageParser</code> object.
     * 
     * @return The Expected Status Sequence Number.
     */
    public final int getExpectedStatusSequenceNumber() {

        return expectedStatusSequenceNumber;
    }

    /**
     * Sets the Command Sequence Number of this <code>InitiatorMessageParser</code> object to the given value.
     * 
     * @param initCmdSN
     *            The new Command Sequence Number.
     */
    public void setCommandSequenceNumber(final int initCmdSN) {

        commandSequenceNumber = initCmdSN;
    }

    /**
     * Sets the Expected Status Sequence Number of this <code>InitiatorMessageParser</code> object to the
     * given value.
     * 
     * @param initExpStatSN
     *            The new Expected Status Sequence Number.
     */
    public final void setExpectedStatusSequenceNumber(final int initExpStatSN) {

        expectedStatusSequenceNumber = initExpStatSN;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected void deserializeBytes1to3(final int line) throws InternetSCSIException {

        Utils.isReserved(line);
    }

    /** {@inheritDoc} */
    @Override
    protected void deserializeBytes20to23(final int line) throws InternetSCSIException {

        Utils.isReserved(line);
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes24to27(final int line) throws InternetSCSIException {

        commandSequenceNumber = line;
    }

    /** {@inheritDoc} */
    @Override
    protected final void deserializeBytes28to31(final int line) throws InternetSCSIException {

        expectedStatusSequenceNumber = line;
    }

    /** {@inheritDoc} */
    @Override
    protected void deserializeBytes32to35(final int line) throws InternetSCSIException {

        Utils.isReserved(line);
    }

    /** {@inheritDoc} */
    @Override
    protected void deserializeBytes36to39(final int line) throws InternetSCSIException {

        Utils.isReserved(line);
    }

    /** {@inheritDoc} */
    @Override
    protected void deserializeBytes40to43(final int line) throws InternetSCSIException {

        Utils.isReserved(line);
    }

    /** {@inheritDoc} */
    @Override
    protected void deserializeBytes44to47(final int line) throws InternetSCSIException {

        Utils.isReserved(line);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    protected int serializeBytes1to3() {

        return Constants.RESERVED_INT;
    }

    /** {@inheritDoc} */
    @Override
    protected int serializeBytes20to23() {

        return Constants.RESERVED_INT;
    }

    /** {@inheritDoc} */
    @Override
    protected int serializeBytes24to27() {

        return commandSequenceNumber;
    }

    /** {@inheritDoc} */
    @Override
    protected int serializeBytes28to31() {

        return expectedStatusSequenceNumber;
    }

    /** {@inheritDoc} */
    @Override
    protected int serializeBytes32to35() {

        return Constants.RESERVED_INT;
    }

    /** {@inheritDoc} */
    @Override
    protected int serializeBytes36to39() {

        return Constants.RESERVED_INT;
    }

    /** {@inheritDoc} */
    @Override
    protected int serializeBytes40to43() {

        return Constants.RESERVED_INT;
    }

    /** {@inheritDoc} */
    @Override
    protected int serializeBytes44to47() {

        return Constants.RESERVED_INT;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
