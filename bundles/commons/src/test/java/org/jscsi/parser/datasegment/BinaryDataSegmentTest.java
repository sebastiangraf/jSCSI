/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * BinaryDataSegmentTest.java 2500 2007-03-05 13:29:08Z lemke $
 */

package org.jscsi.parser.datasegment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.junit.Test;

/**
 * Testing the correctness of the BinaryDataSegment.
 * 
 * @author Volker Wildi
 */
public final class BinaryDataSegmentTest {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  private static final int CHUNK_SIZE = 4;

  /** Test case arrays. */
  private static final byte[] TEST_CASE_1_ARRAY = { 0x34, 0x54, 0x77, 0x32,
      (byte) 0xAF };

  private static final byte[] TEST_CASE_1_ARRAY_LONG = { 0x34, 0x54, 0x77,
      0x32, (byte) 0xAF, 0x00, 0x00, 0x00 };

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @Test
  public final void testDeserializeBuffer() {

    final BinaryDataSegment dataSegment = new BinaryDataSegment(CHUNK_SIZE);
    assertNotNull(dataSegment);
    assertEquals(0, dataSegment.getLength());

    final ByteBuffer testBuffer = ByteBuffer.wrap(TEST_CASE_1_ARRAY);
    dataSegment.deserialize(testBuffer, TEST_CASE_1_ARRAY.length);
    assertEquals(TEST_CASE_1_ARRAY.length, dataSegment.getLength());

    final ByteBuffer expectedResult = ByteBuffer.wrap(TEST_CASE_1_ARRAY_LONG);
    dataSegment.dataBuffer.rewind();
    assertTrue(expectedResult.equals(dataSegment.dataBuffer));
  }

  @Test
  public final void testAppendBuffer() {

    final ByteBuffer testBuffer = ByteBuffer.wrap(TEST_CASE_1_ARRAY);
    assertNotNull(testBuffer);

    final BinaryDataSegment dataSegment = new BinaryDataSegment(CHUNK_SIZE);
    dataSegment.append(testBuffer, testBuffer.remaining());

    assertNotNull(dataSegment);
    assertEquals(TEST_CASE_1_ARRAY.length, dataSegment.getLength());

    final ByteBuffer expectedResult = ByteBuffer.wrap(TEST_CASE_1_ARRAY_LONG);
    dataSegment.dataBuffer.rewind();
    assertTrue(expectedResult.equals(dataSegment.dataBuffer));
  }

  @Test
  public final void testSerializeBuffer() {

    final BinaryDataSegment dataSegment = new BinaryDataSegment(CHUNK_SIZE);
    assertNotNull(dataSegment);
    assertEquals(0, dataSegment.getLength());

    final ByteBuffer testBuffer = ByteBuffer.wrap(TEST_CASE_1_ARRAY);
    dataSegment.deserialize(testBuffer, TEST_CASE_1_ARRAY.length);
    assertEquals(TEST_CASE_1_ARRAY.length, dataSegment.getLength());

    final ByteBuffer expectedResult = ByteBuffer.wrap(TEST_CASE_1_ARRAY_LONG);
    dataSegment.dataBuffer.rewind();

    final ByteBuffer exportedDataSegment = ByteBuffer
        .allocate(AbstractDataSegment.getTotalLength(dataSegment.getLength()));
    dataSegment.serialize(exportedDataSegment, 0);
    exportedDataSegment.rewind();

    assertTrue(expectedResult.equals(exportedDataSegment));
  }

  /**
   * Tests the clear method. The result has to be an empty object.
   */
  @Test
  public final void testClear() {

    final BinaryDataSegment dataSegment = new BinaryDataSegment(CHUNK_SIZE);

    assertNotNull(dataSegment);
    assertEquals(0, dataSegment.getLength());

    final ByteBuffer testBuffer = ByteBuffer.wrap(TEST_CASE_1_ARRAY);
    dataSegment.deserialize(testBuffer, TEST_CASE_1_ARRAY.length);
    assertEquals(TEST_CASE_1_ARRAY.length, dataSegment.getLength());

    dataSegment.clear();

    assertNotNull(dataSegment);
    assertEquals(0, dataSegment.getLength());
  }

