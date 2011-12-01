/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.Random;

import org.jscsi.initiator.Configuration;
import org.jscsi.initiator.Initiator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

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
  // --------------------------------------------------------------------------

  /**
   * Tests the correct behavior of the initiator for sending a read capacity
   * message.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test
  @Ignore("Not working on real xen1, size differs")
  public final void testReadCapacity() throws Exception {

    assertEquals((long) 53372737, initiator.getCapacity(TARGET_DRIVE_NAME));
    assertEquals((long) 512, initiator.getBlockSize(TARGET_DRIVE_NAME));
  }

  /**
   * Tests the correct behavior of the initiator for sending empty bytes and
   * then test write message.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test
  public final void testClearing() throws Exception {

    initiator.write(this, TARGET_DRIVE_NAME, writeBuffer,
        LOGICAL_BLOCK_ADDRESS, writeBuffer.remaining());
    writeBuffer.flip();

    initiator.read(this, TARGET_DRIVE_NAME, readBuffer, LOGICAL_BLOCK_ADDRESS,
        readBuffer.remaining());
    readBuffer.flip();

    assertTrue(writeBuffer.equals(readBuffer));
  }

  /**
   * Tests the correct behavior of the initiator for sending a write and then a
   * read message.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test
  public final void testWriteRead() throws Exception {

    initiator.write(this, TARGET_DRIVE_NAME, writeBuffer,
        LOGICAL_BLOCK_ADDRESS, writeBuffer.remaining());
    initiator.read(this, TARGET_DRIVE_NAME, readBuffer, LOGICAL_BLOCK_ADDRESS,
        readBuffer.remaining());

    writeBuffer.flip();
    readBuffer.flip();

    assertTrue(writeBuffer.equals(readBuffer));
  }

  /**
   * Tests the correct behavior of the initiator for sending multiple read
   * messages.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test
  public final void testMultipleReads() throws Exception {

    initiator.read(this, TARGET_DRIVE_NAME, readBuffer, LOGICAL_BLOCK_ADDRESS,
        readBuffer.remaining());

    final ByteBuffer readBuffer2 = ByteBuffer.allocate(readBuffer.capacity());
    initiator.read(this, TARGET_DRIVE_NAME, readBuffer2, LOGICAL_BLOCK_ADDRESS,
        readBuffer2.remaining());

    readBuffer.flip();
    readBuffer2.flip();

    assertTrue(readBuffer.equals(readBuffer2));
  }

  /**
   * Tests the correct behavior of the initiator for reading data, which is
   * smaller than the destination buffer size.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test
  public final void testPartialRead() throws Exception {

    // FIXME: implement test case
    initiator.read(this, TARGET_DRIVE_NAME, readBuffer, LOGICAL_BLOCK_ADDRESS,
        Math.min(1, readBuffer.remaining() / 10));
  }

  /**
   * Tests the correct behavior of the initiator for sending data, which is
   * smaller than the source buffer size.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test
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
   *           These should never be thrown.
   */
  @Ignore
  public final void testMultipleWrites() throws Exception {

    // FIXME: Useful test case?
    initiator.write(this, TARGET_DRIVE_NAME, writeBuffer,
        LOGICAL_BLOCK_ADDRESS, writeBuffer.remaining());

    writeBuffer.clear();
    initiator.write(this, TARGET_DRIVE_NAME, writeBuffer,
        LOGICAL_BLOCK_ADDRESS, writeBuffer.remaining());
  }

  @Ignore
  public final void testMultiThreading() throws Exception {

    new Thread(new MultiThreadingTest(initiator)).start();
    new Thread(new MultiThreadingTest(initiator)).start();

  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  private final class MultiThreadingTest implements Runnable {

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    private static final int NUMBER_OF_ITERATIONS = 1;

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    private final Initiator MTInitiator;

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    MultiThreadingTest(final Initiator initInitiator) {

      MTInitiator = initInitiator;
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------

    /** {@inheritDoc} */
    public void run() {

      try {
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        Random randomGenerator = new Random();
        randomGenerator.nextBytes(buffer.array());

        for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
          MTInitiator.write(InitiatorTest.MultiThreadingTest.this,
              TARGET_DRIVE_NAME, buffer, randomGenerator.nextInt(), buffer
                  .remaining());
          buffer.rewind();
        }
      } catch (Exception e) {
        fail();
      }
    }

  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
