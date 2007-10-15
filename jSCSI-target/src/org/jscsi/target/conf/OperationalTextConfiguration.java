package org.jscsi.target.conf;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.parser.datasegment.ResultFunctionFactory;
import org.jscsi.target.util.Singleton;

/**
 * Describes a standard iSCSI key-value-pair used in iSCSI. Multiple Values are
 * supported. Only value representation is String.
 * E.g.:
 * "KEY=VALUE"
 * "KEY=VALUE,VALUE"
 * @author Marcus Specht
 * 
 */
public class OperationalTextConfiguration {

	/** The Log interface. */
	private static final Log LOGGER = LogFactory.getLog(OperationalTextConfiguration.class);
	
	/** The ResultFunctionFactory */
	private final ResultFunctionFactory rfFactory = Singleton.getInstance(ResultFunctionFactory.class);
	
	/**
	 * iSCSI's key Value delimiter
	 */
	public static final String KEY_VALUE_DELIMITER = "=";

	/**
	 * iSCSI's delimiter between multiple values used in value lists
	 */
	public static final String VALUE_VALUE_DELIMITER = ",";
	
	/** iSCSI's delimiter between two following key-value-pairs*/
	public static final String PAIR_PAIR_DELIMITER = "\0";
	
	public static final String SCOPE = "scope";
	
	public static final String SCOPE_CONNECTION_WIDE = "connection";
	
	public static final String SCOPE_SESSION_WIDE = "session";
	
	public static final String SENDER = "sender";
	
	public static final String SENDER_TARGET = "target";
	
	public static final String SENDER_INITIATOR = "initiator";
	
	public static final String SENDER_BOTH = "both";
	
	/** String representing the key*/
	private String key;

	/** String representing the value*/
	private String value;
	
	

	}
