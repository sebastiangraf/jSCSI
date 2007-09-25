/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id: LogoutResponseState.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.connection;

import java.io.IOException;

import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.logout.LogoutResponse;
import org.jscsi.parser.logout.LogoutResponseParser;

/**
 * <h1>LogoutResponseState</h1>
 * <p/>
 * 
 * This state handles a Logout Response.
 * 
 * @author Volker Wildi
 */
final class LogoutResponseState extends AbstractState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a <code>LogoutResponseState</code> instance, which
   * uses the given connection for transmission.
   * 
   * @param initConnection
   *          The context connection, which is used for the network
   *          transmission.
   */
  public LogoutResponseState(final Connection initConnection) {

    super(initConnection);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final boolean execute() throws InternetSCSIException {

    final ProtocolDataUnit protocolDataUnit = connection.receive();

    if (!(protocolDataUnit.getBasicHeaderSegment().getParser() instanceof LogoutResponseParser)) {
      throw new InternetSCSIException("This PDU type ("
          + protocolDataUnit.getBasicHeaderSegment().getParser()
          + ") is not expected. ");
    }

    final LogoutResponseParser parser = (LogoutResponseParser) protocolDataUnit
        .getBasicHeaderSegment().getParser();

    if (parser.getResponse() == LogoutResponse.CONNECTION_CLOSED_SUCCESSFULLY) {
      // exception rethrow
      try {
        // FIXME: Implement Connection close
        connection.getSession().close();
      } catch (IOException e) {
        throw new InternetSCSIException(
            "Closing the connection throws this error: "
                + e.getLocalizedMessage());
      }
    } else {
      throw new InternetSCSIException(
          "The connection could not be closed successfully.");
    }

    return false;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
