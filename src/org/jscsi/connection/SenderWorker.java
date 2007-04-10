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
 * $Id: SenderWorker.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.security.DigestException;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.parser.InitiatorMessageParser;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.ProtocolDataUnitFactory;
import org.jscsi.parser.TargetMessageParser;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.exception.InternetSCSIException;

/**
 * <h1>SenderWorker</h1>
 * <p/>
 * 
 * The worker caller to send all the protocol data units over the socket of this
 * connection.
 * 
 * @author Volker Wildi
 */
final class SenderWorker implements Runnable {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The logger interface. */
  private static final Log LOGGER = LogFactory.getLog(SenderWorker.class);

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The <code>Connection</code> instance of this worker caller. */
  private final Connection connection;

  /**
   * The sending queue for the <code>ProtocolDataUnit</code>s, which have to
   * be sent.
   */
  private final Queue<ProtocolDataUnit> sendingQueue;

  /**
   * Contains all the <code>ProtocolDataUnit</code>s of a sequence, which are
   * not acknowledged by the iSCSI Target up to now.
   */
  private final Queue<ProtocolDataUnit> waitForAcknowledgementQueue;

  /**
   * The receiving queue of the <code>ProtocolDataUnit</code>s, which are
   * received.
   */
  private final Queue<ProtocolDataUnit> receivingQueue;

  /**
   * Non-blocking socket connection to use for the data transfer.
   */
  private final SocketChannel socketChannel;

  /**
   * Factory class for creating the several <code>ProtocolDataUnit</code>
   * instances.
   */
  private final ProtocolDataUnitFactory protocolDataUnitFactory;

  /** Do we expecting a response from the iSCSI Target. */
  private boolean responseExpected;

