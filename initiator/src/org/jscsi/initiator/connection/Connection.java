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
 * $Id: Connection.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.initiator.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.core.utils.SerialArithmeticNumber;
import org.jscsi.initiator.Configuration;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.datasegment.SettingsMap;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.exception.NoSuchSessionException;
import org.jscsi.parser.exception.OperationalTextKeyException;
import org.jscsi.parser.login.LoginStage;

/**
 * <h1>Connection</h1>
 * <p/> This class represents a connection, which is used in the iSCSI Standard
 * (RFC3720). Such a connection is directed from the initiator to the target. It
 * is used in Sessions.
 * 
 * @author Volker Wildi
 * @see org.jscsi.connection.Session
 */
public final class Connection {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The logger interface. */
  private static final Log LOGGER = LogFactory.getLog(Connection.class);

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * The <code>Session</code> instance, which contains this
   * <code>Connection</code> instance.
   */
  private final Session referenceSession;

  /** The <code>Configuration</code> instance for this connection. */
  private final Configuration configuration;

  /** The current state of this connection. */
  private IState state;

  /**
   * The ID of this connection. This must be unique within a
   * <code>Session</code>.
   */
  private final short connectionID;

  /**
   * The Expected Status Sequence Number, which is expected to received from the
   * target within this connection.
   */
  private final SerialArithmeticNumber expectedStatusSequenceNumber;

  /**
   * The sending queue for the <code>ProtocolDataUnit</code>s, which have to
   * be sent.
   */
  private final Queue<ProtocolDataUnit> sendingQueue;

  /**
   * The receiving queue of the <code>ProtocolDataUnit</code>s, which are
   * received.
   */
  private final Queue<ProtocolDataUnit> receivingQueue;

  /**
   * The worker caller, which handles the transmission of the packages over the
   * network.
   */
  private final SenderWorker workerThread;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new, empty Connection with the given ID.
   * 
   * @param session
   *          Reference to the <code>Session</code> object, which contains
   *          this connection.
   * @param initConfiguration
   *          The configuration to use within this connection.
   * @param inetAddress
   *          The <code>InetAddress</code> to which this connection should
   *          established.
   * @param port
   *          The TCP port of the listening <code>Socket</code>.
   * @param initConnectionID
   *          The ID of this connection.
   * @throws Exception
   *           if any error occurs.
   */
  public Connection(final Session session,
      final Configuration initConfiguration, final InetAddress inetAddress,
      final int port, final short initConnectionID) throws Exception {

    configuration = initConfiguration;
    connectionID = initConnectionID;

    referenceSession = session;

    sendingQueue = new LinkedList<ProtocolDataUnit>();
    receivingQueue = new LinkedList<ProtocolDataUnit>();
    expectedStatusSequenceNumber = new SerialArithmeticNumber();

    state = new LoginRequestState(this, LoginStage.FULL_FEATURE_PHASE);

    workerThread = new SenderWorker(this, inetAddress, port, sendingQueue,
        receivingQueue);
  }

  /**
   * Updates all entries of the given response key-values with the stored
   * settings of this instance.
   * 
   * @param response
   *          The settings of the response.
   * @throws NoSuchSessionException
   *           if a session with this target name is not open.
   */
  public final void update(final SettingsMap response)
      throws NoSuchSessionException {

    configuration.update(referenceSession.getTargetName(), connectionID,
        response);
  }

  /**
   * Returns the value of the given parameter, which is parsed to an
   * <code>boolean</code>.
   * 
   * @param textKey
   *          The name of the parameter.
   * @return The <code>boolean</code> value of this parameter. So if the value
   *         is equal to <code>Yes</code>, then <code>true</code> will be
   *         returned. Else <code>false</code> is returned.
   * @throws OperationalTextKeyException
   *           If the given parameter cannot be found.
   */
  public final boolean getSettingAsBoolean(final OperationalTextKey textKey)
      throws OperationalTextKeyException {

    return getSetting(textKey).compareTo("Yes") == 0;
  }

  /**
   * Returns the value of the given parameter, which is parsed to an
   * <code>integer</code>.
   * 
   * @param textKey
   *          The name of the parameter.
   * @return The <code>integer</code> value of this parameter.
   * @throws OperationalTextKeyException
   *           If the given parameter cannot be found.
   */
  public final int getSettingAsInt(final OperationalTextKey textKey)
      throws OperationalTextKeyException {

    return Integer.parseInt(getSetting(textKey));
  }

