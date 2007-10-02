package connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SocketListener extends Thread {

	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(Target.class);

	/** the lostened port */
	private final int listeningPort;

	/** the accepted Sockets will be assigned to the sockethandler */
	private final ISocketHandler socketHandler;

	private ServerSocket listeningSocket;

	/**
	 * Listens to the specified port and assign every accepted Socket to the
	 * SocketHandler
	 * 
	 * @param port listened port
	 * @param handler ISocketHandler
	 */
	public SocketListener(final int port, final ISocketHandler handler) {
		listeningPort = port;
		socketHandler = handler;

		try {
			listeningSocket = new ServerSocket(listeningPort);
		} catch (IOException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Couldn't create ServerSocket on port "
						+ listeningPort + ":\n" + e.getMessage());
			}
		}

	}

	/**
	 * force stop Listening
	 */
	public void stopListening() {
		try {
			listeningSocket.close();
		} catch (IOException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Exception while closing ServerSocket on port " + listeningPort + ":\n"
						+ e.getMessage());
			}
		}
		interrupt();
	}
	
	/**
	 * start listening
	 */
	@Override
	public void run() {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Started Listening on port " + listeningPort);
		}
		Socket newSocket = null;
		while (!listeningSocket.isClosed()) {
			try {
				if ((newSocket = listeningSocket.accept()) != null) {
					socketHandler.assignSocket(newSocket);
				}
			} catch (IOException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("While listening on port " + listeningPort +", ServerSocket.accept() exception:\n"
						+	"Exception Message: " + e.getMessage());
				}
			}
		}
	}

}
