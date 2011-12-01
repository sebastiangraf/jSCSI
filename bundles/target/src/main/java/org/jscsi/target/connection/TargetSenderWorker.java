package org.jscsi.target.connection;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.security.DigestException;

import org.apache.log4j.Logger;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.InitiatorMessageParser;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.ProtocolDataUnitFactory;
import org.jscsi.parser.TargetMessageParser;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.target.settings.Settings;
import org.jscsi.target.settings.SettingsException;
import org.jscsi.target.settings.TextKeyword;

/**
 * Instances of this class are used by {@link TargetConnection} objects for
 * sending and receiving {@link ProtocolDataUnit} objects.
 * @author Andreas Ergenzinger
 */
public class TargetSenderWorker {
	
	private static final Logger LOGGER = Logger.getLogger(TargetSenderWorker.class);
	
	/**
	 * The connection which uses this object for sending and receiving PDUs.
	 */
	final private TargetConnection connection;
	
	/**
	 * The session to which {@link #connection} belongs to.
	 */
	private TargetSession session;
	
	/**
	 * Used for writing serialized PDUs to and reading serialized PDUs from.
	 */
	final private SocketChannel socketChannel;
	
	/**
	 * Will be used to create {@link ProtocolDataUnit} objects from the
	 * byte stream read from the {@link #socketChannel}.
	 */
	final private ProtocolDataUnitFactory protocolDataUnitFactory;
	
	/**
	 * If this is <code>true</code>, then the next PDU read from the
	 * {@link #socketChannel} will be the first PDU received in the
	 * {@link #session}.
	 * <p>
	 * Will be initializes to <code>true</code> if and only if the
	 * {@link #connection} is the leading connection of its session.
	 * <p>
	 * PDUs identified by this variable as the first PDU in a session
	 * will not have their counters (i.e. CmdSN and ExpStatSN) checked.
	 * Instead the values of these counters will be used to initialize
	 * the targets local copies of these counters that will be used
	 * to ensure that no PDUs have been lost in transit.
	 */
	private boolean initialPdu;
	
	/**
	 * Creates a new {@link TargetSenderWorker} object.
	 * @param connection the connection that will use this object for
	 * sending and receiving PDUs
	 * @param socketChannel used for sending and receiving serialized
	 * PDU to and from the target
	 */
	public TargetSenderWorker(final TargetConnection connection,
			final SocketChannel socketChannel) {
		this.connection = connection;
		this.socketChannel = socketChannel;
		protocolDataUnitFactory = new ProtocolDataUnitFactory();
		initialPdu = connection.isLeadingConnection();
	}
	
	/**
	 * Sets the {@link #session} variable.
	 * <p>
	 * During the time this object is initialized, the
	 * {@link TargetConnection#getSession()} method will return
	 * <code>null</code>. Therefore {@link #session} must be set manually
	 * once the {@link TargetSession} object has been created.
	 * @param session the session of the {@link #connection}
	 */
	void setSession(final TargetSession session) {
		this.session = session;
	}

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
	 * @throws SettingsException 
	 */
	ProtocolDataUnit receiveFromWire() throws DigestException,
	InternetSCSIException, IOException, SettingsException {
		
		ProtocolDataUnit pdu;
		if (initialPdu) {
			/*
			 * The connection's ConnectionSettingsNegotiator has not been
			 * initialized, hence getSettings() would throw a
			 * NullPointerException.
			 * 
			 * Initialize PDU with default values, i.e. no digests.
			 */
			pdu = protocolDataUnitFactory.create(
						TextKeyword.NONE,//header digest
						TextKeyword.NONE);//data digest
		} else {
			//use negotiated or (now available) default settings
			final Settings settings = connection.getSettings();
			pdu = protocolDataUnitFactory.create(
					settings.getHeaderDigest(),
					settings.getDataDigest());
		}
		
		try {
			pdu.read(socketChannel);
		} catch (ClosedChannelException e) {
			throw new InternetSCSIException(e);
		}
		
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Receiving this PDU:\n" + pdu);
		
		//parse sequence counters
		final BasicHeaderSegment bhs =
			pdu.getBasicHeaderSegment();
		final InitiatorMessageParser parser =
			(InitiatorMessageParser)bhs.getParser();
		final int commandSequenceNumber = 
			parser.getCommandSequenceNumber();
		final int expectedStatusSequenceNumber =
			parser.getExpectedStatusSequenceNumber();
		
		//if this is the first PDU in the leading connection, then
		//initialize the session's ExpectedCommandSequenceNumber
		if (initialPdu) {
			initialPdu = false;
			//PDU is immediate Login PDU, checked in Target.main(),
			//ExpCmdSN of this PDU will be used to initialize the
			//respective session and connection parameters (sequence numbers)
			//see TargetSession and TargetConnection initialization in Target.main()
		} else {
			//check sequence counters
			if (session.getMaximumCommandSequenceNumber().lessThan(commandSequenceNumber))
				throw new InternetSCSIException("received CmdSN > local MaxCmdSN");
			
			if (!connection.getStatusSequenceNumber().equals(expectedStatusSequenceNumber) &&
				expectedStatusSequenceNumber != 0)//required by MS iSCSI initiator DATA-OUT PDU sequence
				throw new InternetSCSIException("received ExpStatusSN != local StatusSN + 1");
		}
		
		//increment CmdSN if not immediate PDU (or Data-Out PDU)
		if (parser.incrementSequenceNumber())
			session.getExpectedCommandSequenceNumber().increment();
			
		return pdu;
	}
	
	
	/**
	 * Sends the given <code>ProtocolDataUnit</code> instance over the socket to
	 * the connected iSCSI Target.
	 * 
	 * @param pdu
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
	
	final void sendOverWire(final ProtocolDataUnit pdu)
	throws InternetSCSIException, IOException, InterruptedException {
		
		//set sequence counters
		final TargetMessageParser parser = (TargetMessageParser)
			pdu.getBasicHeaderSegment().getParser();
		parser.setExpectedCommandSequenceNumber(
				session.getExpectedCommandSequenceNumber().getValue());
		parser.setMaximumCommandSequenceNumber(
				session.getMaximumCommandSequenceNumber().getValue());
		final boolean incrementSequenceNumber = parser.incrementSequenceNumber();
		if (incrementSequenceNumber)//set StatSN only if field is not reserved
			parser.setStatusSequenceNumber(
				connection.getStatusSequenceNumber().getValue());
		
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Sending this PDU:\n" + pdu);
		
		//send pdu
		pdu.write(socketChannel);
		
		//increment StatusSN if this was a Response PDU (with status)
		//or if special cases apply
		if (incrementSequenceNumber)
			connection.getStatusSequenceNumber().increment();
		
	}
}
