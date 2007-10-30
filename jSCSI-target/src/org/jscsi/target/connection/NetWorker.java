package org.jscsi.target.connection;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.security.DigestException;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	private final Queue<ProtocolDataUnit> sendingQueue;

	/**
	 * The receiving queue of the <code>ProtocolDataUnit</code>s, which are
	 * received.
	 */
	private final Queue<ProtocolDataUnit> receivingQueue;

	/** synchronizes sending Worker */
	private final Lock LOCK = new ReentrantLock();

	private final Condition somethingToSend = LOCK.newCondition();

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
		sendingQueue = refConnection.getSendingQueue();
		receivingQueue = refConnection.getReceivingQueue();
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
			while (sendingQueue.size() != 0) {
				Thread.yield();
			}
		}
		sender.interrupt();
	}

	final void signalSendingPDU() {
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
		// wait until there is something to send
		LOCK.lock();
		try {
			if (nanoSecs <= 0) {
				while (sendingQueue.size() == 0)
					somethingToSend.await();
			} else {
				while (sendingQueue.size() == 0)
					somethingToSend.await();
			}
		} catch (InterruptedException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER
						.debug("Synchronisation error while awaiting a PDU to send!");
			}
		}
		LOCK.unlock();
		// finished waiting
		return sendingQueue.poll();
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
		receivingQueue.add(pdu);
		refConnection.signalReceivedPDU();
	}

	private class SenderWorker extends Thread {

		@Override
		public void run() {
			while (!interrupted()) {
				ProtocolDataUnit pdu = getPDUforSending();
				// set necessary tags
				try {
					pdu.write(getSocketChannel());
				} catch (Exception e) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Problem sending a PDU: HeaderSegment=\""
								+ pdu.getBasicHeaderSegment().getParser()
										.getShortInfo() + "\" DataSegment=\""
								+ pdu.getDataSegment().toString() + "\"");
					}
				}
			}
		}

	}

	private class ReceiverWorker extends Thread {

		@Override
		public void run() {
			while (!interrupted()) {
				// should read the cnofiguration's digest settings
				ProtocolDataUnit pdu = protocolDataUnitFactory.create("None",
						"None");

				try {

					pdu.read(getSocketChannel());

				} catch (DigestException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InternetSCSIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				addReceivedPDU(pdu);
			}
		}
	}

}
