package org.jscsi.target.connection;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.Target;
import org.jscsi.target.TargetException;
import org.jscsi.target.conf.operationalText.OperationalTextException;
import org.jscsi.target.conf.target.TargetConfiguration;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.connection.Session;
import org.jscsi.target.util.Singleton;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.login.ISID;
import org.jscsi.parser.login.LoginRequestParser;

/**
 * The TargetSocketRouter holds every active Session within the jSCSI targetTest
 * environment. As a SocketHandler, possible SocketListener can assign a Socket
 * to the TargetSocketRouter, the TargetSocketRouter then tries to create a
 * valid <code>Connection</code>/<code>Session</code>.
 * 
 * @author Marcus Specht
 * 
 */
public final class TargetSocketRouter implements ISocketHandler {

	/** The logger interface. */
	private static final Log LOGGER = LogFactory
			.getLog(TargetSocketRouter.class);

	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------

	/** all active Sessions and their targetTest session identifying handles */
	private final Map<Short, Session> sessions;

	/** the target I am doing all the work for* */
	private final Target target;

	/** port-numbers and their SocketListener * */
	private final Map<Integer, SocketListener> listeningSockets;

	/** the only instance of a <code>TSIHFactory</code> */
	private static TSIHFactory tsihFactory;

	/** the only instance of a <code>TargetConfiguration</code> */
	private static TargetConfiguration targetConfiguration;

	public TargetSocketRouter(Target target) {
		// thread safe ConcurrentHashMap
		sessions = new ConcurrentHashMap<Short, Session>();
		listeningSockets = new ConcurrentHashMap<Integer, SocketListener>();
		this.target = target;
		try {
			targetConfiguration = Singleton
					.getInstance(TargetConfiguration.class);
		} catch (ClassNotFoundException e1) {
			logTrace("Couldn't load " + TargetConfiguration.class);
		}
		try {
			tsihFactory = Singleton.getInstance(TSIHFactory.class);
		} catch (ClassNotFoundException e) {
			logDebug("Couldn't load " + TSIHFactory.class);
		}
		logTrace("Initialized TargetSocketRouter");

	}

	/**
	 * Every Connection/Session within the jSCSI targetTest starts with a
	 * Socket, so here we go!
	 * 
	 * @return true if this Socket can be handled, else false
	 */
	public boolean assignSocket(Socket socket) {
		new Thread(new SocketManager(socket)).start();
		// here we could wait, or not!
		return true;
	}

	/**
	 * Returns the target this TaskRouter is referenced to
	 * @return
	 */
	public Target getReferencedTarget() {
		return target;
	}

	/**
	 * An array filled with all port numbers the SocketRouter is listening to
	 * @return
	 */
	public final int[] getListeningPorts() {
		int[] result = new int[listeningSockets.size()];
		int i = 0;
		for (int port : listeningSockets.keySet()) {
			result[i++] = port;
		}
		return result;
	}

	public String getDescribingString() {
		StringBuffer result = new StringBuffer();
		result
				.append("TargetSocketListener is listening to the following ports :");
		for (int port : getListeningPorts()) {
			result.append(port + ", ");
		}
		result.delete(result.length() - 2, result.length() - 1);
		result.append(";");
		return result.toString();
	}
	
