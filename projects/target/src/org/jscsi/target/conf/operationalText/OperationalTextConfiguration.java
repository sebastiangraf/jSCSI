package org.jscsi.target.conf.operationalText;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.naming.ConfigurationException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.conf.operationalText.OperationalTextKey;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.connection.Session;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

	private static final String CONFIG_TYPE_GLOBAL = "Global";

	private static final String CONFIG_TYPE_SESSION = "Session";

	private static final String CONFIG_TYPE_CONNECTION = "Connection";

	/** iSCSI's key value delimiter */
	public static final String KEY_VALUE_DELIMITER = "=";

	/** Delimiter between two key-value-pairs. */
	public static final String PAIR_DELIMITER = "\0";

	private static GlobalConfigParser globalParser;

	private static OperationalTextConfiguration globalConfig;

	private final OperationalTextConfiguration parentConfiguration;

	private final Set<OperationalTextKey> localConfig;

	private final String configType;

	private OperationalTextConfiguration(String configType,
			OperationalTextConfiguration parentConfig)
			throws OperationalTextException {
		if (globalParser == null) {
			globalParser = new GlobalConfigParser(XML_CONF_FILE_ADRESS,
					XSD_VALIDATE_FILE_ADRESS);
		}
		if (globalConfig == null) {
			try {
				globalConfig = parseGlobalConfig();
			} catch (Exception e) {
				throw new OperationalTextException(
						"Couldn't parse global Config: " + e.getMessage());
			}
		}
		this.configType = configType;
		parentConfiguration = parentConfig;
		localConfig = new HashSet<OperationalTextKey>();
	}

	/**
	 * Constructor for global Configuration
	 * 
	 * @param configType
	 *            "CONFIG_TYPE_GLOBAL"
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private OperationalTextConfiguration() {
		if (globalParser == null) {
			globalParser = new GlobalConfigParser(XML_CONF_FILE_ADRESS,
					XSD_VALIDATE_FILE_ADRESS);
		}
		this.configType = CONFIG_TYPE_GLOBAL;
		parentConfiguration = null;
		localConfig = new HashSet<OperationalTextKey>();
	}

	/**
	 * Add a key to the Configuration. Will automatically assign key to the
	 * correct Configuration, i.e. to this or a following parent Configuration
	 * if parent is not globalConfig.
	 * 
	 * @param key
	 * @throws OperationalTextException
	 */
	public void addKey(OperationalTextKey key) throws OperationalTextException {
		if (configType.equals(CONFIG_TYPE_CONNECTION)) {
			if (key.getScope().equals(configType)) {
				addLocalKey(key);
			} else {
				parentConfiguration.addKey(key);
			}
		}
		if (configType.equals(CONFIG_TYPE_SESSION)) {
			if (key.getScope().equals(configType)) {
				addLocalKey(key);
			} else {
				throw new OperationalTextException(
						"Wrong Configuration type, cannot add key to type "
								+ key.getSender());
			}
		}
	}

	/**
	 * Adds key to this OperationalTextConfiguration instance.
	 * 
	 * @param key
	 * @throws OperationalTextException
	 */
	private void addLocalKey(OperationalTextKey key)
			throws OperationalTextException {
		if (!localConfig.contains(key)) {
			localConfig.add(key);
		} else {
			throw new OperationalTextException(
					"Confiduration already contains key: " + key.getKey());
		}
	}

	/**
	 * Get the key in the local or parent Configuration, if parent is not
	 * global.
	 * 
	 * @param key
	 *            String representation of key
	 * @return
	 * @throws ConfigurationException
	 */
	public OperationalTextKey getKey(String key)
			throws OperationalTextException {
		// search the key in the local configuration
		Iterator<OperationalTextKey> keys = getAllKeys().iterator();
		while (keys.hasNext()) {
			OperationalTextKey testedKey = keys.next();
			if (testedKey.getKey().equals(key)) {
				return testedKey;
			}
		}
		// if not in local configuration, check in parent configuration if not
		// global
		if (!parentConfiguration.configType.equals(CONFIG_TYPE_GLOBAL)) {
			return parentConfiguration.getKey(key);
		}
		throw new OperationalTextException(
				"Configuration doesn't contain Key: " + key);
	}

	public Set<OperationalTextKey> getAllKeys() {
		return localConfig;
	}

	/**
	 * Get all keys in the configuration with the specified sender. Use
	 * OperationalTextKey.SENDER_X to specify the wished Sender keys.
	 * 
	 * @param senderType
	 *            OperationalTextKey.SENDER_X
	 * @return Set filled with all key having Key.getSender().equals(senderType)
	 */
	public Set<OperationalTextKey> getSenderKeys(String senderType) {
		Set<OperationalTextKey> result = new HashSet<OperationalTextKey>();
		Iterator<OperationalTextKey> keys = getAllKeys().iterator();
		while (keys.hasNext()) {
			OperationalTextKey testedKey = keys.next();
			if (testedKey.getSender().equals(senderType)) {
				result.add(testedKey);
			}
		}
		if (!parentConfiguration.configType.equals(CONFIG_TYPE_GLOBAL)) {
			return getSenderKeys(senderType, result);
		}
		return result;
	}

	/**
	 * Get all keys in the configuration with the specified sender, store them
	 * in the given result set. Use OperationalTextKey.SENDER_X to specify the
	 * wished Sender keys.
	 * 
	 * @param senderType
	 *            OperationalTextKey.SENDER_X
	 * 
	 * @return Set filled with all key having Key.getSender().equals(senderType)
	 */
	private Set<OperationalTextKey> getSenderKeys(String senderType,
			Set<OperationalTextKey> result) {
		Iterator<OperationalTextKey> keys = getAllKeys().iterator();
		while (keys.hasNext()) {
			OperationalTextKey testedKey = keys.next();
			if (testedKey.getSender().equals(senderType)) {
				result.add(testedKey);
			}
		}
		if (!parentConfiguration.configType.equals(CONFIG_TYPE_GLOBAL)) {
			return getSenderKeys(senderType, result);
		}
		return result;
	}

	/**
	 * Resets all contained key value pairs within this configuration, if
	 * configuration is not global configuration. To reset global configuration,
	 * use the parseGlobalConfig methods.
	 */
	public void reset() {
		if (!configType.equals(OperationalTextConfiguration.CONFIG_TYPE_GLOBAL)) {
			localConfig.clear();
			Iterator<OperationalTextKey> globalKeys = globalConfig.getAllKeys()
					.iterator();
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

	/**
	 * Check if two configurations are equal.
	 * 
	 * @param config
	 * @return true if equals, false else.
	 */
	public boolean equals(OperationalTextConfiguration config) {
		if (config == null) {
			return false;
		}
		if (this.getAllKeys().containsAll(config.getAllKeys())
				&& (this.getAllKeys().size() == config.getAllKeys().size())) {
			return true;
		}
		return false;
	}

	/**
	 * Creates an OperationalTextConfiguration for an iSCSI Session Object.
	 * Configuration will be created holding all parameters with "scope=Session".
	 * 
	 * @param session
	 *            The Configurations Session
	 * @return
	 * @throws OperationalTextException
	 */
	public static OperationalTextConfiguration create(Session session)
			throws OperationalTextException {
		OperationalTextConfiguration result = new OperationalTextConfiguration(
				OperationalTextConfiguration.CONFIG_TYPE_SESSION, globalConfig);
		result.reset();
		return result;
	}

	/**
	 * Creates an OperationalTextConfiguration for an iSCSI Connection Object.
	 * Configuration will be created holding all parameters with "scope=Connection".
	 * @param connection
	 * @return
	 * @throws OperationalTextException
	 */
	public static OperationalTextConfiguration create(Connection connection)
			throws OperationalTextException {
		OperationalTextConfiguration result = new OperationalTextConfiguration(
				OperationalTextConfiguration.CONFIG_TYPE_CONNECTION, connection
						.getReferencedSession().getConfiguration());
		result.reset();
		return result;
	}
	
	
	/**
	 * Creates an empty OperationalTextConfiguration, that can be used
	 * as global configuration, or you can use this method to ensure
	 * every needed Object instance is created to make every method work,
	 * especially static methods.
	 * @return
	 */
	private static OperationalTextConfiguration createEmptyGlobalConfig() {
		return new OperationalTextConfiguration();
	}

	protected static OperationalTextConfiguration getGlobalConfig() {
		return globalConfig;
	}

	public static OperationalTextConfiguration parseGlobalConfig()
			throws OperationalTextException {
		OperationalTextConfiguration newGlobal = parseGlobalConfig(
				XML_CONF_FILE_ADRESS, XSD_VALIDATE_FILE_ADRESS);
		return newGlobal;
	}

	public static OperationalTextConfiguration parseGlobalConfig(
			String xmlFileAdress, String xsdFileAdress)
			throws OperationalTextException {
		// ensure a globalParser instance
		if (globalParser == null) {
			createEmptyGlobalConfig();
		}
		try {
			globalParser.setConfigSource(xmlFileAdress, xsdFileAdress);
			globalConfig = globalParser.parse();
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperationalTextException(
					"Error occured parsing global Config: " + e.getMessage());
		}
		return globalConfig;
	}
	
	/**
	 * Get the String Representation of a key value pair.
	 * @param key
	 * @param value
	 * @return 
	 */
	public static String toString(String key, String value) {
		StringBuffer result = new StringBuffer();
		result.append(key.toString());
		result.append(KEY_VALUE_DELIMITER);
		result.append(value.toString());
		return result.toString();
	}
	
	/**
	 * Get the String representation of a set of key value pairs.
	 * iSCSI standard limiters and characters are used.
	 * @param keySet
	 * @return
	 */
	public static String toString(Set<OperationalTextKey> keySet) {
		StringBuffer result = new StringBuffer();
		Iterator<OperationalTextKey> keys = keySet.iterator();
		while (keys.hasNext()) {
			result.append(toString(keys.next().getKey(), keys.next().getValue()
					.getValue()));
			result.append(PAIR_DELIMITER);
		}
		result.deleteCharAt(result.length() - 1);
		return result.toString();
	}
	
	/**
	 * Get the String representation of all keys contained in one configuration.
	 * iSCSI standard limiters and characters are used.
	 * @param config
	 * @return
	 */
	public static String toString(OperationalTextConfiguration config) {
		return toString(config.getAllKeys());
	}

	private class GlobalConfigParser {

		/** The XML element name of the global node. */
		private static final String ELEMENT_GLOBAL = "global";

		/** The name of the XML attribute of the result function of this key. */
		private static final String ATTRIBUTE_RESULT = "result";

		/** The name of the XML attribute of the scope of this key. */
		private static final String ATTRIBUTE_SCOPE = "scope";

		/** The name of the XML attribute of the sender of this key. */
		private static final String ATTRIBUTE_SENDER = "sender";

		private String XML_FILE_ADRESS;
		private String XSD_FILE_ADRESS;

		public GlobalConfigParser(String xmlFileAdress, String xsdFileAdress) {
			XML_FILE_ADRESS = xmlFileAdress;
			XSD_FILE_ADRESS = xsdFileAdress;
		}

		public GlobalConfigParser() {
			// TODO Auto-generated constructor stub
		}

		public synchronized void setConfigSource(String xmlFileAdress,
				String xsdFileAdress) {
			XML_FILE_ADRESS = xmlFileAdress;
			XSD_FILE_ADRESS = xsdFileAdress;
		}

		public synchronized OperationalTextConfiguration parse()
				throws SAXException, ParserConfigurationException, IOException {
			Document doc = parseXMLDocument(XSD_FILE_ADRESS, XML_FILE_ADRESS);
			if (doc == null) {
				throw new NullPointerException();
			}
			OperationalTextConfiguration result = parseGlobalKeys(doc
					.getDocumentElement());
			for (OperationalTextKey loadedKey : result.getAllKeys()) {
				logTrace("Loaded parameter: "
						+ OperationalTextConfiguration.toString(loadedKey
								.getKey(), loadedKey.getValue().getValue()));
			}
			logTrace("Succesfully parsed global configuration parameter from "
					+ XML_FILE_ADRESS);
			return result;
		}

		/**
		 * Reads the given configuration file in memory and creates a DOM
		 * representation.
		 * 
		 * @throws SAXException
		 *             If this operation is supported but failed for some
		 *             reason.
		 * @throws ParserConfigurationException
		 *             If a <code>DocumentBuilder</code> cannot be created
		 *             which satisfies the configuration requested.
		 * @throws IOException
		 *             If any IO errors occur.
		 */
		private final Document parseXMLDocument(final String schemaFile,
				final String configFile) throws SAXException,
				ParserConfigurationException, IOException {
			logTrace("Loading xml document \"" + configFile
					+ " as iSCSI operational text configuration");
			final SchemaFactory schemaFactory = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			final File schemaLocation = new File(schemaFile);
			final Schema schema = schemaFactory.newSchema(schemaLocation);

			// create a validator for the document
			final Validator validator = schema.newValidator();

			final DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			domFactory.setNamespaceAware(true); // never forget this
			final DocumentBuilder builder = domFactory.newDocumentBuilder();
			final Document doc = builder.parse(new File(configFile));
			logTrace("Loading finshed successful ");
			final DOMSource source = new DOMSource(doc);
			final DOMResult result = new DOMResult();
			logTrace("Validating \"" + configFile + "\" with \"" + schemaFile
					+ "\"");
			validator.validate(source, result);
			logTrace("Validating finished successful");
			return (Document) result.getNode();
		}

		/**
		 * Parses all global settings form the main configuration file.
		 * 
		 * @param root
		 *            The root element of the configuration.
		 */
		private final OperationalTextConfiguration parseGlobalKeys(
				final Element root) {
			logTrace("Parsing xml");
			// new configuration
			OperationalTextConfiguration newGlobalConfig = OperationalTextConfiguration
					.createEmptyGlobalConfig();
			// parse every key from xml to globalConfig
			final NodeList globalConfiguration = root
					.getElementsByTagName(ELEMENT_GLOBAL);
			Node parameter;
			NodeList parameters;
			NamedNodeMap attributes;
			OperationalTextKey key = null;
			OperationalTextValue value = null;
			String newKey;
			String scope;
			String sender = null;
			String resultType;
			String newValue;
			for (int i = 0; i < globalConfiguration.getLength(); i++) {
				parameters = globalConfiguration.item(i).getChildNodes();

				for (int j = 0; j < parameters.getLength(); j++) {
					parameter = parameters.item(j);
					newKey = parameter.getNodeName();
					if (parameter.getNodeType() == Node.ELEMENT_NODE) {
						attributes = parameter.getAttributes();
						scope = attributes.getNamedItem(ATTRIBUTE_SCOPE)
								.getNodeValue();
						resultType = attributes.getNamedItem(ATTRIBUTE_RESULT)
								.getNodeValue();
						sender = attributes.getNamedItem(ATTRIBUTE_SENDER)
								.getNodeValue();
						// key.setSender(attributes.getNamedItem(ATTRIBUTE_SENDER).getNodeValue());
						newValue = parameter.getTextContent();
						try {
							key = OperationalTextKey.create(newKey, scope,
									sender);
						} catch (OperationalTextException e) {
							if (LOGGER.isDebugEnabled()) {
								// this will never happen, but an empty catch
								// block...never :)
								LOGGER
										.debug("Couldn't parse key from iscsi.xml: "
												+ e.getMessage());
							}
						}
						try {
							value = OperationalTextValue.create(newValue,
									resultType);
						} catch (OperationalTextException e) {
							if (LOGGER.isDebugEnabled()) {
								// this will never happen, but an empty catch
								// block...never :)
								LOGGER
										.debug("Couldn't parse key from iscsi.xml: "
												+ e.getMessage());
							}
						}
						key.setValue(value);

						synchronized (newGlobalConfig) {
							try {
								newGlobalConfig.addLocalKey(key);
							} catch (OperationalTextException e) {
								if (LOGGER.isDebugEnabled()) {
									// this will never happen, but an empty
									// catch block...never :)
									LOGGER
											.debug("iscsi.xml contains key twice: "
													+ e.getMessage());
								}
								;
							}
						}
					}
				}
			}
			logTrace("Parsing finished successful, loaded "
					+ newGlobalConfig.getAllKeys().size() + " key value pairs");
			return newGlobalConfig;
		}

	}

	/**
	 * Logs a trace Message, if trace log is enabled within the logging
	 * environment.
	 * 
	 * @param logMessage
	 */
	private static void logTrace(String logMessage) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(" Message: " + logMessage);

		}
	}

	/**
	 * Logs a debug Message , if debug log is enabled within the logging
	 * environment.
	 * 
	 * @param logMessage
	 */
	private static void logDebug(String logMessage) {
		if (LOGGER.isDebugEnabled()) {

			LOGGER.trace(" Message: " + logMessage);
		}
	}

}
