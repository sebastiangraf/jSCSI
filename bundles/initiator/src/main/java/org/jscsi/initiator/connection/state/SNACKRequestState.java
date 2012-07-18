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
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.snack.SNACKRequestParser;
import org.jscsi.parser.snack.SNACKRequestParser.SNACKType;

/**
 * <h1>SNACKRequestState</h1>
 * <p/>
 * This state handles a SNACK Request.
 * 
 * @author Volker Wildi
 */
public final class SNACKRequestState extends AbstractState {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    private final IState prevState;

    private final int targetTransferTag;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a new, empty <code>SNACKRequestState</code>.
     * 
     * @param initConnection
     *            This is the connection, which is used for the network
     *            transmission.
     * @param initPrevState
     *            The <code>IState</code> instance, which was executed before
     *            this state.
     * @param initTargetTransferTag
     *            The Target Transfer Tag of this state.
     */
    public SNACKRequestState(final Connection initConnection, final IState initPrevState,
        final int initTargetTransferTag) {

        super(initConnection);
        prevState = initPrevState;
        targetTransferTag = initTargetTransferTag;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final void execute() throws InternetSCSIException {

        final ProtocolDataUnit protocolDataUnit =
            protocolDataUnitFactory.create(false, true, OperationCode.SNACK_REQUEST, connection
                .getSetting(OperationalTextKey.HEADER_DIGEST), connection
                .getSetting(OperationalTextKey.DATA_DIGEST));

        final SNACKRequestParser parser =
            (SNACKRequestParser)protocolDataUnit.getBasicHeaderSegment().getParser();
        parser.setType(SNACKType.DATA_ACK);
        parser.setTargetTransferTag(targetTransferTag);

        connection.send(protocolDataUnit);
        connection.nextState(prevState);
        super.stateFollowing = true;
        // return true;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
