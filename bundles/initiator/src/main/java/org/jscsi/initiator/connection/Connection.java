/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.jscsi.initiator.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.DigestException;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.core.utils.SerialArithmeticNumber;
import org.jscsi.initiator.Configuration;
import org.jscsi.initiator.connection.state.IState;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.datasegment.SettingsMap;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.exception.NoSuchSessionException;
import org.jscsi.parser.exception.OperationalTextKeyException;

/**
 * <h1>AbsConnection</h1> <p/> This abstract class represents a connection,
 * which is used in the iSCSI Standard (RFC3720). Such a connection is directed
 * from the initiator to the target. It is used in Sessions.
 * 
 * @author Volker Wildi, Patrice Matthias Brend'amour
 * @see org.jscsi.connection.Connection
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

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * The worker caller, which handles the transmission of the packages over the
   * network.
   */
  private final SenderWorker senderReceiver;

  /**
   * Method to create and return a new, empty <code>Connection</code> object
   * with the configured layer of threading.
   * 
   * @param session
   *          Reference to the <code>AbsSession</code> object, which contains
   *          this connection.
   * @param initConfiguration
   *          The configuration to use within this connection.
   * @param inetAddress
   *          The <code>InetSocketAddress</code> to which this connection should
   *          established.
   * @param initConnectionID
   *          The ID of this connection.
   * @throws Exception
   *           If any error occurs.
   */

  public Connection(final Session session,
      final Configuration initConfiguration,
      final InetSocketAddress inetAddress, final short initConnectionID)
      throws Exception {

    senderReceiver = new SenderWorker(this, inetAddress);

    configuration = initConfiguration;
    connectionID = initConnectionID;

    referenceSession = session;
    expectedStatusSequenceNumber = new SerialArithmeticNumber();

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

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Increments the Expected Status Sequence Number as defined in RFC1982 where
   * <code>SERIAL_BITS = 32</code>.
   */
  public final void incrementExpectedStatusSequenceNumber() {

    expectedStatusSequenceNumber.increment();
  }

  /**
   * Returns the Expected Status Sequence Number of this <code>Connection</code>
   * object.
   * 
   * @return The current Expected Status Sequence Number.
   */
  public final SerialArithmeticNumber getExpectedStatusSequenceNumber() {

    return expectedStatusSequenceNumber;
  }

  /**
   * Sets the expected Status Sequence Number to the given one from the leading
   * Login Response.
   * 
   * @param newExpectedStatusSequenceNumber
   *          The new value.
   */
  public final void setExpectedStatusSequenceNumber(
      final int newExpectedStatusSequenceNumber) {

    expectedStatusSequenceNumber.setValue(newExpectedStatusSequenceNumber);

    LOGGER.trace("Set ExpStatSN to " + expectedStatusSequenceNumber);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Switch to the new state. Start point of the state pattern. All states are computed one after another.
   * 
   * @param newState
   *          The new state.
   * @throws InternetSCSIException
   *           of any kind
   */
  public final void nextState(final IState newState)
      throws InternetSCSIException {

    this.state = newState;
    if (this.state != null) {
      do {
        this.state.execute();
      } while (this.state.nextStateFollowing());
    }

  }

  /**
   * Returns the current state of this connection.
   * 
   * @return The current <code>IState</code> instance of this
   *         <code>Connection</code> instance.
   */
  public final IState getState() {

    return state;
  }

  /**
   * Returns the session, which contains this connection instance.
   * 
   * @return The parent session instance.
   */
  public final Session getSession() {

    return referenceSession;
  }

  /**
   * Returns the ID of this <code>Connection</code> object.
   * 
   * @return The connection ID.
   */
  public final short getConnectionID() {

    return connectionID;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This method does all the necessary steps, which are needed when a
   * connection should be closed.
   * 
   * @throws IOException
   *           if an I/O error occurs.
   */
  public final void close() throws IOException {

    senderReceiver.close();
    LOGGER.debug("Connection with ID " + connectionID + " closed.");
  }

  /**
   * Enqueue this protocol data unit to the end of the sending queue.
   * 
   * @param protocolDataUnit
   *          The protocol data unit to add.
   * @throws InternetSCSIException
   *           for nearly everything
   */
  public final void send(final ProtocolDataUnit protocolDataUnit)
      throws InternetSCSIException {

    try {
      senderReceiver.sendOverWire(protocolDataUnit);
    } catch (IOException e) {
      throw new InternetSCSIException(e);
    } catch (InterruptedException e) {
      throw new InternetSCSIException(e);
    }
  }

  /**
   * Enqueue all protocol data units to the end of the sending queue.
   * 
   * @param protocolDataUnits
   *          The list with all protocol data units to add.
   * @throws InternetSCSIException
   *           for nearly everything
   */
  public final void send(final Queue<ProtocolDataUnit> protocolDataUnits)
      throws InternetSCSIException {

    for (final ProtocolDataUnit unit : protocolDataUnits) {
      send(unit);
    }
  }

  /**
   * Reads one <code>ProtocolDataUnit</code> instance from the
   * <code>receivingQueue</code>.
   * 
   * @return An instance of a <code>ProtocolDataUnit</code>.
   * @throws InternetSCSIException
   *           for nearly everything
   */
  public final ProtocolDataUnit receive() throws InternetSCSIException {

    try {
      return senderReceiver.receiveFromWire();
    } catch (DigestException e) {
      throw new InternetSCSIException(e);
    } catch (IOException e) {
      throw new InternetSCSIException(e);
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
}
