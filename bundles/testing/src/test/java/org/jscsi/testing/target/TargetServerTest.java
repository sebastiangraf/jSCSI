package org.jscsi.testing.target;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.DigestException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import org.jscsi.exception.ConfigurationException;
import org.jscsi.exception.InternetSCSIException;
import org.jscsi.exception.NoSuchSessionException;
import org.jscsi.exception.TaskExecutionException;
import org.jscsi.initiator.Initiator;
import org.jscsi.initiator.LinkFactory;
import org.jscsi.initiator.connection.Session;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.ProtocolDataUnitFactory;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.target.Configuration;
import org.jscsi.target.TargetServer;
import org.jscsi.target.connection.TargetConnection;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.connection.stage.fullfeature.FormatUnitStage;
import org.jscsi.target.connection.stage.fullfeature.InquiryStage;
import org.jscsi.target.connection.stage.fullfeature.ModeSenseStage;
import org.jscsi.target.connection.stage.fullfeature.PingStage;
import org.jscsi.target.connection.stage.fullfeature.ReportLunsStage;
import org.jscsi.target.connection.stage.fullfeature.RequestSenseStage;
import org.jscsi.target.connection.stage.fullfeature.SendDiagnosticStage;
import org.jscsi.target.connection.stage.fullfeature.TestUnitReadyStage;
import org.jscsi.target.connection.stage.fullfeature.TextNegotiationStage;
import org.jscsi.target.connection.stage.fullfeature.UnsupportedOpCodeStage;
import org.jscsi.target.settings.SettingsException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.xml.sax.SAXException;

/**
 * This class tests the functionalities of {@link TargetServer}
 * 
 * @author Andreas Rain
 * 
 */
public class TargetServerTest {

    /**
     * The target server instance to be used
     */
    static TargetServer targetServer;

    /** The targets configuration file */
    private static File targetConfigurationFile;

    /** The initiator object. */
    private static Initiator initiator;

    /** Initiator configuration */
    private static org.jscsi.initiator.Configuration configuration;

    /** Buffer, which is used for storing a read operation. */
    private static ByteBuffer readBuffer;

    /** Buffer, which is used for storing a write operation. */
    private static ByteBuffer writeBuffer;

    /** The random number generator to fill the buffer to send. */
    private static Random randomGenerator;

    /** Name of the device name on the iSCSI Target. */
    private static final String TARGET_DRIVE_NAME = "testing-xen2-disk1";

    /** The size (in bytes) of the buffer to use for reads and writes. */
    private static final int BUFFER_SIZE = 46 * 1024;

    /** The logical block address of the start block to begin an operation. */
    private static final int LOGICAL_BLOCK_ADDRESS = 20;

    /**
     * This stage will be executed using a stub so the code in there is covered.
     */
    static InquiryStage inquiryStage;

    /**
     * This stage will be executed using a stub so the code in there is covered.
     */
    static FormatUnitStage formatUnitStage;

    /**
     * This stage will be executed using a stub so the code in there is covered.
     */
    static UnsupportedOpCodeStage unsupportedOpCodeStage;

    /**
     * This stage will be executed using a stub so the code in there is covered.
     */
    static TextNegotiationStage textNegotiationStage;

    /**
     * This stage will be executed using a stub so the code in there is covered.
     */
    static ModeSenseStage modeSenseStage;

    /**
     * This stage will be executed using a stub so the code in there is covered.
     */
    static RequestSenseStage requestSenseStage;

    /**
     * This stage will be executed using a stub so the code in there is covered.
     */
    static PingStage pingStage;

    /**
     * This stage will be executed using a stub so the code in there is covered.
     */
    static ReportLunsStage reportLunsStage;

    /**
     * This stage will be executed using a stub so the code in there is covered.
     */
    static SendDiagnosticStage sendDiagnosticStage;

    /**
     * This stage will be executed using a stub so the code in there is covered.
     */
    static TestUnitReadyStage testUnitReadyStage;

    /**
     * The relative path (to the project) of the main directory of all
     * configuration files.
     */
    private static final File CONFIG_DIR = new File(new StringBuilder("src").append(File.separator).append(
        "test").append(File.separator).append("resources").append(File.separator).toString());

    /**
     * A helping class to start the target in a threadpool so the tests can run
     * next to it.
     * 
     */
    public static class CallableStart {
        static ExecutorService threadPool;

        /**
         * This method starts a targetserver in a threadpool.
         * 
         * @throws SAXException
         * @throws ParserConfigurationException
         * @throws IOException
         * @throws ConfigurationException
         */
        public static void start() throws SAXException, ParserConfigurationException, IOException,
            ConfigurationException {
            if (isWindows()) {
                targetConfigurationFile = new File(CONFIG_DIR, "jscsi-target-windows.xml");
            } else {
                targetConfigurationFile = new File(CONFIG_DIR, "jscsi-target-linux.xml");
            }

            Configuration targetConfiguration =
                Configuration.create(new File(CONFIG_DIR, "jscsi-target.xsd"), targetConfigurationFile);

            targetServer = new TargetServer(targetConfiguration);

            // Getting an Executor
            threadPool = Executors.newSingleThreadExecutor();
            // Starting the target
            threadPool.submit(targetServer);
        }

