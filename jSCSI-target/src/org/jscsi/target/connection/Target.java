package org.jscsi.target.connection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.conf.operationalText.OperationalTextConfiguration;
import org.jscsi.target.util.Singleton;


/**
 * An iSCSI target implementation based on the RFC(3720).
 * 
 * @author Marcus Specht
 * 
 */
public class Target {

	private final Lock workingLOCK = new ReentrantLock();

	private final Condition shutdownCondition = workingLOCK.newCondition();

	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(Target.class);

	/** This targets TargetSocketRouter */
	private static TargetSocketRouter SESSION_MANAGER;

	/** all active SocketListener */
	private final Map<Integer, SocketListener> socketListeners;
	
	/** the targets configuration*/
	private final OperationalTextConfiguration config;
	
	
	
	/** */
	public Target(OperationalTextConfiguration conf) {
		config = conf;
		try {
			SESSION_MANAGER = Singleton.getInstance(TargetSocketRouter.class);
		} catch (ClassNotFoundException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Couldn't load " + TargetSocketRouter.class);
			}
		}
		socketListeners = new ConcurrentHashMap<Integer, SocketListener>();
		if (LOGGER.isTraceEnabled()) {
			StringBuffer buffer = new StringBuffer();
			for (int port : getListeningPorts()) {
				buffer.append(port).append(", ");
			}
			LOGGER.trace("Started Target, listening to the following ports: "
					+ buffer);
		}
	}
	
	
	public void awaitShutdown(){
		awaitShutdown(0);
	}
	
	public void awaitShutdown(int seconds) {
		boolean stop = false;
		workingLOCK.lock();
		
		while (!stop) {
			try {
				if(seconds <= 0){
					shutdownCondition.await();
				} else {
					shutdownCondition.await(seconds, TimeUnit.SECONDS);
				}
				stop = true;
			} catch (InterruptedException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER
							.debug("Synchronisation problem while awaiting shutdown");
				}
			}
		}
		workingLOCK.unlock();
	}

	public final void shutdown() {
		shutdownCondition.signal();
	}

	public final int[] getListeningPorts() {
		int[] result = new int[socketListeners.size()];
		int i = 0;
		for (int port : socketListeners.keySet()) {
			result[i++] = port;
		}
		return result;
	}

	public final boolean startListeningOnPort(int port) {
		if (socketListeners.containsKey(port)) {
			return false;
		}
		SocketListener newSocketListener = new SocketListener(port, SESSION_MANAGER);
		socketListeners.put(port, newSocketListener);
		newSocketListener.start();
		return true;
	}

	public final boolean stopListeningOnPort(int port) {
		SocketListener pListener = socketListeners.get(port);
		if (pListener == null) {
			return false;
		}
		pListener.stopListening();
		socketListeners.remove(port);
		return true;
	}



}
