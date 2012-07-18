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
package org.jscsi.initiator.connection.state;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.initiator.connection.Connection;
import org.jscsi.initiator.connection.phase.FullFeaturePhase;
import org.jscsi.initiator.connection.phase.LoginOperationalNegotiationPhase;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.DataSegmentFactory;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.datasegment.IDataSegment;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.login.LoginResponseParser;
import org.jscsi.parser.login.LoginStage;

/**
 * <h1>LoginResponseState</h1>
 * <p/>
 * This state handles a Login Response.
 * 
 * @author Volker Wildi
 */
public final class LoginResponseState extends AbstractState {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * The Next Stage of the login process, that the iSCSI Target wants to
     * transit to.
     */
    private final LoginStage nextStage;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a <code>LoginResponseState</code> instance, which
     * uses the given connection for transmission.
     * 
     * @param initConnection
     *            The context connection, which is used for the network
     *            transmission.
     * @param initNextStage
     *            The next stage to which should transfered to.
     */
    public LoginResponseState(final Connection initConnection, final LoginStage initNextStage) {

        super(initConnection);
        nextStage = initNextStage;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final void execute() throws InternetSCSIException {

        ProtocolDataUnit protocolDataUnit;
        final IDataSegment loginResponse =
            DataSegmentFactory.create(DataSegmentFormat.TEXT, connection
                .getSettingAsInt(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH));

        do {
            protocolDataUnit = connection.receive();

            if (!(protocolDataUnit.getBasicHeaderSegment().getParser() instanceof LoginResponseParser)) {
                break;
            }

            loginResponse.append(protocolDataUnit.getDataSegment(), protocolDataUnit.getBasicHeaderSegment()
                .getDataSegmentLength());
        } while (!protocolDataUnit.getBasicHeaderSegment().isFinalFlag());
        // extract Target Session Handle Identifying Handle
        final LoginResponseParser parser =
            (LoginResponseParser)protocolDataUnit.getBasicHeaderSegment().getParser();
        connection.getSession().setTargetSessionIdentifyingHandle(parser.getTargetSessionIdentifyingHandle());
        // Set the Expected Status Sequence Number to the initial value received
        // from the target. Then add the constant 2 to this value, because the
        // incrementExpectedStatusSequence() method was already invoked.
        connection.setExpectedStatusSequenceNumber(parser.getStatusSequenceNumber() + 1);// TODO was +2

        // check parameters....
        LOGGER.info("Retrieving these login parameters:\n" + loginResponse.getSettings());

        connection.update(loginResponse.getSettings());

        LOGGER.info("Updated settings to these:\n" + connection.getSettings());
        LOGGER.info("Nextstage is : " + nextStage);

        // is a transit to the next stage possible
        if (protocolDataUnit.getBasicHeaderSegment().isFinalFlag()) {
            if (nextStage == LoginStage.LOGIN_OPERATIONAL_NEGOTIATION) {
                connection.getSession().setPhase(new LoginOperationalNegotiationPhase());
            } else if (nextStage == LoginStage.FULL_FEATURE_PHASE) {
                connection.getSession().setPhase(new FullFeaturePhase());
                // return false;
            }
        }

        // return true;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public final Exception isCorrect(final ProtocolDataUnit protocolDataUnit) {

        if (protocolDataUnit.getBasicHeaderSegment().getParser() instanceof LoginResponseParser) {
            return null;
        } else {
            return new IllegalStateException(new StringBuilder("Parser ").append(
                protocolDataUnit.getBasicHeaderSegment().getParser().toString()).append(" is instance of ")
                .append(protocolDataUnit.getBasicHeaderSegment().getParser().getClass().toString()).append(
                    " and not instance if LoginParser!").toString());
        }

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
