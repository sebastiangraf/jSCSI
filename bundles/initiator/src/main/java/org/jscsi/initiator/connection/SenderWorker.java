/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * SenderWorker.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.initiator.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.security.DigestException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.parser.InitiatorMessageParser;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.ProtocolDataUnitFactory;
import org.jscsi.parser.TargetMessageParser;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.exception.InternetSCSIException;

/**
 * <h1>SenderWorker</h1> <p/> The worker caller to send all the protocol data
 * units over the socket of this connection.
 * 
 * @author Volker Wildi
 */
final class SenderWorker {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The logger interface. */
  private static final Log LOGGER = LogFactory.getLog(SenderWorker.class);

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The <code>Connection</code> instance of this worker caller. */
  private final Connection connection;

  /**
   * Non-blocking socket connection to use for the data transfer.
   */
  private final SocketChannel socketChannel;

  /**
   * Factory class for creating the several <code>ProtocolDataUnit</code>
   * instances.
   */
  private final ProtocolDataUnitFactory protocolDataUnitFactory;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Creates a new, empty <code>SenderWorker</code> instance.
   * 
   * @param initConnection
   *          The reference connection of this worker caller.
   * @param inetAddress
   *          The InetSocketAddress of the Target.
   * @throws IOException
   *           if any IO error occurs.
   */
  public SenderWorker(final Connection initConnection,
      final InetSocketAddress inetAddress) throws IOException {

    connection = initConnection;
    socketChannel = SocketChannel.open(inetAddress);
    socketChannel.socket().setTcpNoDelay(true);

    protocolDataUnitFactory = new ProtocolDataUnitFactory();

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

    socketChannel.close();

  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Receives a <code>ProtocolDataUnit</code> from the socket and appends it to
   * the end of the receiving queue of this connection.
   * 
   * @return Queue with the resulting units
   * @throws IOException
   *           if an I/O error occurs.
   * @throws InternetSCSIException
   *           if any violation of the iSCSI-Standard emerge.
   * @throws DigestException
   *           if a mismatch of the digest exists.
   */
  public ProtocolDataUnit receiveFromWire() throws DigestException,
      InternetSCSIException, IOException {

    final ProtocolDataUnit protocolDataUnit = protocolDataUnitFactory.create(
        connection.getSetting(OperationalTextKey.HEADER_DIGEST), connection
            .getSetting(OperationalTextKey.DATA_DIGEST));

    try {
      protocolDataUnit.read(socketChannel);
    } catch (ClosedChannelException e) {
      throw new InternetSCSIException(e);
    }

    LOGGER.debug("Receiving this PDU: " + protocolDataUnit);

    final Exception isCorrect = connection.getState().isCorrect(
        protocolDataUnit);
    if (isCorrect == null) {
      LOGGER.trace("Adding PDU to Receiving Queue.");

      final TargetMessageParser parser = (TargetMessageParser) protocolDataUnit
          .getBasicHeaderSegment().getParser();
      final Session session = connection.getSession();

      // the PDU maxCmdSN is greater than the local maxCmdSN, so we
      // have to update the local one
      if (session.getMaximumCommandSequenceNumber().compareTo(
          parser.getMaximumCommandSequenceNumber()) < 0) {
        session.setMaximumCommandSequenceNumber(parser
            .getMaximumCommandSequenceNumber());
      }

      // the PDU expCmdSN is greater than the local expCmdSN, so we
      // have to update the local one
      if (parser.incrementSequenceNumber()) {
        if (connection.getExpectedStatusSequenceNumber().compareTo(
            parser.getStatusSequenceNumber()) >= 0) {
          connection.incrementExpectedStatusSequenceNumber();
        } else {
          LOGGER.error("Status Sequence Number Mismatch (received, expected): "
              + parser.getStatusSequenceNumber() + ", "
              + (connection.getExpectedStatusSequenceNumber().getValue() - 1));
        }

      }

    } else {
      throw new InternetSCSIException(isCorrect);
    }
    return protocolDataUnit;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Sends the given <code>ProtocolDataUnit</code> instance over the socket to
   * the connected iSCSI Target.
   * 
   * @param unit
   *          The <code>ProtocolDataUnit</code> instances to send.
   * @throws InternetSCSIException
   *           if any violation of the iSCSI-Standard emerge.
   * @throws IOException
   *           if an I/O error occurs.
   * @throws InterruptedException
   *           if another caller interrupted the current caller before or while
   *           the current caller was waiting for a notification. The
   *           interrupted status of the current caller is cleared when this
   *           exception is thrown.
   */
  public final void sendOverWire(final ProtocolDataUnit unit)
      throws InternetSCSIException, IOException, InterruptedException {

    final Session session = connection.getSession();

    unit.getBasicHeaderSegment().setInitiatorTaskTag(
        session.getInitiatorTaskTag());
    final InitiatorMessageParser parser = (InitiatorMessageParser) unit
        .getBasicHeaderSegment().getParser();
    parser.setCommandSequenceNumber(session.getCommandSequenceNumber());
    parser.setExpectedStatusSequenceNumber(connection
        .getExpectedStatusSequenceNumber().getValue());

    unit.write(socketChannel);

    LOGGER.debug("Sending this PDU: " + unit);

    // increment the Command Sequence Number
    if (parser.incrementSequenceNumber()) {
      connection.getSession().incrementCommandSequenceNumber();
    }

  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
