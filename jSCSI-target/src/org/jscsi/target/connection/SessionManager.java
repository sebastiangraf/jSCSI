package org.jscsi.target.connection;

import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.connection.Session;
import org.jscsi.target.util.Singleton;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.login.ISID;
import org.jscsi.parser.login.LoginRequestParser;


/**
 * The SessionManager holds every active Session within the jSCSI target
 * environment. As a SocketHandler, possible SocketListener can assign a Socket
 * to the SessionManager, the SessionManager then tries to create a valid
 * <code>Connection</code>/<code>Session</code>.
 * 
 * @author Marcus Specht
 * 
 */
public final class SessionManager implements ISocketHandler {

	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(SessionManager.class);

	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------

	/** all active Sessions and their target session identifying handles */
	private final Map<Short, Session> sessions;

	/** the only instance of a <code>TSIHFactory</code> */
	private static TSIHFactory tsihFactory;

	public SessionManager() {
		// thread safe ConcurrentHashMap
		sessions = new ConcurrentHashMap<Short, Session>();
		try {
			tsihFactory = Singleton.getInstance(TSIHFactory.class);
		} catch (ClassNotFoundException e) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("Couldn't load " + TSIHFactory.class);
			}
		}
	}

	/**
	 * Every Connection/Session within the jSCSI target starts with a Socket, so
	 * here we go!
	 * 
	 * @return true if this Socket can be handled, else false
	 */
	public boolean assignSocket(Socket socket) {
		new Thread(new SocketManager(socket)).start();
		// here we could wait, or not!
		return true;
	}

	/**
	 * Return the SessionMap, which is a ConcurrentHashMap, e.g. should be
	 * thread safe.
	 * 
	 * @return the SessionMap
	 */
	private Map<Short, Session> getSessionMap() {
		return sessions;
	}

	/**
	 * Get the Session by the target session identifying handle.
	 * 
	 * @param targetSessionIdentifyingHandle
	 *            the session's TSIH
	 * @return a session object or null if no such session
	 */
	public Session getSession(short targetSessionIdentifyingHandle) {
		return getSessionMap().get(targetSessionIdentifyingHandle);
	}

	/**
	 * Get the Session with the initiator session id and initiator name.
	 * 
	 * @param initiatorSessionID
	 *            the sessions ISID
	 * @param initiatorName
	 *            the sessions connected initiator name
	 * @return a session object or null if no such session
	 */
	public Session getSession(ISID initiatorSessionID, String initiatorName) {
		Iterator<Session> sessions = getSessions();
		Session result = null;
		// find matching Session
		while (sessions.hasNext()) {
			Session checkedSession = sessions.next();
			if (checkedSession.getInitiatorSessionID().equals(
					initiatorSessionID)
					&& checkedSession.getInitiatorName().equals(initiatorName)) {
				result = checkedSession;
				break;
			}
		}
		// returns null if no session matched
		return result;
	}

	/**
	 * Returns an Iterator over all existing Sessions
	 * 
	 * @return all existing Sessions
	 */
	public Iterator<Session> getSessions() {
		return getSessionMap().values().iterator();
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
			newConnection = new Connection(newSocket.getChannel());
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
		 * If the initiator's socket tries to login, the target must check
		 * whether he wants to create a new Session, or he wants to assign a new
		 * Connection to an existing Session
		 * 
		 * @param parser
		 *            the LoginRequestParser from the first incoming PDU
		 */
		private void processLoginRequest(LoginRequestParser parser) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Processing a new LoginRequest!");
			}
			// prepare Connection
			newConnection.setConnectionID((short) parser.getConnectionID());
			// find appropriate Session
			short tsih = parser.getTargetSessionIdentifyingHandle();
			Session testedSession = getSession(tsih);
			if (testedSession != null) {
				ISID isid = parser.getInitiatorSessionID();
				if (testedSession.getInitiatorSessionID().equals(isid)) {
					assignToSession(testedSession);
				}
			} else {
				createNewSession();
			}
		}

		/**
		 * Create a new Session
		 */
		private void createNewSession() {
			short newUniqueTSIH = tsihFactory.getNewTSIH();
			Session newSession = new Session(newConnection, newUniqueTSIH);
			getSessionMap().put(newUniqueTSIH, newSession);
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Created new Session: TSIH = " + newUniqueTSIH);
			}
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

}