        /**
         * After the tests are finished shutdown the threadpool.
         */
        public static void stop() {
            threadPool.shutdown();

            while (!threadPool.isShutdown())
                ;
        }
    }

    @BeforeClass
    public void beforeClass() {
        try {
            CallableStart.start();

            while (!targetServer.isReady()) {

            }

            configuration =
                org.jscsi.initiator.Configuration.create(new File(CONFIG_DIR, "jscsi.xsd"), new File(
                    CONFIG_DIR, "jscsi.xml"));

            initiator = new Initiator(configuration);

            readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);

            randomGenerator = new Random(System.currentTimeMillis());

            randomGenerator.nextBytes(writeBuffer.array());

            try {
                initiator.createSession(TARGET_DRIVE_NAME);
            } catch (NoSuchSessionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } catch (SAXException | ParserConfigurationException | IOException | ConfigurationException e) {
            Assert.fail("The target could not be started due to an exception.");
            e.printStackTrace();
        }
    }

    @Test
    public void testConfiguration() {
        Assert.assertTrue(targetServer.getConfig().getTargets().size() == 2);
    }

    @Test
    public void testTargetProperties() {
        Assert.assertTrue(targetServer.getTargetNames().length == targetServer.getConfig().getTargets()
            .size());
        Assert.assertTrue(targetServer.getTarget("iqn.2010-04.local-test:disk-1") != null);
        Assert.assertTrue(targetServer.isValidTargetName("iqn.2010-04.local-test:disk-2"));

        Assert.assertTrue(targetServer.getTarget("iqn.2010-04.local-test:disk-1").getTargetName().equals(
            "iqn.2010-04.local-test:disk-1"));
        Assert.assertTrue(targetServer.getTarget("iqn.2010-04.local-test:disk-1").getTargetAlias().equals(
            "jSCSI Target"));
        Assert
            .assertTrue(targetServer.getTarget("iqn.2010-04.local-test:disk-1").hashCode() == 31 + "iqn.2010-04.local-test:disk-1"
                .hashCode());
        Assert.assertFalse(targetServer.getTarget("iqn.2010-04.local-test:disk-1").equals(
            targetServer.getTarget("iqn.2010-04.local-test:disk-2")));
    }

    @Test
    public void testConnectionEstablishment() {
        try {
            initiator.write(TARGET_DRIVE_NAME, writeBuffer, LOGICAL_BLOCK_ADDRESS, writeBuffer.remaining());

            initiator.read(TARGET_DRIVE_NAME, readBuffer, LOGICAL_BLOCK_ADDRESS, writeBuffer.remaining());

        } catch (NoSuchSessionException e) {
            Assert.fail("The session could not be created due to an exception.");
            e.printStackTrace();
        } catch (TaskExecutionException e) {
            Assert.fail("Could not write or read on the established connection.");
            e.printStackTrace();
        }
    }

    /**
     * This test executes the inquiry stage covering the stage execution
     * using a the target connection and a stub'ish pdu.
     */
    @Test
    public void testInquiry() {
        TargetConnection connection = targetServer.getConnection();
        inquiryStage = new InquiryStage(new TargetFullFeaturePhase(connection));

        final ProtocolDataUnit pdu =
            new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None");

        try {
            inquiryStage.execute(pdu);
        } catch (DigestException | IOException | InterruptedException | InternetSCSIException
        | SettingsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * This test executes the format unit stage covering the stage execution
     * using a the target connection and a stub'ish pdu.
     */
    @Test
    public void testFormatUnit() {
        TargetConnection connection = targetServer.getConnection();
        formatUnitStage = new FormatUnitStage(new TargetFullFeaturePhase(connection));

        final ProtocolDataUnit pdu =
            new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None");

        try {
            formatUnitStage.execute(pdu);
        } catch (DigestException | IOException | InterruptedException | InternetSCSIException
        | SettingsException e) {
            e.printStackTrace();
        }
    }

    /**
     * This test executes the unsupported opcode stage covering the stage execution
     * using a the target connection and a stub'ish pdu.
     */
    @Test
    public void testUnsupportedOpCode() {
        TargetConnection connection = targetServer.getConnection();
        unsupportedOpCodeStage = new UnsupportedOpCodeStage(new TargetFullFeaturePhase(connection));

        final ProtocolDataUnit pdu =
            new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_TM_REQUEST, "None", "None");

        try {
            unsupportedOpCodeStage.execute(pdu);
        } catch (DigestException | IOException | InterruptedException | InternetSCSIException
        | SettingsException e) {
            e.printStackTrace();
        }
    }

    /**
     * This test executes the text negotiation stage covering the stage execution
     * using a the target connection and a stub'ish pdu.
     */
    @Test
    public void testTextNegotiationStage() {
        TargetConnection connection = targetServer.getConnection();
        textNegotiationStage = new TextNegotiationStage(new TargetFullFeaturePhase(connection));

        final ProtocolDataUnit pdu =
            new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_TM_REQUEST, "None", "None");

        pdu.setDataSegment(ByteBuffer.wrap("hello world".getBytes()));

        try {
            textNegotiationStage.execute(pdu);
        } catch (DigestException | IOException | InterruptedException | InternetSCSIException
        | SettingsException e) {
            e.printStackTrace();
        }
    }

