package org.jscsi.target.conf;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	
	/**
	 * iSCSI's key Value delimiter
	 */
	public static final String KEY_VALUE_DELIMITER = "=";

	private final Map<OperationalTextKey, OperationalTextValue> textValuePairs;
	
	public OperationalTextConfiguration(){
		textValuePairs = new HashMap<OperationalTextKey, OperationalTextValue>();
	}
	
	public static OperationalTextConfiguration createConfiguration(){
		
		return null;
	}
	

	}
