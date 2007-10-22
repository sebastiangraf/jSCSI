package org.jscsi.target.conf;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.parser.datasegment.ResultFunctionFactory;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.connection.Session;
import org.jscsi.target.util.Singleton;

/**
 * Describes a standard iSCSI key-value-pair used in iSCSI. Multiple Values are
 * supported. Only value representation is String. E.g.: "KEY=VALUE"
 * "KEY=VALUE,VALUE"
 * 
 * @author Marcus Specht
 * 
 */
public class OperationalTextConfiguration {

	/** The Log interface. */
	private static final Log LOGGER = LogFactory
			.getLog(OperationalTextConfiguration.class);

	/**
	 * The relative path (to the project) of the main directory of all
	 * configuration files.
	 */
	private static final String CONFIGURATION_DIR = "conf/";

	/**
	 * The file name of the XML Schema configuration file for the global
	 * settings.
	 */
	private static final String XSD_VALIDATE_FILE_ADRESS = CONFIGURATION_DIR
			+ "iscsi.xsd";

	/** The file name, which contains all global settings. */
	private static final String XML_CONF_FILE_ADRESS = CONFIGURATION_DIR
			+ "iscsi.xml";

	private static final String GLOBAL_WIDE = "global";

	private static final String SESSION_WIDE = "session";

	private static final String CONNECTION_WIDE = "connection";

	/** iSCSI's key value delimiter */
	public static final String KEY_VALUE_DELIMITER = "=";

	/** Delimiter between two key-value-pairs. */
	public static final String PAIR_DELIMITER = "\0";

	private static GlobalConfigParser globalParser;

	private static OperationalTextConfiguration globalConfig;

	private final OperationalTextConfiguration parentConfiguration;

	private final Map<OperationalTextKey, OperationalTextValue> localConfig;

	private final String configType;

	private OperationalTextConfiguration(String configType,
			OperationalTextConfiguration parentConfig){
		if (globalParser == null) {
			globalParser = new GlobalConfigParser(XML_CONF_FILE_ADRESS,
					XSD_VALIDATE_FILE_ADRESS);
		}
		if (globalConfig == null) {
			globalConfig = parseGlobalConfig();
		}
		this.configType = configType;
		
		localConfig = new HashMap<OperationalTextKey, OperationalTextValue>();
		parentConfiguration = parentConfig;
	}

	private OperationalTextConfiguration getParentConfiguration() {
		return parentConfiguration;
	}

	private String getConfigType() {
		return configType;
	}

	private Map<OperationalTextKey, OperationalTextValue> getConfigMap() {
		return localConfig;
	}


	/**
	 * 
	 * @param key
	 * @return
	 */
	public OperationalTextKey getKey(String key) {
		return null;
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public OperationalTextValue getValue(String value) {
		return null;
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public OperationalTextValue getValue(OperationalTextValue value) {
		return null;
	}

	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void update(String key, String value) {

	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void update(OperationalTextKey key, OperationalTextValue value) {

	}
	
	/**
	 * 
	 */
	public void reset() {

	}

	public void reset(OperationalTextKey key) {

	}

	public boolean equals(OperationalTextConfiguration config) {
		if (this.getConfigMap().entrySet().containsAll(
				config.getConfigMap().entrySet())) {
			return true;
		}
		return false;
	}

	public static OperationalTextConfiguration create(Session session) throws OperationalTextException {
		OperationalTextConfiguration result = new OperationalTextConfiguration(
				OperationalTextConfiguration.SESSION_WIDE, globalConfig);
		return result;
	}

	public static OperationalTextConfiguration create(Connection connection) {
		OperationalTextConfiguration result = new OperationalTextConfiguration(
				OperationalTextConfiguration.CONNECTION_WIDE, connection.getConfiguration());
		return result;
	}

	public static OperationalTextConfiguration getGlobalConfig() {
		if (globalConfig == null) {
			globalConfig = parseGlobalConfig();
		}
		return globalConfig;
	}

	public static OperationalTextConfiguration parseGlobalConfig() {
		return globalParser.parse();
	}

	public static String toString(OperationalTextKey key,
			OperationalTextValue value) {
		StringBuffer result = new StringBuffer();
		result.append(key.toString());
		result.append(KEY_VALUE_DELIMITER);
		result.append(value.toString());
		return result.toString();
	}

	public static String toString(OperationalTextConfiguration config) {
		StringBuffer result = new StringBuffer();
		Iterator<Entry<OperationalTextKey, OperationalTextValue>> pairs = config
				.getConfigMap().entrySet().iterator();
		while (pairs.hasNext()) {
			result.append(toString(pairs.next().getKey(), pairs.next()
					.getValue()));
			result.append(PAIR_DELIMITER);
		}
		result.deleteCharAt(result.length() - 1);
		return result.toString();
	}

	private class GlobalConfigParser {

		private final String XML_FILE_ADRESS;
		private final String XSD_FILE_ADRESS;

		public GlobalConfigParser(String xmlFileAdress, String xsdFileAdress) {
			XML_FILE_ADRESS = xmlFileAdress;
			XSD_FILE_ADRESS = xsdFileAdress;
		}

		public synchronized OperationalTextConfiguration parse() {
			//don't forget to set GlOBAL_WIDE;
			return null;
		}

	}

}
