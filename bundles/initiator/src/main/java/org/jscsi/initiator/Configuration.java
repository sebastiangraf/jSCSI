/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jscsi.initiator;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jscsi.exception.ConfigurationException;
import org.jscsi.exception.NoSuchSessionException;
import org.jscsi.exception.OperationalTextKeyException;
import org.jscsi.parser.datasegment.IResultFunction;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.datasegment.ResultFunctionFactory;
import org.jscsi.parser.datasegment.SettingEntry;
import org.jscsi.parser.datasegment.SettingsMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * <h1>Configuration</h1>
 * <p>
 * This class stores all informations, which are set during an iSCSI Session, Connection or are set as the
 * default values. Therefore, this class was implemented as a Singleton Pattern.
 * 
 * @author Volker Wildi, University of Konstanz
 */
public final class Configuration {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The XML element name of the global settings node. */
    private static final String ELEMENT_GLOBAL = "global";

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The XML element name of the target-specific settings nodes. */
    private static final String ELEMENT_TARGET = "target";

    /**
     * The name of the XML attribute of the unique ID within this iSCSI
     * Initiator configuration file.
     */
    private static final String ATTRIBUTE_ID = "id";

    /**
     * The name of the XML attribute of the connecting address of the iSCSI
     * Target.
     */
    private static final String ATTRIBUTE_ADDRESS = "address";

    /**
     * The name of the XML attribute of the connecting port of the iSCSI Target.
     */
    private static final String ATTRIBUTE_PORT = "port";

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The name of the XML attribute of the result function of this setting. */
    private static final String ATTRIBUTE_RESULT = "result";

    /** The name of the XML attribute of the scope of this setting. */
    private static final String ATTRIBUTE_SCOPE = "scope";

    /** The value (session-wide) of the XML attribute scope. */
    private static final String VALUE_SCOPE_SESSION = "Session";

    /** The value (connection-only) of the XML attribute scope. */
    private static final String VALUE_SCOPE_CONNECTION = "Connection";

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * The relative path (to the project) of the main directory of all
     * configuration files.
     */
    private static final File CONFIG_DIR = new File(new StringBuilder("src").append(File.separator).append(
        "main").append(File.separator).append("resources").append(File.separator).toString());

    /**
     * The file name of the XML Schema configuration file for the global
     * settings.
     */
    private static final File CONFIGURATION_SCHEMA_FILE = new File(CONFIG_DIR, "jscsi.xsd");

    /** The file name, which contains all global settings. */
    private static final File CONFIGURATION_CONFIG_FILE = new File(CONFIG_DIR, "jscsi.xml");

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The Log interface. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Contains all global configuration parameters. */
    private final Map<OperationalTextKey, SettingEntry> globalConfig;

    /** Contains all session-wide configuration parameters. */
    private final ConcurrentHashMap<String, SessionConfiguration> sessionConfigs;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a new, empty <code>Configuration</code> object.
     */
    public Configuration() {
        this(new Hashtable<OperationalTextKey, SettingEntry>(),
            new ConcurrentHashMap<String, SessionConfiguration>(0));
    }

