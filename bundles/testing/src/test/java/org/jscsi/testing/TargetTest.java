package org.jscsi.testing;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import org.jscsi.exception.ConfigurationException;
import org.jscsi.exception.InternetSCSIException;
import org.jscsi.initiator.Initiator;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.ProtocolDataUnitFactory;
import org.jscsi.target.Configuration;
import org.jscsi.target.TargetServer;
import org.jscsi.target.connection.TargetConnection;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.connection.stage.TargetStage;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 * This class tests the functionalities of {@link TargetServer}
 * 
 * @author Andreas Rain
 * 
 */
public class TargetTest {

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
    static ModeSenseStage modeSenseStage;

    /**
     * This stage will be executed using a stub so the code in there is covered.
     */
    static PingStage pingStage;

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
        public static void start() throws Exception {
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
            // not needed any more
            threadPool.shutdown();
        }

    }

    @BeforeClass
    public void beforeClass() throws Exception {
        CallableStart.start();

        configuration =
            org.jscsi.initiator.Configuration.create(new File(CONFIG_DIR, "jscsi.xsd"), new File(CONFIG_DIR,
                "jscsi.xml"));

        initiator = new Initiator(configuration);

        readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);

        randomGenerator = new Random(System.currentTimeMillis());

        randomGenerator.nextBytes(writeBuffer.array());

        initiator.createSession(TARGET_DRIVE_NAME);
    }

    @Test
    public void testConfiguration() {
        assertEquals(targetServer.getConfig().getTargets().size(), 2);
    }

    @Test
    public void testTargetProperties() {
        assertEquals(targetServer.getTargetNames().length, targetServer.getConfig().getTargets().size());
        assertTrue(targetServer.getTarget("iqn.2010-04.local-test:disk-1") != null);
        assertTrue(targetServer.isValidTargetName("iqn.2010-04.local-test:disk-2"));

        assertEquals("iqn.2010-04.local-test:disk-1", targetServer.getTarget("iqn.2010-04.local-test:disk-1")
            .getTargetName());
        assertEquals("jSCSI Target", targetServer.getTarget("iqn.2010-04.local-test:disk-1").getTargetAlias());

        assertEquals(targetServer.getTarget("iqn.2010-04.local-test:disk-1").hashCode(),
            31 + "iqn.2010-04.local-test:disk-1".hashCode());
        assertFalse(targetServer.getTarget("iqn.2010-04.local-test:disk-1").equals(
            targetServer.getTarget("iqn.2010-04.local-test:disk-2")));
    }

    @Test
    public void testConnectionEstablishment() throws Exception {
        initiator.write(TARGET_DRIVE_NAME, writeBuffer, LOGICAL_BLOCK_ADDRESS, writeBuffer.remaining());
        initiator.read(TARGET_DRIVE_NAME, readBuffer, LOGICAL_BLOCK_ADDRESS, writeBuffer.remaining());

    }

    /**
     * This test executes the mode sense stage covering the stage execution
     * using a the target connection and a stub'ish pdu.
     */
    @Test(enabled=false)
    public void testModeSenseStage() throws Exception {
        TargetConnection connection = targetServer.getConnection();
        modeSenseStage = new ModeSenseStage(new TargetFullFeaturePhase(connection));

        final ProtocolDataUnit pdu =
            new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None");

        modeSenseStage.execute(pdu);
    }

    /**
     * This test executes the ping stage covering the stage execution
     * using a the target connection and a stub'ish pdu.
     */
    @Test(enabled=false)
    public void testPingStage() throws Exception {
        TargetConnection connection = targetServer.getConnection();
        pingStage = new PingStage(new TargetFullFeaturePhase(connection));

        final ProtocolDataUnit pdu =
            new ProtocolDataUnitFactory().create(false, true, OperationCode.NOP_OUT, "None", "None");

        pingStage.execute(pdu);
    }

    @Test(dataProvider = "instantiateStages")
    public void testStages(Class<TargetStage> pTargetStageClass, TargetStage[] pStages,
        Class<ProtocolDataUnit> pDataUnitClass, ProtocolDataUnit[] pDataUnits) throws DigestException,
        IOException, InterruptedException, InternetSCSIException, SettingsException {
        assertEquals(pStages.length, pDataUnits.length);
        for (int i = 0; i < pStages.length; i++) {
            pStages[i].execute(pDataUnits[i]);
        }

    }

    @DataProvider(name = "instantiateStages")
    public Object[][] provideStages() {
        final TargetFullFeaturePhase phase = new TargetFullFeaturePhase(targetServer.getConnection());
        Object[][] returnVal =
            {
                {
                    TargetStage.class,
                    new TargetStage[] {
                        new TestUnitReadyStage(phase), new SendDiagnosticStage(phase),
                        new ReportLunsStage(phase), new InquiryStage(phase), new RequestSenseStage(phase),
                        new TextNegotiationStage(phase), new UnsupportedOpCodeStage(phase),
                        new FormatUnitStage(phase)
                    },
                    ProtocolDataUnit.class,
                    new ProtocolDataUnit[] {
                        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None",
                            "None"),
                        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None",
                            "None"),
                        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None",
                            "None"),
                        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None",
                            "None"),
                        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None",
                            "None"),
                        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_TM_REQUEST,
                            "None", "None"),
                        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_TM_REQUEST,
                            "None", "None"),
                        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None",
                            "None")
                    }
                }
            };
        return returnVal;
    }

    @AfterClass
    public void afterClass() throws Exception {
        initiator.closeSession(TARGET_DRIVE_NAME);
        targetServer.getConnection().close();
    }

    private static boolean isWindows() {

        String os = System.getProperty("os.name").toLowerCase();
        // windows
        return (os.indexOf("win") >= 0);

    }

}
