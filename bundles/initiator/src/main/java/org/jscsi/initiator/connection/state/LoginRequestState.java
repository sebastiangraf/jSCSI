/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

import java.util.LinkedList;
import java.util.Queue;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.initiator.connection.Connection;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.DataSegmentFactory;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.datasegment.IDataSegment;
import org.jscsi.parser.datasegment.IDataSegmentIterator;
import org.jscsi.parser.datasegment.IDataSegmentIterator.IDataSegmentChunk;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.datasegment.SettingsMap;
import org.jscsi.parser.login.ISID;
import org.jscsi.parser.login.LoginRequestParser;
import org.jscsi.parser.login.LoginStage;

/**
 * <h1>LoginRequestState</h1>
 * <p/>
 * This state handles a Login Request.
 * 
 * @author Volker Wildi
 */
public final class LoginRequestState extends AbstractState {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * The maximum version number, which is supported by this iSCSI
     * Implementation (RFC3720).
     */
    private static final byte MAXIMUM_VERSION = 0x00;

    /**
     * The minimum version number, which is supported by this iSCSI
     * Implementation (RFC3720).
     */
    private static final byte MINIMUM_VERSION = 0x00;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The Next Stage of the login process. */
    private final LoginStage nextStage;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a <code>LoginRequestState</code> instance, which
     * uses the given connection for transmission.
     * 
     * @param initConnection
     *            The context connection, which is used for the network
     *            transmission.
     * @param initNextStage
     *            The next stage to which should transfered to.
     */
    public LoginRequestState(final Connection initConnection,
            final LoginStage initNextStage) {

        super(initConnection);
        nextStage = initNextStage;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final void execute() throws InternetSCSIException {

        final SettingsMap loginParameters = connection.getSettings();
        LOGGER.info("Sending these login parameters:\n" + loginParameters);

        final int maxRecvDataSegmentLength = connection
                .getSettingAsInt(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH);
        final ISID isid = ISID.createRandom(System.currentTimeMillis());

        LoginRequestParser loginRequest;
        boolean continueFlag;
        // here the finalFlag represents the transitFlag
        boolean finalFlag;
        final IDataSegment dataSegment = DataSegmentFactory.create(
                loginParameters.asByteBuffer(), DataSegmentFormat.TEXT,
                maxRecvDataSegmentLength);
        final IDataSegmentIterator iterator = dataSegment.iterator();
        final Queue<ProtocolDataUnit> protocolDataUnits = new LinkedList<ProtocolDataUnit>();

        IDataSegmentChunk dataSegmentChunk;
        ProtocolDataUnit protocolDataUnit;
        int bytes2Transfer = dataSegment.getLength();

        while (bytes2Transfer > 0 && iterator.hasNext()) {

            if (bytes2Transfer <= maxRecvDataSegmentLength) {
                // last PDU to send...
                dataSegmentChunk = iterator.next(bytes2Transfer);
                continueFlag = false;
            } else {
                dataSegmentChunk = iterator.next(maxRecvDataSegmentLength);
                continueFlag = true;
            }

            finalFlag = !continueFlag;
            protocolDataUnit = protocolDataUnitFactory.create(true, finalFlag,
                    OperationCode.LOGIN_REQUEST, "None", "None");
            loginRequest = (LoginRequestParser) protocolDataUnit
                    .getBasicHeaderSegment().getParser();

            loginRequest.setContinueFlag(continueFlag);
            loginRequest.setCurrentStageNumber(connection.getSession()
                    .getPhase());
            LOGGER.debug("Phase:\n" + loginRequest.getCurrentStageNumber());
            if (finalFlag) {
                loginRequest.setNextStageNumber(nextStage);
            }
            loginRequest.setMaxVersion(MAXIMUM_VERSION);
            loginRequest.setMinVersion(MINIMUM_VERSION);

            loginRequest.setInitiatorSessionID(isid);
            loginRequest.setTargetSessionIdentifyingHandle(connection
                    .getSession().getTargetSessionIdentifyingHandle());

            protocolDataUnit.setDataSegment(dataSegmentChunk);

            protocolDataUnits.offer(protocolDataUnit);
            bytes2Transfer -= maxRecvDataSegmentLength;
        }

        connection.send(protocolDataUnits);
        connection.nextState(new LoginResponseState(connection, nextStage));
        super.stateFollowing = true;
        // return true;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
}
