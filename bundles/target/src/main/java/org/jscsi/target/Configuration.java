package org.jscsi.target;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.jscsi.target.scsi.lun.LogicalUnitNumber;
import org.jscsi.target.settings.TextKeyword;
import org.jscsi.target.storage.IStorageModule;
import org.jscsi.target.storage.JCloudsStorageModule;
import org.jscsi.target.storage.RandomAccessStorageModule;
import org.jscsi.target.storage.SynchronizedRandomAccessStorageModule;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;


/**
 * Instances of {@link Configuration} provides access target-wide parameters, variables that are the same across all
 * sessions and connections that do not change after initialization and which play a role during text parameter
 * negotiation. Some of these parameters are provided or can be overridden by the content of an XML file -
 * <code>jscsi-target.xml</code>.
 *
 * @author Andreas Ergenzinger, University of Konstanz
 * @author CHEN Qingcan
 */
public class Configuration {

    public static final String ELEMENT_TARGET_LIST = "TargetList"; // Name of
                                                                   // node that
                                                                   // contains
                                                                   // list of
    // targets

    public static final String ELEMENT_TARGET = "Target"; // Name for nodes
                                                          // that contain a
                                                          // target
    // Target configuration elements
    public static final String ELEMENT_SYNCFILESTORAGE = "SyncFileStorage";
    public static final String ELEMENT_ASYNCFILESTORAGE = "AsyncFileStorage";
    public static final String ELEMENT_JCLOUDSSTORAGE = "JCloudsStorage";
    public static final String ELEMENT_FILESTORAGE = "FileStorage";
    public static final String ELEMENT_EXTENDEDSTORAGE = "ExtendedStorage";
    public static final String ELEMENT_PATH= "Path";
    public static final String ELEMENT_CREATE = "Create";
    public static final String ATTRIBUTE_SIZE = "size";
    public static final String ATTRIBUTE_CLASS = "class";

    // Global configuration elements
    public static final String ELEMENT_ALLOWSLOPPYNEGOTIATION = "AllowSloppyNegotiation";
    public static final String ELEMENT_PORT = "Port";
    public static final String ELEMENT_EXTERNAL_PORT = "ExternalPort";
    public static final String ELEMENT_ADDRESS = "Address";
    public static final String ELEMENT_EXTERNAL_ADDRESS = "ExternalAddress";

    // Vendor info
    public static final String ELEMENT_VENDOR_ID = "VendorID";
    public static final String ELEMENT_PRODUCT_ID = "ProductID";
    public static final String ELEMENT_PRODUCT_REVISION = "ProductRevisionLevel";

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * The relative path (to the project) of the main directory of all configuration files.
     */
    private static final File CONFIG_DIR = new File(new StringBuilder("src").append(File.separator).append("main").append(File.separator).append("resources").append(File.separator).toString());

    /**
     * The file name of the XML Schema configuration file for the global settings.
     */
    public static final File CONFIGURATION_SCHEMA_FILE = new File(CONFIG_DIR, "jscsi-target.xsd");

    /** The file name, which contains all global settings. */
    public static final File CONFIGURATION_CONFIG_FILE = new File(CONFIG_DIR, "jscsi-target.xml");

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    protected final List<Target> targets;

    /**
     * The <code>TargetAddress</code> parameter (the jSCSI Target's IP address).
     * <p>
     * This parameter is initialized automatically.
     */
    protected String targetAddress;

    /**
     * The target address which will be returned by the discovery.
     * <p>
     * Could be different from the targetAddress in case of external
     */
    protected String externalTargetAddress;

    /**
     * The port used by the jSCSI Target for listening for new connections.
     * <p>
     * The default port number is 3260. This value may be overridden by specifying a different value in the
     * configuration file.
     */
    protected int port;

    /**
     * The port used to connect to service. Could be different from the port in case of api gateway.
     */
    protected int externalPort;

    /**
     * This variable toggles the strictness with which the parameters <code>IFMarkInt</code> and <code>OFMarkInt</code>
     * are processed, when provided by the initiator. Usually the offered values must have to following format:
     * <code>smallInteger~largeInteger</code>, however the jSCSI Initiator sends only single integers as <i>value</i>
     * part of the <i>key-value</i> pairs. Since the value of these two parameters always are <code>Irrelevant</code>,
     * this bug can be ignored without any negative consequences by setting {@link #allowSloppyNegotiation} to
     * <code>true</code> in the configuration file. The default is <code>false</code>.
     */
    protected boolean allowSloppyNegotiation;// TODO fix in jSCSI Initiator and
                                             // remove

