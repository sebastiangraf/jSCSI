package org.jscsi.target.connection;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.security.DigestException;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.conf.operationalText.OperationalTextException;
import org.jscsi.target.conf.operationalText.OperationalTextKey;
import org.jscsi.target.connection.Connection;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.ProtocolDataUnitFactory;
import org.jscsi.parser.exception.InternetSCSIException;

/**
 * Every Connection has one NetWorker. The NetWorker represents the Connection's
 * TCP/IP layer, i.e. receive and send PDUs.
 * 
 * @author Marcus Specht
 * 
 */
public class NetWorker {

	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(NetWorker.class);

	/**
	 * The Connection's sending queue for the <code>ProtocolDataUnit</code>s,
	 * which have to be sent.
	 */
	private final Map<Integer, ProtocolDataUnit> sendingBuffer;

	/** synchronizes sending Worker */
	private final Lock LOCK = new ReentrantLock();

	private final Condition somethingToSend = LOCK.newCondition();

	private final SortedSet<Integer> sendingPDUsStatSNs;

	/** sends outgoing PDUs */
	private final SenderWorker sender;

	/** receives incoming PDUs */
	private final ReceiverWorker receiver;

	/**
	 * Factory class for creating the several <code>ProtocolDataUnit</code>
	 * instances.
	 */
	private final ProtocolDataUnitFactory protocolDataUnitFactory;

	/**
	 * The used Socket Channel.
	 */
	private final SocketChannel socketChannel;

	/**
	 * This networker's referenced <code>Connection</code> instance.
	 */
	private final Connection refConnection;

	/**
	 * 
	 * @param sChannel
	 * @param connection
	 */
	public NetWorker(final SocketChannel sChannel, Connection connection) {
		refConnection = connection;
		socketChannel = sChannel;
		sendingBuffer = refConnection.getSendingBuffer();
		sendingPDUsStatSNs = new TreeSet<Integer>();
		protocolDataUnitFactory = new ProtocolDataUnitFactory();
		sender = new SenderWorker();
		receiver = new ReceiverWorker();
	}

	/**
	 * The NetWorker starts listening.
	 */
	public void startListening() {
		sender.start();
		receiver.start();
	}

	/**
	 * The NetWorker stops Listening. If <code>wait</code>, this method waits
	 * stopping until every queued PDU for sending is out.
	 */
	public void stopListening(boolean wait) {
		receiver.interrupt();
		// interrupt send after every PDU was sent
		if (wait) {
			while (hasPDUforSending()) {
				Thread.yield();
			}
		}
		sender.interrupt();
		//to get the SenderWorker out of waiting
		somethingToSend.signalAll();
	}

	/**
	 * Signals the SenderWorker to send the PDU from the sending buffer with the
	 * specified StatSN.
	 * 
	 * @param statusSequenceNumber
	 *            the PDUs StatSN
	 */
	final void signalSendingPDU(int statusSequenceNumber) {
		if (sendingBuffer.containsKey(statusSequenceNumber)) {
			sendingPDUsStatSNs.add(statusSequenceNumber);
			somethingToSend.signalAll();
		} else {
			logDebug("Signaled to send a PDU with it's StatSN, but sending Buffer doesn't contains the PDU: StatSN = "
					+ statusSequenceNumber);
		}

	}

	/**
	 * Waits for a PDU in the sending Queue.
	 * 
	 * @return Protocol Data Unit
	 */
	private ProtocolDataUnit getPDUforSending() {
		return getPDUforSending(0);
	}