    /**
     * Constructor to create a <code>Configuration</code> object with existing
     * data
     */
    public Configuration(final Hashtable<OperationalTextKey, SettingEntry> paramGlobalConfig,
        final ConcurrentHashMap<String, SessionConfiguration> paramConfig) {

        globalConfig = paramGlobalConfig;
        sessionConfigs = paramConfig;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Creates a instance of a <code>Configuration</code> object, which is
     * initialized with the settings from the system-wide configuration file.
     * 
     * @return A <code>Configuration</code> instance with all settings.
     * @throws ConfigurationException
     *             If this operation is supported but failed for some reason.
     */
    public static final Configuration create() throws ConfigurationException {

        return create(CONFIGURATION_SCHEMA_FILE, CONFIGURATION_CONFIG_FILE);
    }

    /**
     * Creates a instance of a <code>Configuration</code> object, which is
     * initialized with the settings from the system-wide configuration file.
     * 
     * @param configSchemaFileName
     *            The file name of the schema to check the configuration file
     *            against.s
     * @param configFileName
     *            The file name of the configuration file to use.
     * @return A <code>Configuration</code> instance with all settings.
     * @throws ConfigurationException
     *             If this operation is supported but failed for some reason.
     * 
     */
    public static final Configuration create(final File configSchemaFileName, final File configFileName)
        throws ConfigurationException {

        final Configuration config = new Configuration();

        final Document doc = config.parse(configSchemaFileName, configFileName);
        config.parseSettings(doc.getDocumentElement());

        return config;
    }

    /**
     * Returns the value of a single parameter, instead of all values.
     * 
     * @param targetName
     *            Name of the iSCSI Target to connect.
     * @param connectionID
     *            The ID of the connection to retrieve.
     * @param textKey
     *            The name of the parameter.
     * @return The value of the given parameter.
     * @throws OperationalTextKeyException
     *             If the given parameter cannot be found.
     */
    public final String getSetting(final String targetName, final int connectionID,
        final OperationalTextKey textKey) throws OperationalTextKeyException {

        try {
            final SessionConfiguration sc;
            synchronized (sessionConfigs) {
                sc = sessionConfigs.get(targetName);

                synchronized (sc) {
                    if (sc != null) {
                        String value = sc.getSetting(connectionID, textKey);
                        if (value != null) {
                            return value;
                        }
                    }
                }
            }
        } catch (OperationalTextKeyException e) {
            // we had not find a session/connection entry, so we have to search
            // in the
            // global settings
        }

        final SettingEntry se;
        synchronized (globalConfig) {
            se = globalConfig.get(textKey);

            synchronized (se) {
                if (se != null) {
                    return se.getValue();
                }
            }
        }

        throw new OperationalTextKeyException("No OperationalTextKey entry found for key: " + textKey.value());
    }

    /**
     * Unifies all parameters (in the right precedence) and returns one <code>SettingsMap</code>. Right order
     * means: default, then the
     * session-wide, and finally the connection-wide valid parameters.
     * 
     * @param targetName
     *            Name of the iSCSI Target to connect.
     * @param connectionID
     *            The ID of the connection to retrieve.
     * @return All unified parameters in one single <code>SettingsMap</code>.
     */
    public final SettingsMap getSettings(final String targetName, final int connectionID) {

        final SettingsMap sm = new SettingsMap();

        // set all default settings
        synchronized (globalConfig) {
            for (Map.Entry<OperationalTextKey, SettingEntry> e : globalConfig.entrySet()) {
                sm.add(e.getKey(), e.getValue().getValue());
            }
        }

        // set all further settings
        final SessionConfiguration sc;
        synchronized (sessionConfigs) {
            sc = sessionConfigs.get(targetName);

            synchronized (sc) {
                if (sc != null) {
                    final SettingsMap furtherSettings = sc.getSettings(connectionID);
                    for (Map.Entry<OperationalTextKey, String> e : furtherSettings.entrySet()) {
                        sm.add(e.getKey(), e.getValue());
                    }
                }
            }
        }

        return sm;
    }

    /**
     * Returns the value of a single parameter. It can only return session and
     * global parameters.
     * 
     * @param targetName
     *            Name of the iSCSI Target to connect.
     * @param textKey
     *            The name of the parameter.
     * @return The value of the given parameter.
     * @throws OperationalTextKeyException
     *             If the given parameter cannot be found.
     */

    public final String getSessionSetting(final String targetName, final OperationalTextKey textKey)
        throws OperationalTextKeyException {

        return getSetting(targetName, -1, textKey);
    }

    /**
     * Returns the <code>InetAddress</code> instance of the connected iSCSI
     * Target.
     * 
     * @param targetName
     *            The name of the iSCSI Target.
     * @return The <code>InetAddress</code> instance of the requested iSCSI
     *         Target.
     * @throws NoSuchSessionException
     *             if a session with this target name is not open.
     */
    public final InetSocketAddress getTargetAddress(final String targetName) throws NoSuchSessionException {

        final SessionConfiguration sc = sessionConfigs.get(targetName);

        if (sc == null) {
            throw new NoSuchSessionException("A session with the ID '" + targetName + "' does not exist.");
        }

        return sc.getInetSocketAddress();
    }

    /**
     * Updates the stored settings of a connection with these values from the
     * response of the iSCSI Target.
     * 
     * @param targetName
     *            The name of the iSCSI Target.
     * @param connectionID
     *            The ID of the connection within this iSCSI Target.
     * @param response
     *            The response settings.
     * @throws NoSuchSessionException
     *             if a session with this target name is not open.
     */
    public final void update(final String targetName, final int connectionID, final SettingsMap response)
        throws NoSuchSessionException {

        final SessionConfiguration sc;
        synchronized (sessionConfigs) {
            sc = sessionConfigs.get(targetName);

            synchronized (sc) {
                if (sc == null) {
                    throw new NoSuchSessionException("A session with the ID '" + targetName
                        + "' does not exist.");
                }

                synchronized (response) {
                    SettingEntry se;
                    for (Map.Entry<OperationalTextKey, String> e : response.entrySet()) {
                        synchronized (globalConfig) {
                            se = globalConfig.get(e.getKey());

                            if (se == null) {
                                if (LOGGER.isWarnEnabled()) {
                                    LOGGER.warn("This key " + e.getKey() + " is not in the globalConfig.");
                                }
                                continue;
                            }

                            synchronized (se) {
                                if (se.getScope().compareTo(VALUE_SCOPE_SESSION) == 0) {
                                    sc.updateSessionSetting(e.getKey(), e.getValue(), se.getResult());
                                } else if (se.getScope().compareTo(VALUE_SCOPE_CONNECTION) == 0) {
                                    sc.updateConnectionSetting(connectionID, e.getKey(), e.getValue(), se
                                        .getResult());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Reads the given configuration file in memory and creates a DOM
     * representation.
     * 
     * @throws SAXException
     *             If this operation is supported but failed for some reason.
     * @throws ParserConfigurationException
     *             If a <code>DocumentBuilder</code> cannot be created which
     *             satisfies the configuration requested.
     * @throws IOException
     *             If any IO errors occur.
     */
    private final Document parse(final File schemaLocation, final File configFile)
        throws ConfigurationException {
        try {
            final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = schemaFactory.newSchema(schemaLocation);

            // create a validator for the document
            final Validator validator = schema.newValidator();

            final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true); // never forget this
            final DocumentBuilder builder = domFactory.newDocumentBuilder();
            final Document doc = builder.parse(configFile);

            final DOMSource source = new DOMSource(doc);
            final DOMResult result = new DOMResult();

            validator.validate(source, result);

            return (Document)result.getNode();
        } catch (final SAXException exc) {
            throw new ConfigurationException(exc);
        } catch (final ParserConfigurationException exc) {
            throw new ConfigurationException(exc);
        } catch (final IOException exc) {
            throw new ConfigurationException(exc);
        }
    }

    /**
     * Parses all settings form the main configuration file.
     * 
     * @param root
     *            The root element of the configuration.
     */
    private final void parseSettings(final Element root) {

        if (root == null) {
            throw new NullPointerException();
        }

        clear();
        parseGlobalSettings(root);
        parseTargetSpecificSettings(root);
    }

    /**
     * Parses all global settings form the main configuration file.
     * 
     * @param root
     *            The root element of the configuration.
     */
    private final void parseGlobalSettings(final Element root) {

        final NodeList globalConfiguration = root.getElementsByTagName(ELEMENT_GLOBAL);

        final ResultFunctionFactory resultFunctionFactory = new ResultFunctionFactory();
        Node parameter;
        NodeList parameters;
        NamedNodeMap attributes;
        SettingEntry key;
        for (int i = 0; i < globalConfiguration.getLength(); i++) {
            parameters = globalConfiguration.item(i).getChildNodes();

            for (int j = 0; j < parameters.getLength(); j++) {
                parameter = parameters.item(j);

                if (parameter.getNodeType() == Node.ELEMENT_NODE) {
                    attributes = parameter.getAttributes();

                    key = new SettingEntry();
                    key.setScope(attributes.getNamedItem(ATTRIBUTE_SCOPE).getNodeValue());
                    key.setResult(resultFunctionFactory.create(attributes.getNamedItem(ATTRIBUTE_RESULT)
                        .getNodeValue()));
                    // key.setSender(attributes.getNamedItem(ATTRIBUTE_SENDER).getNodeValue
                    // ());
                    key.setValue(parameter.getTextContent());

                    synchronized (globalConfig) {
                        globalConfig.put(OperationalTextKey.valueOfEx(parameter.getNodeName()), key);
                    }
                }
            }
        }
    }

    /**
     * Parses all target-specific settings form the main configuration file.
     * 
     * @param root
     *            The root element of the configuration.
     */
    private final void parseTargetSpecificSettings(final Element root) {

        final NodeList targets = root.getElementsByTagName(ELEMENT_TARGET);

        Node target;
        Node parameter;
        NodeList parameters;

        try {
            for (int i = 0; i < targets.getLength(); i++) {
                target = targets.item(i);
                parameters = target.getChildNodes();

                // extract target address and the port (if specified)
                SessionConfiguration sc = new SessionConfiguration();

                sc.setAddress(target.getAttributes().getNamedItem(ATTRIBUTE_ADDRESS).getNodeValue(), Integer
                    .parseInt(target.getAttributes().getNamedItem(ATTRIBUTE_PORT).getNodeValue()));

                // extract the parameters for this target
                for (int j = 0; j < parameters.getLength(); j++) {
                    parameter = parameters.item(j);

                    if (parameter.getNodeType() == Node.ELEMENT_NODE) {
                        sc.addSessionSetting(OperationalTextKey.valueOfEx(parameter.getNodeName()), parameter
                            .getTextContent());
                    }

                }

                synchronized (sessionConfigs) {
                    sessionConfigs.put(target.getAttributes().getNamedItem(ATTRIBUTE_ID).getNodeValue(), sc);
                }
            }
        } catch (UnknownHostException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("The given host is not reachable: " + e.getLocalizedMessage());
            }
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Clears all parameters.
     */
    private final void clear() {

        synchronized (globalConfig) {
            globalConfig.clear();
        }

        synchronized (sessionConfigs) {
            sessionConfigs.clear();
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This class contains a session-wide <code>SettingsMap</code> with one or
     * more connection-specific <code>SettingsMap</code>.
     * 
     * @author Volker Wildi
     */
    private final class SessionConfiguration {

        /** The session-wide settings. */
        private final SettingsMap sessionConfiguration;

        /** The connection-specific settings. */
        private final Map<Integer, SettingsMap> connectionConfiguration;

        /** The <code>InetSocketAddress</code> of the endpoint. */
        private InetSocketAddress inetAddress;

        /**
         * Default constructor to create a new, empty <code>SessionConfiguration</code> object.
         */
        SessionConfiguration() {

            sessionConfiguration = new SettingsMap();
            connectionConfiguration = new LinkedHashMap<Integer, SettingsMap>(0);
        }

        /**
         * Adds a session-wide parameter to this <code>SessionConfiguration</code> object.
         * 
         * @param textKey
         *            The name of the parameter to add
         * @param textValue
         *            The value of the parameter to add.
         */
        final void addSessionSetting(final OperationalTextKey textKey, final String textValue) {

            sessionConfiguration.add(textKey, textValue);
        }

        /**
         * Updates the value of the given <code>OperationTextKey</code> of this
         * session with the response key <code>textValue</code> and the result
         * function.
         * 
         * @param textKey
         *            The <code>OperationalTextKey</code> to update.
         * @param textValue
         *            The value of the response.
         * @param resultFunction
         *            The <code>IResultFunction</code> instance to use to obtain
         *            the result.
         */
        final void updateSessionSetting(final OperationalTextKey textKey, final String textValue,
            final IResultFunction resultFunction) {

            sessionConfiguration.update(textKey, textValue, resultFunction);
        }

        // /**
        // * Adds a connection-specific parameter to this
        // * <code>SessionConfiguration</code> object.
        // *
        // * @param connectionID
        // * The ID of the connection to which this parameter should be
        // * added.
        // * @param textKey
        // * The name of the parameter to add. The name of the
        // * parameter to add.
        // * @param textValue
        // * The value of the parameter to add.
        // */
        // final void addConnectionSetting(final int connectionID,
        // final OperationalTextKey textKey, final String textValue) {
        //
        // SettingsMap sm = connectionConfiguration.get(connectionID);
        // if (sm == null) {
        // sm = new SettingsMap();
        // connectionConfiguration.put(connectionID, sm);
        // }
        //
        // sm.add(textKey, textValue);
        // }

        /**
         * Updates the value of the given <code>OperationTextKey</code> of the
         * given connection within this session with the response key <code>textValue</code> and the result
         * function.
         * 
         * @param connectionID
         *            The ID of the connection.
         * @param textKey
         *            The <code>OperationalTextKey</code> to update.
         * @param textValue
         *            The value of the response.
         * @param resultFunction
         *            The <code>IResultFunction</code> instance to use to obtain
         *            the result.
         */
        final void updateConnectionSetting(final int connectionID, final OperationalTextKey textKey,
            final String textValue, final IResultFunction resultFunction) {

            SettingsMap sm = connectionConfiguration.get(connectionID);
            if (sm == null) {
                sm = new SettingsMap();
                connectionConfiguration.put(connectionID, sm);
            }

            sm.update(textKey, textValue, resultFunction);
        }

        // /**
        // * Returns the value of a key-value pair with the given key for this
        // * session.
        // *
        // * @param textKey
        // * The <code>OperationalTextKey</code> key.
        // * @return The value of the given <code>textKey</code>.
        // * @throws Exception
        // * if any error occurs.
        // */
        // final String getSessionSetting(final OperationalTextKey textKey)
        // throws OperationalTextKeyException {
        //
        // String textValue;
        //
        // do {
        // // look for session-specific information
        // textValue = sessionConfiguration.get(textKey);
        // if (textValue == null) {
        // break;
        // }
        //
        // // look for default information
        // final SettingEntry se = globalConfig.get(textKey);
        // if (se == null) {
        // throw new OperationalTextKeyException("The key "
        // + textKey.value()
        // + " cannot be found in the global configuration.");
        // }
        // } while (false);
        //
        // if (textValue == "") {
        // throw new OperationalTextKeyException("A value of the key "
        // + textKey.value() + " cannot be returned.");
        // } else {
        // return textValue;
        // }
        //
        // }

        // /**
        // * Returns the value of a key-value pair with the given key for the
        // * connection within this session.
        // *
        // * @param connectionID
        // * The ID of the connection.
        // * @param textKey
        // * The <code>OperationalTextKey</code> key.
        // * @return The value of the given <code>textKey</code>.
        // * @throws Exception
        // * if any error occurs.
        // */
        // final String getConnectionSetting(final int connectionID,
        // final OperationalTextKey textKey) throws Exception {
        //
        // final SettingsMap sm = connectionConfiguration.get(connectionID);
        // if (sm != null) {
        // // look for connection-specific information
        // return sm.get(textKey);
        // } else {
        // return getSessionSetting(textKey);
        // }
        // }

        /**
         * Returns a single setting value of a connection (specified by the ID).
         * 
         * @param connectionID
         *            The ID of the connection.
         * @param textKey
         *            The name of the parameter.
         * @return the value of the given parameter of the connection.
         * @throws OperationalTextKeyException
         *             If the given parameter cannot be found.
         */
        final String getSetting(final int connectionID, final OperationalTextKey textKey)
            throws OperationalTextKeyException {

            final SettingsMap sm = connectionConfiguration.get(connectionID);
            if (sm != null) {
                final String value = sm.get(textKey);
                if (value != null) {
                    return value;
                }
            }

            final String value = sessionConfiguration.get(textKey);
            if (value != null) {
                return value;
            }

            throw new OperationalTextKeyException("No OperationalTextKey entry found for key: "
                + textKey.value());
        }

        /**
         * Returns all settings of a connection (specified by the ID).
         * 
         * @param connectionID
         *            The ID of the connection.
         * @return All session-wide and connection-specific settings of the
         *         connection.
         */
        final SettingsMap getSettings(final int connectionID) {

            final SettingsMap sm = new SettingsMap();

            // set all session settings
            for (Map.Entry<OperationalTextKey, String> e : sessionConfiguration.entrySet()) {
                sm.add(e.getKey(), e.getValue());
            }

            // set all connection settings (if any)
            final SettingsMap connectionSettings = connectionConfiguration.get(connectionID);
            if (connectionSettings != null) {

                for (Map.Entry<OperationalTextKey, String> e : connectionSettings.entrySet()) {
                    sm.add(e.getKey(), e.getValue());
                }
            }
            return sm;
        }

        /**
         * Returns the <code>InetAddress</code> of the leading connection of the
         * session.
         * 
         * @return An <code>InetAddress</code> instance.
         */
        final InetSocketAddress getInetSocketAddress() {

            return inetAddress;
        }

        /**
         * Sets the <code>InetAddress</code> of the leading connection to the
         * given value.
         * 
         * @param newInetAddress
         *            The new <code>InetAddress</code> of the leading
         *            connection.
         * @param port
         *            The new Port of the leading connection;
         * @throws UnknownHostException
         *             This exception is thrown, when the host with the given <code>InetAddress</code> is not
         *             reachable.
         */
        final void setAddress(final String newInetAddress, final int port) throws UnknownHostException {

            inetAddress = new InetSocketAddress(newInetAddress, port);
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