    /**
     * The <code>TargetPortalGroupTag</code> parameter.
     */
    protected final int targetPortalGroupTag = 1;

    /**
     * The Logical Unit Number of the virtual Logical Unit.
     */
    protected final LogicalUnitNumber logicalUnitNumber = new LogicalUnitNumber(0L);

    /**
     * The <code>MaxRecvDataSegmentLength</code> parameter for PDUs sent in the out direction (i.e. initiator to
     * target).
     * <p>
     * Since the value of this variable is equal to the specified default value, it does not have to be declared during
     * login.
     */
    protected final int outMaxRecvDataSegmentLength = 8192;

    /**
     * The maximum number of consecutive Login PDUs or Text Negotiation PDUs the target will accept in a single
     * sequence.
     * <p>
     * The iSCSI standard does not dictate a minimum or maximum text PDU sequence length, but only suggests to select a
     * value large enough for all expected key-value pairs that might be sent in a single sequence. A limit should be
     * imposed, however, to prevent {@link OutOfMemoryError}s resulting from malicious or accidental text PDU sequences
     * of extreme lengths.
     * <p>
     * Since all common text parameters (plus values) easily fit into a single text PDU with the default data segment
     * size, this value could be set to <code>1</code> without negatively affecting compatibility with most initiators.
     */
    private final int maxRecvTextPduSequenceLength = 4;

    /**
     * VENDOR IDENTIFICATION field returned in Standard INQUIRY data.
     */
    protected String idVendor = "";

    /**
     * PRODUCT IDENTIFICATION field returned in Standard INQUIRY data.
     */
    protected String idProduct = "";

    /**
     * PRODUCT REVISION LEVEL field returned in Standard INQUIRY data.
     */
    protected String revProduct = "";

    public Configuration(final String pTargetAddress, String externalTargetAddress, int externalPort) throws IOException {
        this.port = 3260;
        this.externalPort = externalPort;
        this.externalTargetAddress = externalTargetAddress;
        this.targetAddress = pTargetAddress;
        targets = new ArrayList<>();
    }

    public Configuration (final String pTargetAddress) throws IOException {
        this(defaultTargetAddress(pTargetAddress), defaultTargetAddress(pTargetAddress), 3260);
    }

    private static String defaultTargetAddress(String pTargetAddress) throws UnknownHostException {
        if (Strings.isNullOrEmpty (pTargetAddress)) {
            return InetAddress.getLocalHost().getHostAddress();

        } else {
            return pTargetAddress;
        }
    }

    public int getInMaxRecvTextPduSequenceLength () {
        return maxRecvTextPduSequenceLength;
    }

    public int getOutMaxRecvDataSegmentLength () {
        return outMaxRecvDataSegmentLength;
    }

    public String getTargetAddress () {
        return targetAddress;
    }

    public int getPort () {
        return port;
    }

    public boolean getAllowSloppyNegotiation () {
        return allowSloppyNegotiation;
    }

    public int getTargetPortalGroupTag () {
        return targetPortalGroupTag;
    }

    public LogicalUnitNumber getLogicalUnitNumber () {
        return logicalUnitNumber;
    }

    public List<Target> getTargets () {
        return targets;
    }

    public static Configuration create (final String pTargetAddress) throws SAXException , ParserConfigurationException , IOException {
        return create(CONFIGURATION_SCHEMA_FILE, CONFIGURATION_CONFIG_FILE, pTargetAddress);
    }

    /**
     * Reads the given configuration file in memory and creates a DOM representation.
     *
     * @param  pTargetAddress   Get from configuration XML if this parameter is null or empty.
     *
     * @throws SAXException If this operation is supported but failed for some reason.
     * @throws ParserConfigurationException If a {@link DocumentBuilder} cannot be created which satisfies the
     *             configuration requested.
     * @throws IOException If any IO errors occur.
     */
    public static Configuration create (final File schemaLocation, final File configFile, final String pTargetAddress) throws SAXException , ParserConfigurationException , IOException {
        return create(new FileInputStream(schemaLocation), new FileInputStream(configFile), pTargetAddress);
    }