    /**
     * This test executes the mode sense stage covering the stage execution
     * using a the target connection and a stub'ish pdu.
     */
    @Test
    public void testModeSenseStage() {
        TargetConnection connection = targetServer.getConnection();
        modeSenseStage = new ModeSenseStage(new TargetFullFeaturePhase(connection));

        final ProtocolDataUnit pdu =
            new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None");

        try {
            modeSenseStage.execute(pdu);
        } catch (DigestException | IOException | InterruptedException | InternetSCSIException
        | SettingsException e) {
            e.printStackTrace();
        }
    }

    /**
     * This test executes the request sense stage covering the stage execution
     * using a the target connection and a stub'ish pdu.
     */
    @Test
    public void testRequestSenseStage() {
        TargetConnection connection = targetServer.getConnection();
        requestSenseStage = new RequestSenseStage(new TargetFullFeaturePhase(connection));

        final ProtocolDataUnit pdu =
            new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None");

        try {
            requestSenseStage.execute(pdu);
        } catch (DigestException | IOException | InterruptedException | InternetSCSIException
        | SettingsException e) {
            e.printStackTrace();
        }
    }

    /**
     * This test executes the ping stage covering the stage execution
     * using a the target connection and a stub'ish pdu.
     */
    @Test
    public void testPingStage() {
        TargetConnection connection = targetServer.getConnection();
        pingStage = new PingStage(new TargetFullFeaturePhase(connection));

        final ProtocolDataUnit pdu =
            new ProtocolDataUnitFactory().create(false, true, OperationCode.NOP_OUT, "None", "None");

        try {
            pingStage.execute(pdu);
        } catch (DigestException | IOException | InterruptedException | InternetSCSIException
        | SettingsException e) {
            e.printStackTrace();
        }
    }

    /**
     * This test executes the report luns stage covering the stage execution
     * using a the target connection and a stub'ish pdu.
     */
    @Test
    public void testReportLunsStage() {
        TargetConnection connection = targetServer.getConnection();
        reportLunsStage = new ReportLunsStage(new TargetFullFeaturePhase(connection));

        final ProtocolDataUnit pdu =
            new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None");

        try {
            reportLunsStage.execute(pdu);
        } catch (DigestException | IOException | InterruptedException | InternetSCSIException
        | SettingsException e) {
            e.printStackTrace();
        }
    }

    /**
     * This test executes the send diagnostic stage covering the stage execution
     * using a the target connection and a stub'ish pdu.
     */
    @Test
    public void testSendDiagnosticStage() {
        TargetConnection connection = targetServer.getConnection();
        sendDiagnosticStage = new SendDiagnosticStage(new TargetFullFeaturePhase(connection));

        final ProtocolDataUnit pdu =
            new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None");

        try {
            sendDiagnosticStage.execute(pdu);
        } catch (DigestException | IOException | InterruptedException | InternetSCSIException
        | SettingsException e) {
            e.printStackTrace();
        }
    }

    /**
     * This test executes the test unit ready stage covering the stage execution
     * using a the target connection and a stub'ish pdu.
     */
    @Test
    public void testTestUnitReadyStage() {
        TargetConnection connection = targetServer.getConnection();
        testUnitReadyStage = new TestUnitReadyStage(new TargetFullFeaturePhase(connection));

        final ProtocolDataUnit pdu =
            new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None");

        try {
            testUnitReadyStage.execute(pdu);
        } catch (DigestException | IOException | InterruptedException | InternetSCSIException
        | SettingsException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public void afterClass() {
        try {
            initiator.closeSession(TARGET_DRIVE_NAME);
        } catch (NoSuchSessionException e) {
            e.printStackTrace();
        } catch (TaskExecutionException e) {
            e.printStackTrace();
        }

        targetServer.getConnection().close();
        CallableStart.stop();
    }

    private static boolean isWindows() {

        String os = System.getProperty("os.name").toLowerCase();
        // windows
        return (os.indexOf("win") >= 0);

    }

}
