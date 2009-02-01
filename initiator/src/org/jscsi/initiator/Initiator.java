/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id: Initiator.java
 * 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.initiator;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.initiator.connection.Session;
import org.jscsi.parser.exception.NoSuchSessionException;

/**
 * <h1>Initiator</h1>
 * <p>
 * This class represents an initiator, which request messages to a target
 * defined by the iSCSI Protocol (RFC3720).
 * 
 * @author Volker Wildi
 */
public final class Initiator {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The Logger interface. */
  private static final Log LOGGER = LogFactory.getLog(Initiator.class);

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** Stores all configuration parameters. */
  private final Configuration configuration;

  /** Stores all opened sessions. */
  private final Map<String, Session> sessions;

  /** Stores all configuration parameters. */
  private final LinkFactory factory;

  /**
   * Constructor to create an empty <code>Initiator</code> object with the given
   * configuration.
   * 
   * @param initConfiguration
   *          The user-defined configuration file.
   */
  public Initiator(final Configuration initConfiguration) {

    configuration = initConfiguration;
    sessions = new Hashtable<String, Session>(1);
    factory = new LinkFactory(this);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Creates a new session with the given target name, which is read from the
   * configuration file.
   * 
   * @param targetName
   *          The name of the iSCSI Target to connect.
   * @throws Exception
   *           if any error occurs.
   */
  public final void createSession(final String targetName) throws Exception {

    createSession(configuration.getTargetAddress(targetName), targetName);
  }

  /**
   * Creates a new session to a target with the given Internet address and port.
   * The target has the name <code>targetName</code>.
   * 
   * @param targetAddress
   *          The Internet address and Port of the target.
   * @param targetName
   *          Name of the target, to which a connection should be created.
   * @throws Exception
   *           if any error occurs.
   */
  public final void createSession(final InetSocketAddress targetAddress,
      final String targetName) throws Exception {

    final Session session = factory.getSession(configuration, targetName,
        targetAddress);
    sessions.put(session.getTargetName(), session);
    LOGGER.info("Created the session with iSCSI Target '" + targetName
        + "' at " + targetAddress.getHostName() + " on port "
        + targetAddress.getPort() + ".");
  }

  /**
   * Closes all opened connections within this session to the given target.
   * 
   * @param targetName
   *          The name of the target, which connection should be closed.
   * @throws Exception
   *           if any error occurs.
   */
  public final void closeSession(final String targetName) throws Exception {

    getSession(targetName).logout(this);
    // TODO Test the removal from the map.
    sessions.remove(targetName);

    LOGGER.info("Closed the session to the iSCSI Target '" + targetName + "'.");
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Invokes a read operation for the session <code>targetName</code> and store
   * the read bytes in the buffer <code>dst</code>. Start reading at the logical
   * block address and request <code>transferLength</code> blocks.
   * 
   * @param caller
   *          The pointer to the calling instance of this method.
   * @param targetName
   *          The name of the session to invoke this read operation.
   * @param dst
   *          The buffer to store the read data.
   * @param logicalBlockAddress
   *          The logical block address of the beginning.
   * @param transferLength
   *          Number of bytes to read.
   * @throws Exception
   *           if any error occurs.
   *           
   * @return FutureObject for MultiThreadedReads 
   */
  public final Future<Void> multiThreadedRead(final Object caller,
      final String targetName, final ByteBuffer dst,
      final int logicalBlockAddress, final long transferLength)
      throws Exception {

    final Future<Void> returnVal = getSession(targetName).read(caller, dst,
        logicalBlockAddress, transferLength);
    return returnVal;
  }

  
  /**
   * Invokes a read operation for the session <code>targetName</code> and store
   * the read bytes in the buffer <code>dst</code>. Start reading at the logical
   * block address and request <code>transferLength</code> blocks.
   * 
   * @param caller
   *          The pointer to the calling instance of this method.
   * @param targetName
   *          The name of the session to invoke this read operation.
   * @param dst
   *          The buffer to store the read data.
   * @param logicalBlockAddress
   *          The logical block address of the beginning.
   * @param transferLength
   *          Number of bytes to read.
   * @throws Exception
   *           if any error occurs.
   */
  public final void read(final Object caller, final String targetName,
      final ByteBuffer dst, final int logicalBlockAddress,
      final long transferLength) throws Exception {

    multiThreadedRead(caller, targetName, dst, logicalBlockAddress,
        transferLength).get();

  }

  /**
   * Invokes a write operation for the session <code>targetName</code> and
   * transmits the bytes in the buffer <code>dst</code>. Start writing at the
   * logical block address and transmit <code>transferLength</code> blocks.
   * 
   * @param caller
   *          The pointer to the calling instance of this method.
   * @param targetName
   *          The name of the session to invoke this write operation.
   * @param src
   *          The buffer to transmit.
   * @param logicalBlockAddress
   *          The logical block address of the beginning.
   * @param transferLength
   *          Number of bytes to write.
   * @throws Exception
   *           if any error occurs.
   * @return FutureObject for the multi-threaded write operation
   */
  public final Future<Void> multiThreadedWrite(final Object caller,
      final String targetName, final ByteBuffer src,
      final int logicalBlockAddress, final long transferLength)
      throws Exception {

    return getSession(targetName).write(caller, src, logicalBlockAddress,
        transferLength);
  }

  
  /**
   * Invokes a write operation for the session <code>targetName</code> and
   * transmits the bytes in the buffer <code>dst</code>. Start writing at the
   * logical block address and transmit <code>transferLength</code> blocks.
   * 
   * @param caller
   *          The pointer to the calling instance of this method.
   * @param targetName
   *          The name of the session to invoke this write operation.
   * @param src
   *          The buffer to transmit.
   * @param logicalBlockAddress
   *          The logical block address of the beginning.
   * @param transferLength
   *          Number of bytes to write.
   * @throws Exception
   *           if any error occurs.
   */
  public final void write(final Object caller, final String targetName,
      final ByteBuffer src, final int logicalBlockAddress,
      final long transferLength) throws Exception {

    multiThreadedWrite(caller, targetName, src, logicalBlockAddress,
        transferLength).get();
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Returns the used block size (in bytes) of the iSCSI Target.
   * 
   * @param targetName
   *          The name of the session to invoke this capacity operation.
   * @return the used block size (in bytes) of the connected iSCSI Target.
   * @throws NoSuchSessionException
   *           if the session connected to the target is yet not open.
   */
  public final long getBlockSize(final String targetName)
      throws NoSuchSessionException {

    return getSession(targetName).getBlockSize();
  }

  /**
   * Returns the capacity (in blocks) of the iSCSI Target.
   * 
   * @param targetName
   *          The name of the session to invoke this capacity operation.
   * @return the capacity in blocks of the connected iSCSI Target.
   * @throws NoSuchSessionException
   *           if the session connected to the target is yet not open.
   */
  public final long getCapacity(final String targetName)
      throws NoSuchSessionException {

    return getSession(targetName).getCapacity();
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Returns the <code>Session</code> instance of the iSCSI Target with the
   * given name.
   * 
   * @param targetName
   *          The name of the session, which instance you want.
   * @return The requested <code>Session</code> instance.
   */
  private final Session getSession(final String targetName)
      throws NoSuchSessionException {

    final Session session = sessions.get(targetName);

    if (session != null) {
      return session;
    } else {
      throw new NoSuchSessionException("Session " + targetName + " not found!");
    }
  }

  /**
   * Removes the <code>Session</code> instances form the sessions queue.
   * 
   * @param sessionReq
   *          The Session to remove
   * @throws NoSuchSessionException
   *           if the Session does not exist in the Map
   */
  public final void removeSession(final Session sessionReq)
      throws NoSuchSessionException {

    final Session session = sessions.get(sessionReq.getTargetName());

    if (session != null) {
      sessions.remove(sessionReq.getTargetName());
    } else {
      throw new NoSuchSessionException("Session " + sessionReq.getTargetName()
          + " not found!");
    }
  }

  /**
   * TODO Search a better solution for this. How can we notify the Application,
   * that all Sessions are closed (and all Tasks are finished)
   */
  /**
   * is the Sessions Map empty?.
   * 
   * @return true if it is empty
   */
  public final Boolean noSessions() {

    return (sessions.size() == 0);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