  /**
   * Tests the equals method. The result has to be <code>true</code>.
   */
  @Test
  public final void testEquals() {

    final ByteBuffer testBuffer = ByteBuffer.allocate(TEST_CASE_1_ARRAY.length);
    assertNotNull(testBuffer);
    testBuffer.put(TEST_CASE_1_ARRAY);
    testBuffer.rewind();

    final BinaryDataSegment dataSegment = new BinaryDataSegment(CHUNK_SIZE);
    dataSegment.append(testBuffer, testBuffer.limit());
    assertNotNull(dataSegment);
    assertEquals(5, dataSegment.getLength());

    final BinaryDataSegment anotherDataSegment = new BinaryDataSegment(
        CHUNK_SIZE);
    testBuffer.rewind();
    anotherDataSegment.append(testBuffer, testBuffer.limit());
    assertNotNull(anotherDataSegment);
    assertEquals(5, anotherDataSegment.getLength());

    assertTrue(dataSegment.equals(anotherDataSegment));
  }

  /**
   * Tests the correct functionality of the serialize method. The given
   * destination buffer has size, which is not a multiple of
   * Constants.BYTES_PER_INT.
   */
  @Test(expected = IllegalArgumentException.class)
  public final void testSerialize2() {

    final BinaryDataSegment dataSegment = new BinaryDataSegment(CHUNK_SIZE);
    final ByteBuffer serialized = ByteBuffer.allocate(TEST_CASE_1_ARRAY.length);
    dataSegment.serialize(serialized, 0);
  }

  /**
   * Tests the correct functionality of the serialize and the deserialize
   * method.
   */
  @Test
  public final void testSerializeAndDeserialize() {

    final ByteBuffer testBuffer = ByteBuffer.wrap(TEST_CASE_1_ARRAY);
    final BinaryDataSegment dataSegment = new BinaryDataSegment(CHUNK_SIZE);
    assertEquals(0, dataSegment.getLength());

    dataSegment.append(testBuffer, testBuffer.remaining());
    assertNotNull(dataSegment);
    assertEquals(TEST_CASE_1_ARRAY.length, dataSegment.getLength());

    final int totalSize = AbstractDataSegment
        .getTotalLength(TEST_CASE_1_ARRAY.length);
    final ByteBuffer serialized = ByteBuffer.allocate(totalSize);
    assertEquals(totalSize, dataSegment.serialize(serialized, 0));
    serialized.rewind();

    final BinaryDataSegment anotherDataSegment = new BinaryDataSegment(
        CHUNK_SIZE);
    assertNotNull(anotherDataSegment);
    assertEquals(0, anotherDataSegment.getLength());

    assertEquals(totalSize, anotherDataSegment.deserialize(serialized,
        serialized.remaining()));
    assertNotNull(anotherDataSegment);
    assertEquals(totalSize, anotherDataSegment.getLength());
  }

  /**
   * Tests the correct functionality of the <code>DataSegmentFactory</code> and
   * the corresponding <code>DataSegmentIterator</code>.
   */
  @Test
  public final void testDataSegmentIterator() {

    final ByteBuffer testBuffer = ByteBuffer.allocate(10 * 16 * 1024);
    final IDataSegment dataSegment = DataSegmentFactory.create(testBuffer,
        10000, 57344, DataSegmentFormat.BINARY, 8192);
    final IDataSegmentIterator iterator = dataSegment.iterator();

    int counter = 0;
    while (iterator.hasNext()) {
      iterator.next(8192);
      counter++;
    }

    assertEquals(7, counter);
  }

  /**
   * Tests the correct functionality of the <code>DataSegmentFactory</code> and
   * the corresponding <code>DataSegmentIterator</code>.
   */
  @Test
  public final void testDataSegmentIterator2() {

    final ByteBuffer testBuffer = ByteBuffer.allocate(10 * 16 * 1024);
    final IDataSegment dataSegment = DataSegmentFactory.create(testBuffer,
        10000, 56344, DataSegmentFormat.BINARY, 8192);
    final IDataSegmentIterator iterator = dataSegment.iterator();

    int counter = 0;
    while (iterator.hasNext()) {
      iterator.next(8192);
      counter++;
    }

    assertEquals(7, counter);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
}
