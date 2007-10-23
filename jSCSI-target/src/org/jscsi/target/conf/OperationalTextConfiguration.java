package org.jscsi.target.conf;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.naming.ConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.parser.datasegment.ResultFunctionFactory;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.connection.Session;

/**
 * The OperationalTextConfiguration represents a standard system to work with
 * iSCSI text parameter. The global used iSCSI parameters are stored within a
 * xml file and will be loaded at startup. Connections and Sessions use these
 * parameters to negotiate a working I-T-Nexus.
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

	private static final String CONFIG_TYPE_GLOBAL = "global";

	private static final String CONFIG_TYPE_SESSION = "session";

	private static final String CONFIG_TYPE_CONNECTION = "connection";

	/** iSCSI's key value delimiter */
	public static final String KEY_VALUE_DELIMITER = "=";

	/** Delimiter between two key-value-pairs. */
	public static final String PAIR_DELIMITER = "\0";

	private static GlobalConfigParser globalParser;

	private static OperationalTextConfiguration globalConfig;

	private final Set<OperationalTextKey> localConfig;

	private final String configType;

	private OperationalTextConfiguration(String configType,
			OperationalTextConfiguration parentConfig) {
		if (globalParser == null) {
			globalParser = new GlobalConfigParser(XML_CONF_FILE_ADRESS,
					XSD_VALIDATE_FILE_ADRESS);
		}
		if (globalConfig == null) {
			globalConfig = parseGlobalConfig();
		}
		this.configType = configType;

		localConfig = new HashSet<OperationalTextKey>();
	}

	private Set<OperationalTextKey> getConfigSet() {
		return localConfig;
	}

	public void addKey(OperationalTextKey key) throws OperationalTextException {
		if (!localConfig.contains(key)) {
			localConfig.add(key);
		} else {
			throw new OperationalTextException(
					"Confiduration already contains key: " + key.getKey());
		}

	}

	/**
	 * 
	 * @param key
	 * @return
	 * @throws ConfigurationException
	 */
	public OperationalTextKey getKey(String key)
			throws OperationalTextException {
		Iterator<OperationalTextKey> keys = getConfigSet().iterator();
		while (keys.hasNext()) {
			OperationalTextKey testedKey = keys.next();
			if (testedKey.getKey().equals(key)) {
				return testedKey;
			}
		}
		throw new OperationalTextException(
				"Configuration doesn't contain Key: " + key);
	}

	/**
	 * 
	 */
	public void reset() {
		if (!configType.equals(OperationalTextConfiguration.CONFIG_TYPE_GLOBAL)) {
			localConfig.clear();
			Iterator<OperationalTextKey> globalKeys = globalConfig
					.getConfigSet().iterator();
			while (globalKeys.hasNext()) {
				OperationalTextKey newKey = globalKeys.next();
				if (configType
						.equals(OperationalTextConfiguration.CONFIG_TYPE_CONNECTION)) {
					if (newKey.getScope().equals(
							OperationalTextKey.SCOPE_CONNECTION_WIDE)) {
						localConfig.add(OperationalTextKey.copy(newKey));
					}
				}
				if (configType
						.equals(OperationalTextConfiguration.CONFIG_TYPE_SESSION)) {
					if (newKey.getScope().equals(
							OperationalTextKey.SCOPE_SESSION_WIDE)) {
						localConfig.add(OperationalTextKey.copy(newKey));
					}
				}
			}
		}
	}

	public void reset(String key) {

	}

	public void delete() {

	}

	public boolean equals(OperationalTextConfiguration config) {
		if (this.getConfigSet().containsAll(config.getConfigSet())) {
			return true;
		}
		return false;
	}

	public static OperationalTextConfiguration create(Session session) {
		OperationalTextConfiguration result = new OperationalTextConfiguration(
				OperationalTextConfiguration.CONFIG_TYPE_SESSION, globalConfig);
		result.reset();
		return result;
	}

	public static OperationalTextConfiguration create(Connection connection) {
		OperationalTextConfiguration result = new OperationalTextConfiguration(
				OperationalTextConfiguration.CONFIG_TYPE_CONNECTION, connection
						.getReferencedSession().getConfiguration());
		result.reset();
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

	public static String toString(String key, String value) {
		StringBuffer result = new StringBuffer();
		result.append(key.toString());
		result.append(KEY_VALUE_DELIMITER);
		result.append(value.toString());
		return result.toString();
	}

	public static String toString(OperationalTextConfiguration config) {
		StringBuffer result = new StringBuffer();
		Iterator<OperationalTextKey> pairs = config.getConfigSet().iterator();
		while (pairs.hasNext()) {
			result.append(toString(pairs.next().getKey(), pairs.next()
					.getValue().getString()));
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
			// don't forget to set GlOBAL_WIDE;
			return null;
		}

	}

}
