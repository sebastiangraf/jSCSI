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
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Instances of {@link TargetConfiguration} provides access target-wide
 * parameters, variables that are the same across all sessions and connections
 * that do not change after initialization and which play a role during text
 * parameter negotiation. Some of these parameters are provided or can be
 * overridden by the content of an XML file - <code>jscsi-target.xml</code>.
 * 
 * @author Andreas Ergenzinger, University of Konstanz
 */
public class TargetConfiguration {

    protected List<TargetInfo> targets;

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
    protected int port;

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
    protected boolean allowSloppyNegotiation;// TODO fix in jSCSI Initiator and
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

    public int getInMaxRecvTextPduSequenceLength() {
        return maxRecvTextPduSequenceLength;
    }

    public int getOutMaxRecvDataSegmentLength() {
        return outMaxRecvDataSegmentLength;
    }

    public String getTargetAddress() {
        return targetAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setAllowSloppyNegotiation(boolean allowSloppyNegotiation) {
        this.allowSloppyNegotiation = allowSloppyNegotiation;
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

    public TargetConfiguration() throws IOException {
        port = 3260;
        targetAddress = InetAddress.getLocalHost().getHostAddress();
        targets = new ArrayList<TargetInfo>();
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
    protected Document parse(final File schemaLocation, final File configFile) throws SAXException,
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

}