  /**
   * Returns the value of the given parameter as <code>String</code>.
   * 
   * @param textKey
   *          The name of the parameter.
   * @return The value of this parameter.
   * @throws OperationalTextKeyException
   *           If the given parameter cannot be found.
   */
  public final String getSetting(final OperationalTextKey textKey)
      throws OperationalTextKeyException {

    return configuration.getSetting(referenceSession.getTargetName(),
        connectionID, textKey);
  }

  /**
   * Returns the settings of the given session and connection.
   * 
   * @return The settings of this specific connection.
   */
  public final SettingsMap getSettings() {

    return configuration.getSettings(referenceSession.getTargetName(),
        connectionID);
  }

  /**
   * Repeat the invocation of the <code>IState.execute()</code>.
   * 
   * @throws InternetSCSIException
   *           if any violation of the iSCSI Standard (RFC3720) has emerged.
   */
  public final void execute() throws InternetSCSIException {

    while (state.execute()) {
      // repeat until the final state is reached
    }
  }

  /**
   * This method does all the necessary steps, which are needed when a
   * connection should be closed.
   * 
   * @throws IOException
   *           if an I/O error occurs.
   */
  public final void close() throws IOException {

    synchronized (workerThread) {
      workerThread.close();
    }

    synchronized (sendingQueue) {
      sendingQueue.clear();
    }

    synchronized (receivingQueue) {
      receivingQueue.clear();
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Connection with ID " + connectionID + " closed.");
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Reads one <code>ProtocolDataUnit</code> instance from the
   * <code>receivingQueue</code>.
   * 
   * @return An instance of a <code>ProtocolDataUnit</code>.
   */
  public final ProtocolDataUnit receive() {

    while (true) {
      synchronized (receivingQueue) {
        if (!receivingQueue.isEmpty()) {
          return receivingQueue.poll();
        }
      }

      Thread.yield();
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Increments the Expected Status Sequence Number as defined in RFC1982 where
   * <code>SERIAL_BITS = 32</code>.
   */
  final void incrementExpectedStatusSequenceNumber() {

    expectedStatusSequenceNumber.increment();
  }

  /**
   * Returns the Expected Status Sequence Number of this <code>Connection</code>
   * object.
   * 
   * @return The current Expected Status Sequence Number.
   */
  final SerialArithmeticNumber getExpectedStatusSequenceNumber() {

    return expectedStatusSequenceNumber;
  }

  /**
   * Sets the expected Status Sequence Number to the given one from the leading
   * Login Response.
   * 
   * @param newExpectedStatusSequenceNumber
   *          The new value.
   */
  final void setExpectedStatusSequenceNumber(
      final int newExpectedStatusSequenceNumber) {

    expectedStatusSequenceNumber.setValue(newExpectedStatusSequenceNumber);

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Set ExpStatSN to " + expectedStatusSequenceNumber);
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Enqueue this protocol data unit to the end of the sending queue.
   * 
   * @param protocolDataUnit
   *          The protocol data unit to add.
   */
  final void enqueue(final ProtocolDataUnit protocolDataUnit) {

    if (protocolDataUnit == null) {
      return;
    }

    synchronized (sendingQueue) {
      sendingQueue.add(protocolDataUnit);
    }
  }

  /**
   * Enqueue all protocol data units to the end of the sending queue.
   * 
   * @param protocolDataUnits
   *          The list with all protocol data units to add.
   */
  final void enqueue(final List<ProtocolDataUnit> protocolDataUnits) {

    for (ProtocolDataUnit protocolDataUnit : protocolDataUnits) {
      enqueue(protocolDataUnit);
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Switch to the new state.
   * 
   * @param newState
   *          The new state.
   */
  final void setState(final IState newState) {

    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Switching to state " + newState.getClass().getSimpleName());
    }

    state = newState;
  }

  /**
   * Returns the current state of this connection.
   * 
   * @return The current <code>IState</code> instance of this
   *         <code>Connection</code> instance.
   */
  final IState getState() {

    return state;
  }

  /**
   * Returns the session, which contains this connection instance.
   * 
   * @return The parent session instance.
   */
  final Session getSession() {

    return referenceSession;
  }

  /**
   * Returns the ID of this <code>Connection</code> object.
   * 
   * @return The connection ID.
   */
  final short getConnectionID() {

    return connectionID;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