    /**
     * Reads the given configuration file in memory and creates a DOM representation.
     *
     * @throws SAXException If this operation is supported but failed for some reason.
     * @throws ParserConfigurationException If a {@link DocumentBuilder} cannot be created which satisfies the
     *             configuration requested.
     * @throws IOException If any IO errors occur.
     */
    public static Configuration create (final File schemaLocation, final File configFile) throws SAXException , ParserConfigurationException , IOException {
        return create(new FileInputStream(schemaLocation), new FileInputStream(configFile), null);
    }

    /**
     * Reads the given configuration file in memory and creates a DOM representation.
     *
     * @param  pTargetAddress   Get from configuration XML if this parameter is null or empty.
     *
     * @throws SAXException If this operation is supported but failed for some reason.
     * @throws ParserConfigurationException If a {@link DocumentBuilder} cannot be created which satisfies the
     *             configuration requested.
     * @throws IOException If any IO errors occur.
     */
    public static Configuration create (final InputStream schemaLocation, final InputStream configFile, String pTargetAddress) throws SAXException , ParserConfigurationException , IOException {
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        final Schema schema = schemaFactory.newSchema(new StreamSource(schemaLocation));

        // create a validator for the document
        final Validator validator = schema.newValidator();

        final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true); // never forget this
        final DocumentBuilder builder = domFactory.newDocumentBuilder();
        final Document doc = builder.parse(configFile);

        final DOMSource source = new DOMSource(doc);
        final DOMResult result = new DOMResult();

        validator.validate(source, result);
        Document root = (Document) result.getNode();

        // target address
        if (Strings.isNullOrEmpty (pTargetAddress)) {
            NodeList tagsAddress = root.getElementsByTagName(ELEMENT_ADDRESS);
            if (tagsAddress.getLength() > 0) {
                pTargetAddress = tagsAddress.item(0).getTextContent();
            }
        }

        // TargetName
        Configuration returnConfiguration = new Configuration(pTargetAddress);
        Element targetListNode = (Element) root.getElementsByTagName(ELEMENT_TARGET_LIST).item(0);
        NodeList targetList = targetListNode.getElementsByTagName(ELEMENT_TARGET);
        for (int curTargetNum = 0; curTargetNum < targetList.getLength(); curTargetNum++) {
            Target curTargetInfo = parseTargetElement((Element) targetList.item(curTargetNum));
            synchronized (returnConfiguration.targets) {
                returnConfiguration.targets.add(curTargetInfo);
            }

        }

        // else it is null

        // port
        NodeList portTags = root.getElementsByTagName(ELEMENT_PORT);
        if (portTags.getLength() > 0) {
            returnConfiguration.port = Integer.parseInt(portTags.item(0).getTextContent());
        } else {
            returnConfiguration.port = 3260;
        }

        // external port
        NodeList externalPortTags = root.getElementsByTagName(ELEMENT_EXTERNAL_PORT);
        if (externalPortTags.getLength() > 0) {
            returnConfiguration.externalPort = Integer.parseInt(externalPortTags.item(0).getTextContent());
        } else {
            returnConfiguration.externalPort = returnConfiguration.port;
        }


        // external address
        NodeList externalAddressTAgs = root.getElementsByTagName(ELEMENT_EXTERNAL_ADDRESS);
        if (externalAddressTAgs.getLength() > 0) {
            returnConfiguration.externalTargetAddress = externalAddressTAgs.item(0).getTextContent();
        } else {
            returnConfiguration.externalTargetAddress = pTargetAddress;
        }


        // support sloppy text parameter negotiation (i.e. the jSCSI Initiator)?
        final Node allowSloppyNegotiationNode = root.getElementsByTagName(ELEMENT_ALLOWSLOPPYNEGOTIATION).item(0);
        if (allowSloppyNegotiationNode == null)
            returnConfiguration.allowSloppyNegotiation = false;
        else
            returnConfiguration.allowSloppyNegotiation = Boolean.parseBoolean(allowSloppyNegotiationNode.getTextContent());


