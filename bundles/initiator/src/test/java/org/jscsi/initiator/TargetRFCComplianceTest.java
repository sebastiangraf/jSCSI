package org.jscsi.initiator;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.parsers.ParserConfigurationException;

import org.jscsi.exception.ConfigurationException;
import org.jscsi.initiator.connection.Session;
import org.jscsi.target.TargetServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * The TargetRFCComplianceTest should test if the targets responses
 * are correct and compliant to the RFC.
 * 
 * @author Andreas Rain
 * 
 */
public class TargetRFCComplianceTest {

    /** Name of the device name on the iSCSI Target. */
    private static final String TARGET_DRIVE_NAME = "testing-xen2-disk1";

    /** The size (in bytes) of the buffer to use for reads and writes. */
    private static final int BUFFER_SIZE = 46 * 1024;

    /** The logical block address of the start block to begin an operation. */
    private static final int LOGICAL_BLOCK_ADDRESS = 20;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The initiator object. */
    private static Initiator initiator;

    /** The session object **/
    private static Session session;

    /** The linkfactory object **/
    private static LinkFactory factory;

    /** Initiator configuration */
    private static Configuration configuration;

    /** Buffer, which is used for storing a read operation. */
    private static ByteBuffer readBuffer;

    /** Buffer, which is used for storing a write operation. */
    private static ByteBuffer writeBuffer;

    /** The random number generator to fill the buffer to send. */
    private static Random randomGenerator;

    /** An instance of the target will be dynamically created */
    private static TargetServer target;

    /** The targets configuration file */
    private static File targetConfigurationFile;

    /** The targets configuration file */
    private static File targetConfigurationSchemaFile;

    /**
     * The relative path (to the project) of the main directory of all
     * configuration files.
     */
    private static final File CONFIG_DIR = new File(new StringBuilder("src").append(File.separator).append(
        "test").append(File.separator).append("resources").append(File.separator).toString());

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * A helping class to start the target in a threadpool so the tests can run next to it.
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

            org.jscsi.target.Configuration targetConfiguration =
                new org.jscsi.target.Configuration().create(new File(CONFIG_DIR, "jscsi-target.xsd"),
                    targetConfigurationFile);

            target = new TargetServer(targetConfiguration);

            // Getting an Executor
            threadPool = Executors.newSingleThreadExecutor();
            // Starting the target
            threadPool.submit(target);
        }

        /**
         * After the tests are finished shutdown the threadpool.
         */
        public static void stop() {
            threadPool.shutdown();
        }
    }

    @BeforeClass
    public static final void initialize() throws Exception {
        CallableStart.start();

        configuration =
            Configuration.create(new File(CONFIG_DIR, "jscsi.xsd"), new File(CONFIG_DIR, "jscsi.xml"));

        initiator = new Initiator(configuration);

        readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);

        randomGenerator = new Random(System.currentTimeMillis());

        randomGenerator.nextBytes(writeBuffer.array());

        // Not working
        factory = new LinkFactory(initiator);
        session =
            factory.getSession(configuration, TARGET_DRIVE_NAME, configuration
                .getTargetAddress(TARGET_DRIVE_NAME));

        // Working:
        // initiator.createSession(TARGET_DRIVE_NAME);
        System.out.println("created Session succesfull");
    }

    @AfterClass
    public static final void close() throws Exception {
        // initiator.closeSession(TARGET_DRIVE_NAME);
        session.logout();
        session.close();
        CallableStart.stop();
    }

    /**
     * This test should check, whether or not a correct response is given for a
     * specific command.
     * 
     * @throws Exception
     */
    @Test
    public final void testPDUResponses() throws Exception {

        // Working Code:

        // initiator.write(TARGET_DRIVE_NAME, writeBuffer, LOGICAL_BLOCK_ADDRESS, writeBuffer.remaining());
        //
        // assertTrue(!writeBuffer.equals(readBuffer));
        //
        // initiator.read(TARGET_DRIVE_NAME, readBuffer, LOGICAL_BLOCK_ADDRESS, readBuffer.remaining());
        //
        // assertArrayEquals(writeBuffer.array(), readBuffer.array());

        // Not working:
        Future<Void> returnVal1 = session.write(writeBuffer, LOGICAL_BLOCK_ADDRESS, writeBuffer.remaining());
        returnVal1.get();

        assertTrue(!writeBuffer.equals(readBuffer));

        Future<Void> returnVal2 = session.read(readBuffer, LOGICAL_BLOCK_ADDRESS, readBuffer.remaining());
        returnVal2.get();

        assertTrue(writeBuffer.equals(readBuffer));

        writeBuffer.flip();
        readBuffer.flip();

        assertTrue(writeBuffer.equals(readBuffer));

    }

    private static boolean isWindows() {

        String os = System.getProperty("os.name").toLowerCase();
        // windows
        return (os.indexOf("win") >= 0);

    }

}