  /** Should this caller to be stopped? */
  private boolean stop;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Creates a new, empty <code>SenderWorker</code> instance.
   * 
   * @param initConnection
   *          The reference connection of this worker caller.
   * @param inetAddress
   *          The Internet Address of the Target.
   * @param port
   *          The used iSCSI port of this connection.
   * @param initSendingQueue
   *          A reference to the sending queue.
   * @param initReceivingQueue
   *          A reference to the receiving queue.
   * 
   * @throws IOException
   *           if any IO error occurs.
   */
  SenderWorker(final Connection initConnection, final InetAddress inetAddress,
      final int port, final Queue<ProtocolDataUnit> initSendingQueue,
      final Queue<ProtocolDataUnit> initReceivingQueue) throws IOException {

    connection = initConnection;
    socketChannel = SocketChannel
        .open(new InetSocketAddress(inetAddress, port));

    synchronized (initSendingQueue) {
      sendingQueue = initSendingQueue;
    }

    waitForAcknowledgementQueue = new LinkedList<ProtocolDataUnit>();

    synchronized (initReceivingQueue) {
      receivingQueue = initReceivingQueue;
    }

    protocolDataUnitFactory = new ProtocolDataUnitFactory();

    new Thread(this, getClass().getSimpleName()).start();
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final void run() {

    ProtocolDataUnit head;
    while (!stop) {
      if (responseExpected) {
        // reading...
        try {
          receiveFromWire();
        } catch (Exception e) {
          e.printStackTrace();

          if (LOGGER.isErrorEnabled()) {
            LOGGER.error("Error during or receiving PDUs: " + e);
          }
        }
      } else {
        // sending...
        try {
          // do not overwhelm the iSCSI Target
          if (connection.getSession().hasTargetMoreResources()) {
            synchronized (sendingQueue) {
              head = sendingQueue.poll();
            }

            if (head != null) {
              waitForAcknowledgementQueue.add(head);
              sendOverWire(head);
            }
          } else {
            LOGGER.info("Target has no more resources to accept more input. ("
                + connection.getSession().getMaximumCommandSequenceNumber()
                + ", " + connection.getSession().getCommandSequenceNumber()
                + ")");
          }
        } catch (Exception e) {
          if (LOGGER.isErrorEnabled()) {
            LOGGER.error("Error during sending PDUs: " + e);
          }
        }
      }

      Thread.yield();
    }
  }

  /**
   * Stops this caller instance.
   */
  public final void stop() {

    stop = true;
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
  final void close() throws IOException {

    synchronized (socketChannel) {
      socketChannel.close();
    }

    stop = true;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Receives a <code>ProtocolDataUnit</code> from the socket and appends it
   * to the end of the receiving queue of this connection.
   * 
   * @throws IOException
   *           if an I/O error occurs.
   * @throws InternetSCSIException
   *           if any violation of the iSCSI-Standard emerge.
   * @throws DigestException
   *           if a mismatch of the digest exists.
   */
  private final void receiveFromWire() throws DigestException,
      InternetSCSIException, IOException {

    final ProtocolDataUnit protocolDataUnit = protocolDataUnitFactory.create(
        connection.getSetting(OperationalTextKey.HEADER_DIGEST), connection
            .getSetting(OperationalTextKey.DATA_DIGEST));

    synchronized (socketChannel) {
      protocolDataUnit.read(socketChannel);
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Receiving this PDU: " + protocolDataUnit);
    }

    // we have to read more Protocol Data Units from the socket
    responseExpected = !protocolDataUnit.getBasicHeaderSegment().isFinalFlag();

    if (connection.getState().isCorrect(protocolDataUnit)) {
      synchronized (receivingQueue) {
        receivingQueue.add(protocolDataUnit);
      }

      if (protocolDataUnit.getBasicHeaderSegment().isFinalFlag()) {
        waitForAcknowledgementQueue.clear();
      }

      synchronized (connection) {
        final TargetMessageParser parser = (TargetMessageParser) protocolDataUnit
            .getBasicHeaderSegment().getParser();
        final Session session = connection.getSession();

        // the PDU maxCmdSN is greater than the local maxCmdSN, so we have to
        // update the local one
        if (session.getMaximumCommandSequenceNumber().compareTo(
            parser.getMaximumCommandSequenceNumber()) < 0) {
          session.setMaximumCommandSequenceNumber(parser
              .getMaximumCommandSequenceNumber());
        }

        // the PDU expCmdSN is greater than the local expCmdSN, so we have to
        // update the local one
        if (parser.incrementSequenceNumber()) {
          if (connection.getExpectedStatusSequenceNumber().compareTo(
              parser.getStatusSequenceNumber()) >= 0) {
            connection.incrementExpectedStatusSequenceNumber();
          } else {
            if (LOGGER.isErrorEnabled()) {
              LOGGER
                  .error("Status Sequence Number Mismatch (received, expected): "
                      + parser.getStatusSequenceNumber()
                      + ", "
                      + (connection.getExpectedStatusSequenceNumber()
                          .getValue() - 1));
            }
          }
        }
      }

      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Head job in Sending Queue is removed.");
      }
    } else {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn("Received incorrect PDU.");
      }

      if (protocolDataUnit.getBasicHeaderSegment().isFinalFlag()) {
        synchronized (sendingQueue) {
          if (sendingQueue instanceof LinkedList) {
            final LinkedList<ProtocolDataUnit> linkedList = (LinkedList<ProtocolDataUnit>) sendingQueue;
            final ProtocolDataUnit[] resendItems = (ProtocolDataUnit[]) waitForAcknowledgementQueue
                .toArray();

            for (int i = resendItems.length; i > 0; i--) {
              linkedList.addFirst(resendItems[i]);
            }
          } else {
            throw new UnsupportedOperationException(
                "This kind of queue subclass is not supported.");
          }
        }
      }
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Sends the given <code>ProtocolDataUnit</code> instance over the socket to
   * the connected iSCSI Target.
   * 
   * @param protocolDataUnit
   *          The <code>ProtocolDataUnit</code> instance to send.
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
  private final void sendOverWire(final ProtocolDataUnit protocolDataUnit)
      throws InternetSCSIException, IOException, InterruptedException {

    final Session session = connection.getSession();
    protocolDataUnit.getBasicHeaderSegment().setInitiatorTaskTag(
        session.getInitiatorTaskTag());
    final InitiatorMessageParser parser = (InitiatorMessageParser) protocolDataUnit
        .getBasicHeaderSegment().getParser();
    parser.setCommandSequenceNumber(session.getCommandSequenceNumber());
    parser.setExpectedStatusSequenceNumber(connection
        .getExpectedStatusSequenceNumber().getValue());

    synchronized (socketChannel) {
      protocolDataUnit.write(socketChannel);
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Sending this PDU: " + protocolDataUnit);
    }

    // increment the Command Sequence Number
    if (parser.incrementSequenceNumber()) {
      connection.getSession().incrementCommandSequenceNumber();
    }

    // are finished with sending?
    responseExpected = protocolDataUnit.getBasicHeaderSegment().isFinalFlag();
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
