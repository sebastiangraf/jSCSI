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
package org.jscsi.initiator.opentarget;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jscsi.initiator.Configuration;
import org.jscsi.initiator.Initiator;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public final class InitiatorTest {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    /** previous Target_Name - "disk6" */
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

    /** Buffer, which is used for storing a read operation. */
    private static ByteBuffer readBuffer;

    /** Buffer, which is used for storing a write operation. */
    private static ByteBuffer writeBuffer;

    /** The random number generator to fill the buffer to send. */
    private static Random randomGenerator;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    @BeforeClass
    public static final void initialize() throws Exception {

        initiator = new Initiator(Configuration.create());

        readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);

        randomGenerator = new Random(System.currentTimeMillis());

        randomGenerator.nextBytes(writeBuffer.array());

        initiator.createSession(TARGET_DRIVE_NAME);
        System.out.println("created Session succesfull");
    }

    @AfterClass
    public static final void close() throws Exception {

        initiator.closeSession(TARGET_DRIVE_NAME);
    }

    // --------------------------------------------------------------------------

    /**
     * Tests the correct behavior of the initiator for sending empty bytes and
     * then test write message.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test(enabled=false)
    public final void testClearing() throws Exception {

        initiator.write(TARGET_DRIVE_NAME, writeBuffer, LOGICAL_BLOCK_ADDRESS, writeBuffer.remaining());
        writeBuffer.flip();

        initiator.read(TARGET_DRIVE_NAME, readBuffer, LOGICAL_BLOCK_ADDRESS, readBuffer.remaining());
        readBuffer.flip();

        AssertJUnit.assertTrue(writeBuffer.equals(readBuffer));
    }

    /**
     * Tests the correct behavior of the initiator for sending a write and then
     * a read message.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test(enabled=false)
    public final void testWriteRead() throws Exception {

        initiator.write(TARGET_DRIVE_NAME, writeBuffer, LOGICAL_BLOCK_ADDRESS, writeBuffer.remaining());
        initiator.read(TARGET_DRIVE_NAME, readBuffer, LOGICAL_BLOCK_ADDRESS, readBuffer.remaining());

        writeBuffer.flip();
        readBuffer.flip();

        AssertJUnit.assertTrue(writeBuffer.equals(readBuffer));
    }

    /**
     * Tests the correct behavior of the initiator for sending multiple read
     * messages.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test(enabled=false)
    public final void testMultipleReads() throws Exception {

        initiator.read(TARGET_DRIVE_NAME, readBuffer, LOGICAL_BLOCK_ADDRESS, readBuffer.remaining());

        final ByteBuffer readBuffer2 = ByteBuffer.allocate(readBuffer.capacity());
        initiator.read(TARGET_DRIVE_NAME, readBuffer2, LOGICAL_BLOCK_ADDRESS, readBuffer2.remaining());

        readBuffer.flip();
        readBuffer2.flip();

        AssertJUnit.assertTrue(readBuffer.equals(readBuffer2));
    }

    /**
     * Tests the correct behavior of the initiator for reading data, which is
     * smaller than the destination buffer size.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test(enabled=false)
    public final void testPartialRead() throws Exception {

        // FIXME: implement test case
        initiator.read(TARGET_DRIVE_NAME, readBuffer, LOGICAL_BLOCK_ADDRESS, Math.min(1, readBuffer
            .remaining() / 10));
    }

    /**
     * Tests the correct behavior of the initiator for sending data, which is
     * smaller than the source buffer size.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test(enabled=false)
    public final void testPartialWrite() throws Exception {

        // FIXME: Implement test case
        // initiator.write(this, TARGET_DRIVE_NAME, writeBuffer,
        // LOGICAL_BLOCK_ADDRESS, writeBuffer.remaining() / 10);

    }

    /**
     * Tests the correct behavior of the initiator for sending multiple write
     * messages.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test(enabled=false)
    public final void testMultipleWrites() throws Exception {

        // FIXME: Useful test case?
        initiator.write(TARGET_DRIVE_NAME, writeBuffer, LOGICAL_BLOCK_ADDRESS, writeBuffer.remaining());

        writeBuffer.clear();
        initiator.write(TARGET_DRIVE_NAME, writeBuffer, LOGICAL_BLOCK_ADDRESS, writeBuffer.remaining());
    }

    @Test(enabled=false)
    public final void testMultiThreading() throws Exception {

        final ExecutorService service = Executors.newCachedThreadPool();
        final Future<Void> get1 = service.submit(new MultiThreadingTest(initiator));
        final Future<Void> get2 = service.submit(new MultiThreadingTest(initiator));
        service.shutdown();

        get1.get();
        get2.get();

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    private final class MultiThreadingTest implements Callable<Void> {

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        private static final int NUMBER_OF_ITERATIONS = 1;

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        private final Initiator MTInitiator;

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        MultiThreadingTest(final Initiator initInitiator) {

            MTInitiator = initInitiator;
        }

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        /** {@inheritDoc} */
        public Void call() throws Exception {

            final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            Random randomGenerator = new Random();
            randomGenerator.nextBytes(buffer.array());

            for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
                MTInitiator.write(TARGET_DRIVE_NAME, buffer, randomGenerator.nextInt(), buffer.remaining());
                buffer.rewind();
            }
            return null;
        }

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
