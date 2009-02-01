/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * SNACKRequestState.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.initiator.connection.state;

import org.jscsi.initiator.connection.Connection;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.snack.SNACKRequestParser;
import org.jscsi.parser.snack.SNACKRequestParser.SNACKType;

/**
 * <h1>SNACKRequestState</h1> <p/> This state handles a SNACK Request.
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
   *          This is the connection, which is used for the network
   *          transmission.
   * @param initPrevState
   *          The <code>IState</code> instance, which was executed before this
   *          state.
   * @param initTargetTransferTag
   *          The Target Transfer Tag of this state.
   */
  public SNACKRequestState(final Connection initConnection,
      final IState initPrevState, final int initTargetTransferTag) {

    super(initConnection);
    prevState = initPrevState;
    targetTransferTag = initTargetTransferTag;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final void execute() throws InternetSCSIException {

    final ProtocolDataUnit protocolDataUnit = protocolDataUnitFactory.create(
        false, true, OperationCode.SNACK_REQUEST, connection
            .getSetting(OperationalTextKey.HEADER_DIGEST), connection
            .getSetting(OperationalTextKey.DATA_DIGEST));

    final SNACKRequestParser parser = (SNACKRequestParser) protocolDataUnit
        .getBasicHeaderSegment().getParser();
    parser.setType(SNACKType.DATA_ACK);
    parser.setTargetTransferTag(targetTransferTag);

    connection.send(protocolDataUnit);
    connection.nextState(prevState);
    super.stateFollowing = true;
//    return true;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