	/**
	 * Waits for a PDU in the sendingBuffer until waiting time exceeded.
	 * 
	 * @param nanoSecs
	 *            wait <code>nanoSecs</code> before returning null
	 * @return Protocol Data Unit
	 */
	private ProtocolDataUnit getPDUforSending(long nanoSecs) {
		ProtocolDataUnit result = null;
		// wait until there is something to send
		LOCK.lock();
		try {
			if (nanoSecs <= 0) {
				while (!hasPDUforSending())
					somethingToSend.await();
			} else {
				while (!hasPDUforSending())
					somethingToSend.awaitNanos(nanoSecs);
			}
		} catch (InterruptedException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER
						.debug("Synchronisation error while awaiting a PDU to send!");
			}
		}
		result = sendingBuffer.get(sendingPDUsStatSNs.first());
		sendingPDUsStatSNs.remove(sendingPDUsStatSNs.first());
		LOCK.unlock();
		// finished waiting
		return result;
	}

	/**
	 * Returns true if the sending Buffer contains PDUs the Connection wants to
	 * send.
	 * 
	 * @return true if PDUs must be sent, false else
	 */
	private boolean hasPDUforSending() {
		if (sendingPDUsStatSNs.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Synchronized socketChannel
	 * 
	 * @return
	 */
	private SocketChannel getSocketChannel() {
		return socketChannel;

	}

	/**
	 * Signal the Connection a received PDU.
	 * @param pdu the received PDU
	 */
	private void addReceivedPDU(ProtocolDataUnit pdu) {
		refConnection.signalReceivedPDU(pdu);
	}
	
	/**
	 * Logs a trace Message specific to the referenced Connection, if
	 * trace log is enabled within the logging environment.
	 * @param logMessage 
	 */
	private void logTrace(String logMessage) {
		if (LOGGER.isTraceEnabled()) {
			if (refConnection.hasConnectionID) {
				LOGGER.trace("CID=" + refConnection.getConnectionID()
						+ " LogMessage: " + logMessage);
			}
		}
	}
	
	/**
	 * Logs a debug Message specific to the referenced Connection, if
	 * debug log is enabled within the logging environment.
	 * @param logMessage 
	 */
	private void logDebug(String logMessage) {
		if (LOGGER.isDebugEnabled()) {
			if (refConnection.hasConnectionID) {
				LOGGER.trace("CID=" + refConnection.getConnectionID()
						+ " LogMessage: " + logMessage);
			}
		}
	}
	
	/**
	 * The SenderWorker is sending every PDU the Connection is signaling,
	 * as long as the Thread runs.
	 * @author Marcus Specht
	 *
	 */
	private class SenderWorker extends Thread {

		@Override
		public void run() {
			while (!interrupted()) {
				ProtocolDataUnit pdu = getPDUforSending();
				try {
					if(pdu != null){
					pdu.write(getSocketChannel());
					}
				} catch (Exception e) {
					logDebug("Problem sending a PDU: HeaderSegment=\""
							+ pdu.getBasicHeaderSegment().getParser()
									.getShortInfo() + "\" DataSegment=\""
							+ pdu.getDataSegment().toString() + "\"  "
							+ e.getMessage());

				}
			}
		}

	}

	/**
	 * The ReceiverWorker Threads is waiting for incoming PDUs and adds them to
	 * the Connection's receiving Buffer
	 * 
	 * @author Marcus Specht
	 */
	private class ReceiverWorker extends Thread {

		private boolean successfulRead = true;

		private String headerDigest = null;
		private String dataDigest = null;

		@Override
		public void run() {
			while (!interrupted()) {
				// read digest configuration
				try {
					headerDigest = refConnection.getConfiguration().getKey(
							OperationalTextKey.HEADER_DIGEST).getValue()
							.getValue();
					dataDigest = refConnection.getConfiguration().getKey(
							OperationalTextKey.DATA_DIGEST).getValue()
							.getValue();
				} catch (OperationalTextException e1) {
					logDebug("Configuration has no valid Digest Keys: HeaderDigest="
							+ headerDigest + " DataDigest=" + dataDigest);
					// only an interrupt will destroy at least the Connection,
					// a clean shutdown should be done here
					this.interrupt();
				}
				// create and read receiving PDU
				successfulRead = true;
				ProtocolDataUnit pdu = protocolDataUnitFactory.create(
						headerDigest, dataDigest);
				try {
					pdu.read(getSocketChannel());

				} catch (DigestException e) {
					successfulRead = false;
				} catch (InternetSCSIException e) {
					successfulRead = false;
				} catch (IOException e) {
					successfulRead = false;
				}
				// targetTest can ignore error transmissions, because he's signaling
				// these with responses containing ExpCmdSN
				if (successfulRead) {
					addReceivedPDU(pdu);
				}
			}
		}
	}

}
