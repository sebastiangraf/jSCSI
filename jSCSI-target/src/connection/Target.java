package connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import utilities.Singleton;

/**
 * An iSCSI target implementation based on the RFC(3720).
 * @author Marcus Specht
 *
 */
public class Target {

	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(Target.class);
	
	/** This targets SessionManager */
	private static SessionManager SESSION_MANAGER;

	/** all active PortListener*/
	private final Map<Integer, PortListener> portListeners;
	
	/**
	 * 
	 */
	public Target() {
		try {
			SESSION_MANAGER = Singleton.getInstance(SessionManager.class);
		} catch (ClassNotFoundException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Couldn't load " + SessionManager.class);
			}
		}
		portListeners = new ConcurrentHashMap<Integer, PortListener>();
		if (LOGGER.isTraceEnabled()) {
			StringBuffer buffer = new StringBuffer();
			for (int port : getListeningPorts()) {
				buffer.append(port).append(", ");
			}
			LOGGER.trace("Started Target, listening to the following ports: "
					+ buffer);
		}
	}

	public final int[] getListeningPorts() {
		int[] result = new int[portListeners.size()];
		int i = 0;
		for (int port : portListeners.keySet()) {
			result[i++] = port;
		}
		return result;
	}

	public final boolean startListeningOnPort(int port) {
		if (portListeners.containsKey(port)) {
			return false;
		}
		PortListener newPortListener = new PortListener(port);
		portListeners.put(port, newPortListener);
		newPortListener.start();
		return true;
	}

	public final boolean stopListeningOnPort(int port) {
		PortListener pListener = portListeners.get(port);
		if (pListener == null) {
			return false;
		}
		pListener.stopListening();
		portListeners.remove(port);
		return true;
	}
	
	/**
	 * The PortListener accepts new incoming Sockets
	 * and forwards them to the <code>SessionManager</code>.
	 * @author apu
	 *
	 */
	private class PortListener extends Thread {
		
		private ServerSocket listeningSocket;
		
		/** The SessionManager's ISocketHandler interface */ 
		private final ISocketHandler handler = SESSION_MANAGER;
		
		/** the listening port */
		private final int PORT;
		
		
		public PortListener(int port) {
			PORT = port;
			try {
				listeningSocket = new ServerSocket(PORT);
			} catch (IOException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Couldn't start listening on port: " + PORT
							+ ": " + e.getMessage());
				}
			}
		}

		/**
		 * Stops listening
		 */
		final void stopListening() {
			try {
				listeningSocket.close();
			} catch (IOException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER
							.debug("Error when stopped listening on port "
									+ PORT);
				}
			}
			interrupt();
		}

		/**
		 * Listen and forward Socket.
		 */
		@Override
		public void run() {
			Socket newSocket = null;
			while (!interrupted()) {
				try {
					if ((newSocket = listeningSocket.accept()) != null) {
						handler.assignSocket(newSocket);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

}
