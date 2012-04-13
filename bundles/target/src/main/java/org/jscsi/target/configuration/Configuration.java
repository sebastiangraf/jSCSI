package org.jscsi.target.configuration;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jscsi.target.scsi.lun.LogicalUnitNumber;
import org.jscsi.target.settings.TextKeyword;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Instances of {@link Configuration} provides access target-wide
 * parameters, variables that are the same across all sessions and connections
 * that do not change after initialization and which play a role during text
 * parameter negotiation. Some of these parameters are provided or can be
 * overridden by the content of an XML file - <code>jscsi-target.xml</code>.
 * 
 * @author Andreas Ergenzinger, University of Konstanz
 */
public class Configuration {

    private static final String TARGET_LIST_ELEMENT_NAME = "TargetList"; // Name of node that contains list of
    // targets

    private static final String TARGET_ELEMENT_NAME = "Target"; // Name for nodes that contain a target
    // Target configuration elements
    private static final String FILE_PATH_ELEMENT_NAME = "FilePath";
    private static final String FILE_LENGTH_ELEMENT_NAME = "FileLength";
    private static final String STORAGE_FILE_ELEMENT_NAME = "StorageFile";

    // Global configuration elements
    private static final String ALLOW_SLOPPY_NEGOTIATION_ELEMENT_NAME = "AllowSloppyNegotiation";
    private static final String PORT_ELEMENT_NAME = "Port";

    private List<TargetInfo> targets;

    /**
     * The <code>TargetAddress</code> parameter (the jSCSI Target's IP address).
     * <p>
     * This parameter is initialized automatically.
     */
    private String targetAddress;

    /**
     * The port used by the jSCSI Target for listening for new connections.
     * <p>
     * The default port number is 3260. This value may be overridden by specifying a different value in the
     * configuration file.
     */
    private int port;

    /**
     * This variable toggles the strictness with which the parameters <code>IFMarkInt</code> and
     * <code>OFMarkInt</code> are processed, when
     * provided by the initiator. Usually the offered values must have to
     * following format: <code>smallInteger~largeInteger</code>, however the
     * jSCSI Initiator sends only single integers as <i>value</i> part of the
     * <i>key-value</i> pairs. Since the value of these two parameters always
     * are <code>Irrelevant</code>, this bug can be ignored without any negative
     * consequences by setting {@link #allowSloppyNegotiation} to <code>true</code> in the configuration file.
     * The default is <code>false</code>.
     */
    private boolean allowSloppyNegotiation;// TODO fix in jSCSI Initiator and
                                           // remove

    /**
     * The <code>TargetPortalGroupTag</code> parameter.
     */
    private final int targetPortalGroupTag = 1;

    /**
     * The Logical Unit Number of the virtual Logical Unit.
     */
    private final LogicalUnitNumber logicalUnitNumber = new LogicalUnitNumber(0L);

    /**
     * The <code>MaxRecvDataSegmentLength</code> parameter for PDUs sent in the
     * out direction (i.e. initiator to target).
     * <p>
     * Since the value of this variable is equal to the specified default value, it does not have to be
     * declared during login.
     */
    private final int outMaxRecvDataSegmentLength = 8192;

    /**
     * The maximum number of consecutive Login PDUs or Text Negotiation PDUs the
     * target will accept in a single sequence.
     * <p>
     * The iSCSI standard does not dictate a minimum or maximum text PDU sequence length, but only suggests to
     * select a value large enough for all expected key-value pairs that might be sent in a single sequence. A
     * limit should be imposed, however, to prevent {@link OutOfMemoryError}s resulting from malicious or
     * accidental text PDU sequences of extreme lengths.
     * <p>
     * Since all common text parameters (plus values) easily fit into a single text PDU with the default data
     * segment size, this value could be set to <code>1</code> without negatively affecting compatibility with
     * most initiators.
     */
    private final int maxRecvTextPduSequenceLength = 4;

    public Configuration() throws IOException {
        port = 3260;
        targetAddress = InetAddress.getLocalHost().getHostAddress();
        targets = new ArrayList<TargetInfo>();
    }

    public int getInMaxRecvTextPduSequenceLength() {
        return maxRecvTextPduSequenceLength;
    }

    public int getOutMaxRecvDataSegmentLength() {
        return outMaxRecvDataSegmentLength;
    }

    public String getTargetAddress() {
        return targetAddress;
    }

    public int getPort() {
        return port;
    }

    public boolean getAllowSloppyNegotiation() {
        return allowSloppyNegotiation;
    }

    public int getTargetPortalGroupTag() {
        return targetPortalGroupTag;
    }

    public LogicalUnitNumber getLogicalUnitNumber() {
        return logicalUnitNumber;
    }

    /**
     * Reads the given configuration file in memory and creates a DOM
     * representation.
     * 
     * @throws SAXException
     *             If this operation is supported but failed for some reason.
     * @throws ParserConfigurationException
     *             If a {@link DocumentBuilder} cannot be created which
     *             satisfies the configuration requested.
     * @throws IOException
     *             If any IO errors occur.
     */
    private Document parse(final File schemaLocation, final File configFile) throws SAXException,
        ParserConfigurationException, IOException {

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
    }

    public TargetInfo[] getTargetInfo() {
        synchronized (targets) {
            TargetInfo[] returnInfo = new TargetInfo[targets.size()];
            returnInfo = targets.toArray(returnInfo);
            return returnInfo;
        }
    }

    public void addTargetInfo(TargetInfo targetInfo) {
        synchronized (targets) {
            targets.add(targetInfo);
        }
    }

