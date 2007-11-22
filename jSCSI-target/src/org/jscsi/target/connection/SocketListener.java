package org.jscsi.target.connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.Target;

public class SocketListener extends Thread {

	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(SocketListener.class);

	/** the lostened port */
	private final int listeningPort;

	/** the accepted Sockets will be assigned to the sockethandler */
	private final ISocketHandler socketHandler;

	private ServerSocket listeningSocket;

	/**
	 * Listens to the specified port and assign every accepted Socket to the
	 * SocketHandler
	 * 
	 * @param port
	 *            listened port
	 * @param handler
	 *            ISocketHandler
	 */
	public SocketListener(final int port, final ISocketHandler handler) {
		listeningPort = port;
		socketHandler = handler;

		try {
			listeningSocket = new ServerSocket(listeningPort);
		} catch (IOException e) {

			logTrace("Couldn't create ServerSocket on port " + listeningPort
					+ ":\n" + e.getMessage());

		}

	}

	/**
	 * force stop Listening
	 */
	public void stopListening() {
		try {
			listeningSocket.close();
		} catch (IOException e) {
			logDebug("Exception while closing ServerSocket on port "
					+ listeningPort + ":\n" + e.getMessage());
		}
		interrupt();
	}

	/**
	 * start listening
	 */
	@Override
	public void run() {
		logTrace("Started Listening on port " + listeningPort);
		Socket newSocket = null;
		while (!listeningSocket.isClosed()) {
			try {
				if ((newSocket = listeningSocket.accept()) != null) {
					socketHandler.assignSocket(newSocket);
				}
			} catch (IOException e) {
				logDebug("While listening on port " + listeningPort
						+ ", ServerSocket.accept() exception:\n"
						+ "Exception Message: " + e.getMessage());
			}
		}
	}

	/**
	 * Logs a trace Message specific to the session, if trace log is enabled
	 * within the logging environment.
	 * 
	 * @param logMessage
	 */
	private void logTrace(String logMessage) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(" Message: " + logMessage);

		}
	}

	/**
	 * Logs a debug Message specific to the session, if debug log is enabled
	 * within the logging environment.
	 * 
	 * @param logMessage
	 */
	private void logDebug(String logMessage) {
		if (LOGGER.isDebugEnabled()) {

			LOGGER.trace(" Message: " + logMessage);
		}
	}

}