        // vendor info
        NodeList tagsVendorID = root.getElementsByTagName(ELEMENT_VENDOR_ID);
        if (tagsVendorID.getLength() > 0) {
            returnConfiguration.idVendor = tagsVendorID.item(0).getTextContent();
        }
        NodeList tagsProductID = root.getElementsByTagName(ELEMENT_PRODUCT_ID);
        if (tagsProductID.getLength() > 0) {
            returnConfiguration.idProduct = tagsProductID.item(0).getTextContent();
        }
        NodeList tagsProductRevision = root.getElementsByTagName(ELEMENT_PRODUCT_REVISION);
        if (tagsProductRevision.getLength() > 0) {
            returnConfiguration.revProduct = tagsProductRevision.item(0).getTextContent();
        }

        return returnConfiguration;

    }

    protected static Target parseTargetElement (Element targetElement) throws IOException {
        // TargetName
        Node nextNode = chopNotEquals (targetElement.getFirstChild(), TextKeyword.TARGET_NAME);
        if (nextNode == null) {
            throw new IOException (TextKeyword.TARGET_NAME + " tag not found.");
        }
        String targetName = nextNode.getTextContent();

        // TargetAlias (optional)
        nextNode = chopWhiteSpaces(nextNode.getNextSibling());
        if (nextNode == null) {
            throw new IOException (TextKeyword.TARGET_NAME + " tag has no sibling.");
        }
        String targetAlias = "";
        if (nextNode.getLocalName().equals(TextKeyword.TARGET_ALIAS)) {
            targetAlias = nextNode.getTextContent();
            nextNode = chopWhiteSpaces(nextNode.getNextSibling());
        }

        // Finding out the concrete storage
        Class<? extends IStorageModule> kind = null;
        switch (nextNode.getLocalName()) {
            case ELEMENT_SYNCFILESTORAGE :
                kind = SynchronizedRandomAccessStorageModule.class;
                break;
            case ELEMENT_ASYNCFILESTORAGE :
                kind = RandomAccessStorageModule.class;
                break;
            case ELEMENT_JCLOUDSSTORAGE :
                kind = JCloudsStorageModule.class;
                break;
            case ELEMENT_EXTENDEDSTORAGE :
                Node   attrClass = nextNode.getAttributes().getNamedItem(ATTRIBUTE_CLASS);
                String nameClass = attrClass.getTextContent();
                try {
                    @SuppressWarnings ("unchecked")
                    Class<? extends IStorageModule> unc = (Class<? extends IStorageModule>) Class.forName (nameClass);
                    kind = unc;
                } catch (ClassNotFoundException e) {
                    throw new IOException ("Storage class not found: " + nameClass + " (" + e.getMessage() + ")");
                }
                break;
            default:
                throw new IOException ("Unknown storage: " + nextNode.getLocalName());
        }

        // Getting storage path
        nextNode = chopNotEquals (nextNode.getFirstChild(), ELEMENT_PATH);
        if (nextNode == null) {
            throw new IOException (ELEMENT_PATH + " tag not found.");
        }
        String storageFilePath = nextNode.getTextContent();

        // CreateNode with size
        nextNode = chopWhiteSpaces(nextNode.getNextSibling());
        long storageLength;
        boolean create = true;
        if (nextNode.getLocalName().equals(ELEMENT_CREATE)) {
            Node sizeAttribute = nextNode.getAttributes().getNamedItem(ATTRIBUTE_SIZE);
            storageLength = Math.round(Double.valueOf(sizeAttribute.getTextContent()) * Math.pow(1024, 3));
        } else {
            storageLength = new File(storageFilePath).length();
            create = false;
            // assert nextNode.getLocalName().equals(ELEMENT_DONTCREATE);
        }
        final IStorageModule module = RandomAccessStorageModule.open(new File(storageFilePath), storageLength, create, kind);

        return new Target(targetName, targetAlias, module);
    }

    protected static Node chopWhiteSpaces (final Node node) {
        Node toIterate = node;
        while (toIterate != null) {
            if (toIterate instanceof Element) break;
            toIterate = toIterate.getNextSibling();
        }
        return toIterate;
    }

    protected static Node chopNotEquals (final Node node, final String tag) {
        Node next = node;
        while (next != null) {
            if (Strings.nullToEmpty (next.getLocalName()).equals(tag)) break;
            next = next.getNextSibling();
        }
        return next;
    }

    public String getExternalTargetAddress() {
        return externalTargetAddress;
    }

    public int getExternalPort() {
        return externalPort;
    }

    public String getVendorID() {
        return idVendor;
    }

    public String getProductID() {
        return idProduct;
    }

    public String getProductRevisionLevel() {
        return revProduct;
    }

}