    /**
     * The file name of the configuration file.
     */
    private static final String CONFIGURATION_FILE_NAME = "jscsi-target.xml";

    /**
     * The name of the schema file describing the format of the configuration
     * file.
     */
    private static final String SCHEMA_FILE_NAME = "jscsi-target.xsd";

    /**
     * The primary folder containing all configuration files.
     */
    private static final File RELEASE_CONFIGURATION_DIRECTORY = new File(new StringBuilder(System
        .getProperty("user.dir")).append(File.separator).toString());

    /**
     * The back-up folder which will be searched for the <code>xsd</code> and <code>xml</code>files if they
     * cannot be found in the {@link #RELEASE_CONFIGURATION_DIRECTORY}.
     */
    private static final File DEVELOPMENT_CONFIGURATION_DIRECTORY = new File(new StringBuilder(System
        .getProperty("user.dir")).append(File.separator).append("src").append(File.separator).append("main")
        .append(File.separator).append("resources").append(File.separator).toString());

    /**
     * The <code>xsd</code> file containing the schema information for all
     * target-specific settings.
     */
    private static final File CONFIGURATION_FILE = getFile(RELEASE_CONFIGURATION_DIRECTORY,
        DEVELOPMENT_CONFIGURATION_DIRECTORY, CONFIGURATION_FILE_NAME);

    /**
     * The <code>xml</code> file containing all target-specific settings.
     */
    private static final File SCHEMA_FILE = getFile(RELEASE_CONFIGURATION_DIRECTORY,
        DEVELOPMENT_CONFIGURATION_DIRECTORY, SCHEMA_FILE_NAME);

    /**
     * Searches and tries to return a {@link File} with the specified
     * <i>fileName</i> from the given <i>mainDirectory</i>. If this {@link File} does not exist, a
     * {@link File} with the specified <i>fileName</i> from
     * the given <i>backUpDirectory</i> will be returned.
     * 
     * @param mainDirectory
     *            the first directory to search
     * @param backUpDirectory
     *            the folder to use if the first search fails
     * @param fileName
     *            the name of the file without path information
     * @return a {@link File} that may or may not exist
     */
    private static File getFile(final File mainDirectory, final File backUpDirectory, String fileName) {
        final File file = new File(mainDirectory, fileName);
        if (file.exists())
            return file;
        return new File(backUpDirectory, fileName);
    }

    public Document parse() throws SAXException, ParserConfigurationException, IOException {
        return parse(SCHEMA_FILE, CONFIGURATION_FILE);
    }

    public Configuration parseSettings() throws SAXException, ParserConfigurationException, IOException {
        return parseSettings(parse().getDocumentElement());
    }

    /**
     * Parses all settings form the main configuration file.
     * 
     * @param root
     *            The root element of the configuration.
     */
    public Configuration parseSettings(final Element root) throws IOException {
        // TargetName
        Configuration returnConfiguration = new Configuration();
        Element targetListNode = (Element)root.getElementsByTagName(TARGET_LIST_ELEMENT_NAME).item(0);
        NodeList targetList = targetListNode.getElementsByTagName(TARGET_ELEMENT_NAME);
        for (int curTargetNum = 0; curTargetNum < targetList.getLength(); curTargetNum++) {
            TargetInfo curTargetInfo = parseTargetElement((Element)targetList.item(curTargetNum));
            returnConfiguration.addTargetInfo(curTargetInfo);
        }

        // else it is null

        // port
        if (root.getElementsByTagName(PORT_ELEMENT_NAME).getLength() > 0)
            returnConfiguration.port =
                Integer.parseInt(root.getElementsByTagName(PORT_ELEMENT_NAME).item(0).getTextContent());
        else
            returnConfiguration.port = 3260;

        // support sloppy text parameter negotiation (i.e. the jSCSI Initiator)?
        final Node allowSloppyNegotiationNode =
            root.getElementsByTagName(ALLOW_SLOPPY_NEGOTIATION_ELEMENT_NAME).item(0);
        if (allowSloppyNegotiationNode == null)
            returnConfiguration.allowSloppyNegotiation = false;
        else
            returnConfiguration.allowSloppyNegotiation =
                Boolean.parseBoolean(allowSloppyNegotiationNode.getTextContent());

        return returnConfiguration;
    }

    public TargetInfo parseTargetElement(Element targetElement) {
        String targetName =
            targetElement.getElementsByTagName(TextKeyword.TARGET_NAME).item(0).getTextContent();
        // TargetAlias (optional)
        Node targetAliasNode = targetElement.getElementsByTagName(TextKeyword.TARGET_ALIAS).item(0);
        String targetAlias = "";
        if (targetAliasNode != null)
            targetAlias = targetAliasNode.getTextContent();

        NodeList fileProperties =
            targetElement.getElementsByTagName(STORAGE_FILE_ELEMENT_NAME).item(0).getChildNodes();
        String storageFilePath = null;
        long storageLength = -1;

        for (int i = 0; i < fileProperties.getLength(); ++i) {
            if (FILE_PATH_ELEMENT_NAME.equals(fileProperties.item(i).getNodeName())) {
                storageFilePath = fileProperties.item(i).getTextContent();
            } else if (FILE_LENGTH_ELEMENT_NAME.equals(fileProperties.item(i).getNodeName())) {
                storageLength =
                    Math.round(((Double.valueOf(fileProperties.item(i).getTextContent())) * Math.pow(1024, 3)));
            }
        }

        return new StorageFileTargetInfo(targetName, targetAlias, new File(storageFilePath), storageLength);
    }

}
