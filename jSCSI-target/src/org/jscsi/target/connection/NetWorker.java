package org.jscsi.target.connection;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.security.DigestException;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.conf.OperationalTextException;
import org.jscsi.target.conf.OperationalTextKey;
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

	/**
	 * The receiving queue of the <code>ProtocolDataUnit</code>s, which are
	 * received.
	 */
	private final SortedMap<Integer, ProtocolDataUnit> receivingBuffer;

	/** synchronizes sending Worker */
	private final Lock LOCK = new ReentrantLock();

	private final Condition somethingToSend = LOCK.newCondition();

	private int nextSendingStatSN = -1;
	
	private int lastSendingStatSN = -1;
	
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
		receivingBuffer = refConnection.getReceivingBuffer();
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
			while (sendingBuffer.size() != 0) {
				Thread.yield();
			}
		}
		sender.interrupt();
	}
	
	final void setNextStatusSequenceNumber(int statSN){
		nextSendingStatSN = statSN;
	}
	
	final void signalSendingPDU(int statusSequenceNumber) {
		lastSendingStatSN = statusSequenceNumber;
		somethingToSend.signalAll();
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
	 * Waits for a PDU in the sending Queue until waiting time exceeded.
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
				while (hasPDUforSending())
					somethingToSend.await();
			} else {
				while (hasPDUforSending())
					somethingToSend.awaitNanos(nanoSecs);
			}
		} catch (InterruptedException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER
						.debug("Synchronisation error while awaiting a PDU to send!");
			}
		}
		result = sendingBuffer.get(nextSendingStatSN);
		nextSendingStatSN++;
		LOCK.unlock();
		// finished waiting
		return result;
	}
	
	private boolean hasPDUforSending(){
		if(nextSendingStatSN <= lastSendingStatSN){
			return true;
		} else{
			return false;
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
	 * Adds a received PDU to the <code>Connection</code>'s received PDU
	 * queue. All waiting Threads on the Connection are signaled.
	 * 
	 * @param pdu
	 */
	private void addReceivedPDU(ProtocolDataUnit pdu) {
		refConnection.signalReceivedPDU(pdu);
	}

	private class SenderWorker extends Thread {

		@Override
		public void run() {
			while (!interrupted()) {
				ProtocolDataUnit pdu = getPDUforSending();
				try {
					pdu.write(getSocketChannel());
				} catch (Exception e) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Problem sending a PDU: HeaderSegment=\""
								+ pdu.getBasicHeaderSegment().getParser()
										.getShortInfo() + "\" DataSegment=\""
								+ pdu.getDataSegment().toString() + "\"  "
								+ e.getMessage());
					}
				}
			}
		}

	}

	/**
	 * The ReceiverWorker Threads is waiting for incoming PDUs and adds them to
	 * the Connection's receiving Queue
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
				// read digst configuration
				try {
					headerDigest = refConnection.getConfiguration().getKey(
							OperationalTextKey.HEADER_DIGEST).getValue()
							.getString();
					dataDigest = refConnection.getConfiguration().getKey(
							OperationalTextKey.DATA_DIGEST).getValue()
							.getString();
				} catch (OperationalTextException e1) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER
								.debug("Configuration has no valid Digest Keys: HeaderDigest="
										+ headerDigest
										+ " DataDigest="
										+ dataDigest);
					}
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
				// target can ignore error transmissions, because he's signaling
				// these with responses containing ExpCmdSN
				if (successfulRead) {
					addReceivedPDU(pdu);
				}
			}
		}
	}

}
