/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * InitiatorTest.java 2500 2007-03-05 13:29:08Z lemke $
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
  public final void testReadCapacity() throws Exception {

    assertEquals((long) 2097151, initiator.getCapacity(TARGET_DRIVE_NAME));
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
