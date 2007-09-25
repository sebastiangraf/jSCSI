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
 * $Id: IState.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.connection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.ProtocolDataUnitFactory;
import org.jscsi.parser.exception.InternetSCSIException;

/**
 * <h1>IState</h1>
 * <p/>
 * 
 * Each inherited state must implement this interface.
 * 
 * @author Volker Wildi
 */
interface IState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This method is always invoked, when a request message must be send or
   * response message must be received and then parsed.
   * 
   * @return <code>true</code>, if another state must follow this state. Else
   *         an final state is reached.
   * @throws InternetSCSIException
   *           if any violation of the iSCSI Standard (RfC3720) has emerged.
   */
  public boolean execute() throws InternetSCSIException;

  /**
   * This method checks the correctness of the given
   * <code>ProtocolDataUnit</code> instance within the current state of a
   * connection.
   * 
   * @param protocolDataUnit
   *          The <code>ProtocolDataUnit</code> instance to check.
   * @return <code>true</code> if the given <code>ProtocolDataUnit</code>
   *         instance is correct. Else <code>false</code>.
   * @throws InternetSCSIException
   *           if any violation of the iSCSI Standard occurs.
   */
  public boolean isCorrect(final ProtocolDataUnit protocolDataUnit)
      throws InternetSCSIException;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

/**
 * <h1>AbstractState</h1>
 * <p/>
 * 
 * Each connection state must extend this abstract class to support some basic
 * features.
 * 
 * @author Volker Wildi
 */
abstract class AbstractState implements IState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The Logger interface. */
  protected static final Log LOGGER = LogFactory.getLog(AbstractState.class);

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The context connection used by all message kinds. */
  protected final Connection connection;

  /** Factory to create the <code>ProtocolDataUnit</code> instances. */
  protected final ProtocolDataUnitFactory protocolDataUnitFactory = new ProtocolDataUnitFactory();

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public boolean isCorrect(final ProtocolDataUnit protocolDataUnit)
      throws InternetSCSIException {

    // TODO: Add RejectParser check
    return true;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor, which each subclass must implement to set the context
   * connectioncorrectly.
   * 
   * @param initConnection
   *          The connection, which is used for the message transmission.
   */
  protected AbstractState(final Connection initConnection) {

    connection = initConnection;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
