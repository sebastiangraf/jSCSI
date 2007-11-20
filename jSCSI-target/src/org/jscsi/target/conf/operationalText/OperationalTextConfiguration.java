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

	private static final String CONFIG_TYPE_GLOBAL = "global";

	private static final String CONFIG_TYPE_SESSION = "session";

	private static final String CONFIG_TYPE_CONNECTION = "connection";

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
		if (parentConfiguration.configType.equals(CONFIG_TYPE_GLOBAL)) {
			return parentConfiguration.getKey(key);
		}
		throw new OperationalTextException(
				"Configuration doesn't contain Key: " + key);
	}

	/**
	 * 
	 * @param key
	 * @return
	 * @throws ConfigurationException
	 */
	public Set<OperationalTextKey> getSenderKeys(String targetOrInitiator)
			throws OperationalTextException {
		Set<OperationalTextKey> result = new HashSet<OperationalTextKey>();
		Iterator<OperationalTextKey> keys = getConfigSet().iterator();
		while (keys.hasNext()) {
			OperationalTextKey testedKey = keys.next();
			if (testedKey.getSender().equals(targetOrInitiator)) {
				result.add(testedKey);
			}
		}
		if (!parentConfiguration.configType.equals(CONFIG_TYPE_GLOBAL)) {
			return getSenderKeys(targetOrInitiator, result);
		}
		throw new OperationalTextException(
				"Configuration doesn't contain keys with sender: "
						+ targetOrInitiator);
	}

	/**
	 * 
	 * @param key
	 * @return
	 * @throws ConfigurationException
	 */
	private Set<OperationalTextKey> getSenderKeys(String targetOrInitiator,
			Set<OperationalTextKey> result) throws OperationalTextException {
		Iterator<OperationalTextKey> keys = getConfigSet().iterator();
		while (keys.hasNext()) {
			OperationalTextKey testedKey = keys.next();
			if (testedKey.getSender().equals(targetOrInitiator)) {
				result.add(testedKey);
			}
		}
		if (!parentConfiguration.configType.equals(CONFIG_TYPE_GLOBAL)) {
			return getSenderKeys(targetOrInitiator, result);
		}
		throw new OperationalTextException(
				"Configuration doesn't contain keys with sender: "
						+ targetOrInitiator);
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
		if (this.getConfigSet().containsAll(config.getConfigSet())
				&& (this.getConfigSet().size() == config.getConfigSet().size())) {
			return true;
		}
		return false;
	}

	/**
	 * Creates an OperationalTextConfiguration for an iSCSI Session Object.
	 * Configuration will be created holding all parameters with "scope=sessio".
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

	public static OperationalTextConfiguration create(Connection connection)
			throws OperationalTextException {
		OperationalTextConfiguration result = new OperationalTextConfiguration(
				OperationalTextConfiguration.CONFIG_TYPE_CONNECTION, connection
						.getReferencedSession().getConfiguration());
		result.reset();
		return result;
	}

	public static OperationalTextConfiguration createGlobalConfig() throws OperationalTextException{
		createEmptyGlobalConfig();
		try {
			globalConfig = parseGlobalConfig();
		} catch (Exception e){
			throw new OperationalTextException("Error occured parsing global Config: " + e.getMessage());
		}
		return globalConfig;
		
	}
	
	private static OperationalTextConfiguration createEmptyGlobalConfig() {
		return new OperationalTextConfiguration();
	}
	
	

	protected static OperationalTextConfiguration getGlobalConfig() {
		return globalConfig;
	}

	public static OperationalTextConfiguration parseGlobalConfig()
			throws SAXException, ParserConfigurationException, IOException {
		return globalParser.parse();
	}

	public static String toString(String key, String value) {
		StringBuffer result = new StringBuffer();
		result.append(key.toString());
		result.append(KEY_VALUE_DELIMITER);
		result.append(value.toString());
		return result.toString();
	}

	public static String toString(Set<OperationalTextKey> keySet) {
		StringBuffer result = new StringBuffer();
		Iterator<OperationalTextKey> keys = keySet.iterator();
		while (keys.hasNext()) {
			result.append(toString(keys.next().getKey(), keys.next().getValue()
					.getString()));
			result.append(PAIR_DELIMITER);
		}
		result.deleteCharAt(result.length() - 1);
		return result.toString();
	}

	public static String toString(OperationalTextConfiguration config) {
		return toString(config.getConfigSet());
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

		private final String XML_FILE_ADRESS;
		private final String XSD_FILE_ADRESS;

		public GlobalConfigParser(String xmlFileAdress, String xsdFileAdress) {
			XML_FILE_ADRESS = xmlFileAdress;
			XSD_FILE_ADRESS = xsdFileAdress;
		}

		public synchronized OperationalTextConfiguration parse()
				throws SAXException, ParserConfigurationException, IOException {
			Element root = (Element) parseXMLDocument(XSD_FILE_ADRESS,
					XML_FILE_ADRESS);
			if (root == null) {
				throw new NullPointerException();
			}
			return parseGlobalKeys(root);
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

			final DOMSource source = new DOMSource(doc);
			final DOMResult result = new DOMResult();

			validator.validate(source, result);
			
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
										.debug("Couldn't parse key from iscsi.xsd: "
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

						synchronized (globalConfig) {
							try {
								newGlobalConfig.addKey(key);
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
			return newGlobalConfig;
		}

	}

}
