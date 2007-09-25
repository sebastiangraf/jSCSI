/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id: TextParameterDataSegmentTest.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.datasegment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.junit.Test;

/**
 * Testing the correctness of the TextParameterDataSegment.
 * 
 * @author Volker Wildi
 * 
 */
public final class TextParameterDataSegmentTest {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  private static final int CHUNK_SIZE = 3;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Tests the constructor, if a valid maximum length is given.
   */
  @Test
  public final void testConstructor1() {

    final TextParameterDataSegment dataSegment = new TextParameterDataSegment(
        CHUNK_SIZE);
    assertNotNull(dataSegment);
    assertEquals(0, dataSegment.getLength());
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Tests the clear method. The result has to be an empty object.
   * 
   * @throws Exception
   *           should never be thrown.
   */
  @Test
  public final void testClear() throws Exception {

    final TextParameterDataSegment dataSegment = new TextParameterDataSegment(
        CHUNK_SIZE);
    assertNotNull(dataSegment);

    assertTrue(dataSegment.getSettings().entrySet().isEmpty());
    dataSegment.add(OperationalTextKey.HEADER_DIGEST, "Yes");
    assertFalse(dataSegment.getSettings().entrySet().isEmpty());

    dataSegment.clear();

    assertNotNull(dataSegment);
    assertTrue(dataSegment.getSettings().entrySet().isEmpty());
  }

  /**
   * Tests the correct functionality of the add method.
   * 
   * @throws Exception
   *           should never be thrown.
   */
  @Test
  public final void testAdd() throws Exception {

    final TextParameterDataSegment dataSegment = new TextParameterDataSegment(
        CHUNK_SIZE);
    assertNotNull(dataSegment);

    dataSegment.add(OperationalTextKey.DATA_DIGEST, "Yes");
    dataSegment.add(OperationalTextKey.HEADER_DIGEST, "No");
    dataSegment.add(OperationalTextKey.MAX_CONNECTIONS, "50");

    assertEquals(3, dataSegment.getSettings().entrySet().size());
    assertFalse(dataSegment.getSettings().entrySet().isEmpty());
  }

  /**
   * Tests the correct functionality of the addAll method.
   * 
   * @throws Exception
   *           should never be thrown.
   */
  @Test
  public final void testAddAll() throws Exception {

    final TextParameterDataSegment dataSegment = new TextParameterDataSegment(
        CHUNK_SIZE);

    assertNotNull(dataSegment);

    dataSegment.add(OperationalTextKey.DATA_DIGEST, "Yes");
    dataSegment.add(OperationalTextKey.HEADER_DIGEST, "No");
    dataSegment.add(OperationalTextKey.MAX_CONNECTIONS, "50");

    assertEquals(3, dataSegment.getSettings().entrySet().size());
    assertFalse(dataSegment.getSettings().entrySet().isEmpty());

    final TextParameterDataSegment anotherDataSegment = new TextParameterDataSegment(
        CHUNK_SIZE);
    assertNotNull(anotherDataSegment);

    anotherDataSegment.addAll(dataSegment.getSettings());
    assertNotNull(anotherDataSegment);
    assertEquals(3, anotherDataSegment.getSettings().entrySet().size());
    assertFalse(anotherDataSegment.getSettings().entrySet().isEmpty());
  }

  /**
   * Tests the correct functionality of the equals method. The two compared
   * <code>TextParameterDataSegment</code> has to be treated as equal.
   * 
   * @throws Exception
   *           should never be thrown.
   */
  @Test
  public final void testEquals() throws Exception {

    final TextParameterDataSegment dataSegment = new TextParameterDataSegment(
        CHUNK_SIZE);

    assertNotNull(dataSegment);
    assertEquals(0, dataSegment.getLength());

    dataSegment.add(OperationalTextKey.DATA_DIGEST, "Yes");
    dataSegment.add(OperationalTextKey.HEADER_DIGEST, "No");
    dataSegment.add(OperationalTextKey.MAX_CONNECTIONS, "50");

    assertEquals(3, dataSegment.getSettings().entrySet().size());
    assertFalse(dataSegment.getSettings().entrySet().isEmpty());

    final TextParameterDataSegment anotherDataSegment = new TextParameterDataSegment(
        CHUNK_SIZE);
    assertNotNull(anotherDataSegment);

    anotherDataSegment.add(OperationalTextKey.HEADER_DIGEST, "No");
    anotherDataSegment.add(OperationalTextKey.DATA_DIGEST, "Yes");
    anotherDataSegment.add(OperationalTextKey.MAX_CONNECTIONS, "50");

    assertEquals(3, anotherDataSegment.getSettings().entrySet().size());
    assertFalse(anotherDataSegment.getSettings().entrySet().isEmpty());

    assertTrue(dataSegment.equals(anotherDataSegment));
  }

  /**
   * Tests the correct functionality of the equals method. The two compared
   * <code>TextParameterDataSegment</code> has to be treated as unequal.
   * 
   * @throws Exception
   *           should never be thrown.
   */
  @Test
  public final void testUnEquals() throws Exception {

    final TextParameterDataSegment dataSegment = new TextParameterDataSegment(
        CHUNK_SIZE);

    assertNotNull(dataSegment);
    assertEquals(0, dataSegment.getLength());

    dataSegment.add(OperationalTextKey.DATA_DIGEST, "Yes");
    dataSegment.add(OperationalTextKey.HEADER_DIGEST, "No");
    dataSegment.add(OperationalTextKey.MAX_CONNECTIONS, "50");

    assertEquals(3, dataSegment.getSettings().entrySet().size());
    assertFalse(dataSegment.getSettings().entrySet().isEmpty());

    final TextParameterDataSegment anotherDataSegment = new TextParameterDataSegment(
        CHUNK_SIZE);
    assertNotNull(anotherDataSegment);

    anotherDataSegment.add(OperationalTextKey.DATA_DIGEST, "Yes");
    anotherDataSegment.add(OperationalTextKey.HEADER_DIGEST, "No");
    anotherDataSegment.add(OperationalTextKey.MAX_CONNECTIONS, "55");

    assertEquals(3, anotherDataSegment.getSettings().entrySet().size());
    assertFalse(anotherDataSegment.getSettings().entrySet().isEmpty());
    assertFalse(dataSegment.equals(anotherDataSegment));
  }

  /**
   * Tests the correct functionality of the serialize and the deserialize
   * method.
   */
  @Test
  public final void testSerializeAndDeserialize() {

    final TextParameterDataSegment dataSegment = new TextParameterDataSegment(
        CHUNK_SIZE);

    assertNotNull(dataSegment);
    dataSegment.add(OperationalTextKey.DATA_DIGEST, "Yes");
    dataSegment.add(OperationalTextKey.HEADER_DIGEST, "No");
    dataSegment.add(OperationalTextKey.MAX_CONNECTIONS, "50");

    final int totalSize = AbstractDataSegment.getTotalLength(dataSegment
        .getLength());
    final ByteBuffer serialized = ByteBuffer.allocate(totalSize);
    assertEquals(totalSize, dataSegment.serialize(serialized, 0));
    serialized.rewind();

    final TextParameterDataSegment anotherDataSegment = new TextParameterDataSegment(
        dataSegment.getLength());
    assertEquals(AbstractDataSegment.getTotalLength(dataSegment.getLength()),
        anotherDataSegment.deserialize(serialized, serialized.remaining()));
    assertNotNull(anotherDataSegment);
    assertEquals(AbstractDataSegment.getTotalLength(dataSegment.getLength()),
        anotherDataSegment.getLength());
    assertEquals(AbstractDataSegment.getTotalLength(dataSegment.getLength()),
        AbstractDataSegment.getTotalLength(anotherDataSegment.getLength()));

    assertFalse(dataSegment.equals(anotherDataSegment));
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
}
