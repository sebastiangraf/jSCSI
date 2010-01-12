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

package org.jscsi.initiator;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.initiator.connection.Connection;
import org.jscsi.initiator.connection.Session;
import org.jscsi.parser.exception.NoSuchSessionException;

/**
 * <h1>LinkFactory</h1>
 * <p>
 * Implements a Factory which enables the Initiator to switch between
 * multithreaded and singlethreaded modes.
 * </p>
 * 
 * @author Patrice Matthias Brend'amour
 */

public final class LinkFactory {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The logger interface. */
  private static final Log LOGGER = LogFactory.getLog(LinkFactory.class);

  /** The calling Initiator. */
  private final Initiator initiator;


  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Default Constructor to create a Linkfactory. Creates everything
   * MultiThreaded and MultiConnectioned
   * 
   * @param initiat
   *          The calling Initiator
   */

  public LinkFactory(final Initiator initiat) {

    this.initiator = initiat;
  }

  /**
   * Method to create and return a new, empty <code>Session</code> object with
   * the configured layer of threading.
   * 
   * @param initConfiguration
   *          The configuration to use within this session.
   * @param initTargetName
   *          The name of the iSCSI Target.
   * @param inetAddress
   *          The <code>InetAddress</code> of the leading connection of this
   *          session.
   * @return AbsSession The SessionObject.
   */
  public final Session getSession(final Configuration initConfiguration,
      final String initTargetName, final InetSocketAddress inetAddress) {

    try {
      // Create a new Session
      final Session newSession = new Session(this, initConfiguration, initTargetName,
          inetAddress, Executors.newSingleThreadExecutor());
      return newSession;
    } catch (Exception e) {
      LOGGER.error("This exception is thrown: " + e);
      e.printStackTrace();
      return null;
    }

  }

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
   * @return AbsConnection The Connection Object.
   */
  public final Connection getConnection(final Session session,
      final Configuration initConfiguration,
      final InetSocketAddress inetAddress, final short initConnectionID) {

    try {
      final Connection newConnection = new Connection(session, initConfiguration,
          inetAddress, initConnectionID);
      return newConnection;
    } catch (Exception e) {
      LOGGER.error("This exception is thrown: " + e);
      e.printStackTrace();
      return null;
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Adds a dying <code>Session</code> instance to the Queue.
   * 
   * @param session
   *          The name of the session, which instance you want.
   */
  public final void closedSession(final Session session) {

    try {
      initiator.removeSession(session);
    } catch (NoSuchSessionException e) {
      // DO NOTHING
    }

  }
}
