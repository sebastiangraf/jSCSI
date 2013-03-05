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
package org.jscsi.testing;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.parsers.ParserConfigurationException;

import org.jscsi.exception.ConfigurationException;
import org.jscsi.exception.TaskExecutionException;
import org.jscsi.initiator.Configuration;
import org.jscsi.initiator.Initiator;
import org.jscsi.initiator.LinkFactory;
import org.jscsi.initiator.connection.Session;
import org.jscsi.target.TargetServer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

/**
 * This test suite independently test the initiatior against a dynamically
 * generated target.
 * 
 * Throughout the project a lot of tests have been set in aspects.
 * 
 * @author Andreas Rain
 * 
 */
public class BlackBoxTest {

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

    /**
     * The relative path (to the project) of the main directory of all
     * configuration files.
     */
    private static final File CONFIG_DIR = new File(new StringBuilder("src").append(File.separator).append(
        "test").append(File.separator).append("resources").append(File.separator).toString());

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

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

            org.jscsi.target.Configuration targetConfiguration =
                org.jscsi.target.Configuration.create(new File(CONFIG_DIR, "jscsi-target.xsd"),
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

        factory = new LinkFactory(initiator);
        session =
            factory.getSession(configuration, TARGET_DRIVE_NAME, configuration
                .getTargetAddress(TARGET_DRIVE_NAME));
    }

    @AfterClass
    public static final void close() throws Exception {
        // initiator.closeSession(TARGET_DRIVE_NAME);
        session.logout();
        session.close();
        CallableStart.stop();
    }

    /**
     * This test checks if a written bytebuffer still is the same when being
     * read from the target.
     * 
     * @throws Exception
     */
    @Test
    public final void testReadWriteEquality() throws Exception {

        Future<Void> returnVal1 = session.write(writeBuffer, 0, writeBuffer.remaining());
        returnVal1.get();

        assertTrue(!Arrays.equals(writeBuffer.array(), readBuffer.array()));

        Future<Void> returnVal2 = session.read(readBuffer, 0, readBuffer.remaining());
        returnVal2.get();

        assertTrue(Arrays.equals(writeBuffer.array(), readBuffer.array()));

        writeBuffer.flip();
        readBuffer.flip();

        assertTrue(Arrays.equals(writeBuffer.array(), readBuffer.array()));

    }

    /**
     * This test writes multiple buffers into the target and reads them
     * afterwards.
     * 
     * @throws TaskExecutionException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public final void testMultipleReadWrite() throws TaskExecutionException, InterruptedException,
        ExecutionException {
        /**
         * Creating multiple buffers.
         */
        ByteBuffer[] readBuffers = new ByteBuffer[10];
        ByteBuffer[] writeBuffers = new ByteBuffer[10];

        for (int i = 0; i < writeBuffers.length; i++) {
            readBuffers[i] = ByteBuffer.allocate(BUFFER_SIZE);
            writeBuffers[i] = ByteBuffer.allocate(BUFFER_SIZE);

            randomGenerator = new Random(System.currentTimeMillis());

            randomGenerator.nextBytes(writeBuffers[i].array());
        }

        for (int i = 0; i < writeBuffers.length; i++) {
            Future<Void> returnVal1;
            returnVal1 = session.write(writeBuffers[i], LOGICAL_BLOCK_ADDRESS, writeBuffers[i].remaining());
            returnVal1.get();

            Future<Void> returnVal2;
            returnVal2 = session.read(readBuffers[i], LOGICAL_BLOCK_ADDRESS, readBuffers[i].remaining());
            returnVal2.get();

        }

        for (int i = 0; i < writeBuffers.length; i++) {
            assertTrue(Arrays.equals(writeBuffers[i].array(), readBuffers[i].array()));
            // Need to write them into a separate buffer since flip doesn't seem
            // to work in a Array of ByteBuffers..
            ByteBuffer write = ByteBuffer.allocate(BUFFER_SIZE);
            ByteBuffer read = ByteBuffer.allocate(BUFFER_SIZE);

            write.put(writeBuffers[i]);
            read.put(readBuffers[i]);

            write.flip();
            read.flip();
            assertTrue(write.equals(read));
        }

    }

    private static boolean isWindows() {

        String os = System.getProperty("os.name").toLowerCase();
        // windows
        return (os.indexOf("win") >= 0);

    }

}