	public void loadConfig(TargetConfiguration config){
		for(int port : config.getListeningPorts()){
				try {
					startListening(port);
				} catch (TargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	/**
	 * 
	 * @param port
	 * @throws TargetException
	 */
	public void startListening(int port) throws TargetException {
		if (listeningSockets.containsKey(port)) {
			logDebug("Tried to start listening on port that target is already listening: "
					+ port);
			throw new TargetException(
					"Tried to start listening on port that target is already listening: "
							+ port);
		} else {
			SocketListener newListener = new SocketListener(port, this);
			listeningSockets.put(port, newListener);
			newListener.start();
		}
	}

	/**
	 * 
	 * @param port
	 */
	public void stopListening(int port) {
		if (listeningSockets.containsKey(port)) {
			listeningSockets.get(port).stopListening();
			listeningSockets.remove(port);
			logTrace("Stopped listening on port " + port);
		}
	}


	/**
	 * Every incoming Socket is temporarily managed from the SocketHandler. He
	 * creates a Connection and analyzes the first incoming PDUs. If he doesn't
	 * find an appropriate Session, a new Session will be created!
	 * 
	 * @author Marcus Specht
	 * 
	 */
	private class SocketManager implements Runnable {

		private final Socket newSocket;

		private Connection newConnection = null;

		private SocketManager(Socket socket) {
			newSocket = socket;
		}

		public void run() {
			try {
				newConnection = new Connection(newSocket.getChannel());
			} catch (OperationalTextException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ProtocolDataUnit firstPDU = newConnection.peekReceivedPDU();
			if (firstPDU.getBasicHeaderSegment().getParser() instanceof LoginRequestParser) {
				processLoginRequest((LoginRequestParser) firstPDU
						.getBasicHeaderSegment().getParser());
				return;
			}
			// discard this Socket, Security Negotiation Phase or Discovery
			// Session would be possible too

		}

		@SuppressWarnings("unused")
		private void processDiscoveryRequest() {

		}

		@SuppressWarnings("unused")
		private void processSecurityNegotiationRequest() {

		}

		/**
		 * If the initiator's socket tries to login, the targetTest must check
		 * whether he wants to create a new Session, or he wants to assign a new
		 * Connection to an existing Session
		 * 
		 * @param parser
		 *            the LoginRequestParser from the first incoming PDU
		 */
		private void processLoginRequest(LoginRequestParser parser) {

			logTrace("Processing a new LoginRequest!");

			// prepare Connection
			newConnection.setConnectionID((short) parser.getConnectionID());
			// find appropriate Session
			short tsih = parser.getTargetSessionIdentifyingHandle();
			Session testedSession = getReferencedTarget().getSession(tsih);
			if (testedSession != null) {
				ISID isid = parser.getInitiatorSessionID();
				if (testedSession.getInitiatorSessionID().equals(isid)) {
					assignToSession(testedSession);
				}
			} else {
				try {
					createNewSession();
				} catch (Exception e) {
					logTrace("Dropped incoming Socket, couldn't create new Session");
				}
			}
		}

		/**
		 * Create a new Session
		 * 
		 * @throws Exception
		 */
		private void createNewSession() throws Exception {
			short newUniqueTSIH = tsihFactory.getNewTSIH();
			Session newSession = null;
			try {
				newSession = new Session(newConnection, newUniqueTSIH);
			} catch (OperationalTextException e) {
				logTrace("Had to drop a Socket, error occured creating a Session!");
			}
			getReferencedTarget().getSessionMap()
					.put(newUniqueTSIH, newSession);

			logTrace("Created new Session: TSIH = " + newUniqueTSIH);

		}

		/**
		 * Assign the new Connection to an existing Session
		 */
		private void assignToSession(Session session) {
			session.addConnection(newConnection);
			if (LOGGER.isTraceEnabled()) {
				LOGGER
						.trace("Assigned new Connection to existing Session: TSIH = "
								+ session.getTargetSessionIdentifyingHandleD()
								+ ", CID = " + newConnection.getConnectionID());
			}
		}

	}

	/**
	 * Logs a trace Message , if trace log is enabled within the logging
	 * environment.
	 * 
	 * @param logMessage
	 */
	private void logTrace(String logMessage) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(" Message: " + logMessage);
		}
	}

	/**
	 * Logs a debug Message, if debug log is enabled within the logging
	 * environment.
	 * 
	 * @param logMessage
	 */
	private void logDebug(String logMessage) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.trace(" Message: " + logMessage);

		}
	}

}
