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
		nextTSIH = FIRST_TSIH;
		removedTSIHs = new TreeSet<Short>();
	}

	/**
	 * Returns a new unique TSIH within a targetTest
	 * 
	 * @return a short representing a targetTest session identifying handle
	 */
	public final short getNewTSIH() {
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
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("Maximum TSIH reached: MAX_VALUE = "
								+ Short.MAX_VALUE);
					}
					// FixMe: throw Exception
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
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Tried to remove non existing TSIH: " + tsih);
				}
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

}
