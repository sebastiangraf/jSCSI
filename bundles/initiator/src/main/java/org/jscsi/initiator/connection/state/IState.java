/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.initiator.connection.Connection;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.ProtocolDataUnitFactory;
import org.jscsi.parser.exception.InternetSCSIException;

/**
 * <h1>IState</h1> <p/> Each inherited state must implement this interface.
 * 
 * @author Volker Wildi
 */
public interface IState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This method is always invoked, when a request message must be send or
   * response message must be received and then parsed.
   * 
   * @throws InternetSCSIException
   *           if any violation of the iSCSI Standard (RfC3720) has emerged.
   */
  public void execute() throws InternetSCSIException;

  /**
   * This method shows if another state is following this one.
   * 
   * @return <code>true</code>, if another state must follow this state. Else an
   *         final state is reached.
   */
  public boolean nextStateFollowing();

  /**
   * This method checks the correctness of the given
   * <code>ProtocolDataUnit</code> instance within the current state of a
   * connection.
   * 
   * @param protocolDataUnit
   *          The <code>ProtocolDataUnit</code> instance to check.
   * @return <code>Exception</code> if any problem occured with the PDU,
   *         <code>null</code> otherwise.
   */
  public Exception isCorrect(final ProtocolDataUnit protocolDataUnit);

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

/**
 * <h1>AbstractState</h1> <p/> Each connection state must extend this abstract
 * class to support some basic features.
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

  /** boolean to mark following states.*/
  protected boolean stateFollowing;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public Exception isCorrect(final ProtocolDataUnit protocolDataUnit) {

    return null;
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
  
  /**
   * {@inheritDoc}
   */
  public boolean nextStateFollowing() {
    return this.stateFollowing;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
