package org.jscsi.target.connection;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An iSCSI targetTest environment generates a unique identifier for every existing
 * Session, the TargetTest Session Identifying Handle (TSIH). This Factory produces
 * unique TSIHs and produced TSIHs can be removed.
 * 
 * @author Marcus Specht
 * 
 */
public class TSIHFactory {

	/** The Logger interface. */
	private static final Log LOGGER = LogFactory.getLog(TSIHFactory.class);

	/** The next TSIH if there are no removed ones */
	private static short nextTSIH;

	/** Can't be zero, reeved*/
	private final short FIRST_TSIH = 1;

	/** The set with already removed TSIHs */
	private static SortedSet<Short> removedTSIHs;

	public TSIHFactory() {
		// nextTSIH, i.e. here first TSIH could be Short.MIN_VALUE too, but you
		// know...
		logTrace("Initialized target session identifying handle factory - TSIHFactory");
		nextTSIH = FIRST_TSIH;
		removedTSIHs = new TreeSet<Short>();
	}

	/**
	 * Returns a new unique TSIH within a targetTest
	 * 
	 * @return a short representing a targetTest session identifying handle
	 * @throws Exception 
	 */
	public final short getNewTSIH() throws Exception {
		synchronized (this) {
			short result = 0;
			// if there are some already removed TSIHs, use one of them,
			// else take a new one
			if (removedTSIHs.size() > 0) {
				result = removedTSIHs.first();
				removedTSIHs.remove(result);
			} else {
				// are there more TSIHs allowed?
				if (nextTSIH <= Short.MAX_VALUE) {
					result = nextTSIH;
					++nextTSIH;
				} else {
					logTrace("Maximum TSIH reached: MAX_VALUE = "
							+ Short.MAX_VALUE);

					throw new Exception("No more resources, maximum possible number of sessions reached");
				}
			}
			return result;
		}
	}

	/**
	 * Removes a used "targetTest session identifying handle".
	 * 
	 * @param tsih
	 */
	public final void removeTSIH(final short tsih) {
		synchronized (this) {
			if ((tsih < nextTSIH) && (tsih >= FIRST_TSIH)
					&& (!removedTSIHs.contains(tsih))) {
				removedTSIHs.add(tsih);
			} else {
				logTrace("Tried to remove non existing TSIH: " + tsih);

			}
			clean();
		}
	}

	/**
	 * Cleans up the removedTSIHs Set, i.e. removes all last TSIHs from
	 * removedTSIHs that are (in exact downward order) one less than nextTSIH.
	 */
	private final void clean() {
		while ((removedTSIHs.size() != 0)
				&& (removedTSIHs.last().compareTo(nextTSIH) == -1)) {
			nextTSIH = removedTSIHs.last();
			removedTSIHs.remove(removedTSIHs.last());
		}

	}

	/**
	 * Logs a trace Message, if trace log is enabled
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
	 * Logs a debug Message , if debug log is enabled
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
