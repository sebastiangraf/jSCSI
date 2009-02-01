/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * LogoutRequestState.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.initiator.connection.state;

import org.jscsi.initiator.connection.Connection;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.logout.LogoutRequestParser;
import org.jscsi.parser.logout.LogoutRequestParser.LogoutReasonCode;

/**
 * <h1>LogoutRequestState</h1> <p/> This state handles a Logout Request.
 * 
 * @author Volker Wildi
 */
public final class LogoutRequestState extends AbstractState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The reason for the logout request. */
  private final LogoutReasonCode reasonCode;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a <code>LogoutRequestState</code> instance, which
   * uses the given connection for transmission.
   * 
   * @param initConnection
   *          The context connection, which is used for the network
   *          transmission.
   * @param initReasonCode
   *          The reason code for the logout.
   */
  public LogoutRequestState(final Connection initConnection,
      final LogoutReasonCode initReasonCode) {

    super(initConnection);
    reasonCode = initReasonCode;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final void execute() throws InternetSCSIException {

    final ProtocolDataUnit protocolDataUnit = protocolDataUnitFactory.create(
        true, true, OperationCode.LOGOUT_REQUEST, connection
            .getSetting(OperationalTextKey.HEADER_DIGEST), connection
            .getSetting(OperationalTextKey.DATA_DIGEST));
    final LogoutRequestParser logoutRequest = (LogoutRequestParser) protocolDataUnit
        .getBasicHeaderSegment().getParser();

    logoutRequest.setReasonCode(reasonCode);
    if (reasonCode != LogoutReasonCode.CLOSE_SESSION) {
      logoutRequest.setConnectionID(connection.getConnectionID());
    }

    connection.send(protocolDataUnit);
    connection.nextState(new LogoutResponseState(connection));
    super.stateFollowing = true;
    // return true;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
