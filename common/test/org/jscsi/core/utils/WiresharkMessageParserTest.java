/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * WiresharkMessageParserTest.java 2500 2007-03-05 13:29:08Z lemke $
 */

package org.jscsi.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;

import org.jscsi.parser.Constants;
import org.junit.Test;

/**
 * Tests the correct parsing of several <em>ethereal</em> trace logs.
 * 
 * @author Volker Wildi
 */
public class WiresharkMessageParserTest {

  /** Testing of a validate parsing process of a single line message. */
  @Test
  public void testParseSingleLineMessage() {

    String str = "43 00 02 02";
    int result = 1124073986;

    int[] test = WiresharkMessageParser.parseToIntArray(str);
    assertEquals(test[0], result);
  }

  /** Testing of a validate parsing process of multiple lines message. */
  @Test
  public void testParseMultiLineMessage() {

    String str = "43 00 02 02 69 74 69 61";
    int[] result = { 1124073986, 1769236833 };

    int[] test = WiresharkMessageParser.parseToIntArray(str);

    assertEquals(result.length, test.length);
    for (int i = 0; i < test.length; i++) {
      assertEquals(test[i], result[i]);
    }
  }

  /** Testing of a validate parsing process of a single line message. */
  @Test
  public void testParseSingleLineMessageToByteBuffer() {

    String str = "43 00 02 02";

    int result = 1124073986;
    ByteBuffer resultBuffer = ByteBuffer.allocate(Constants.BYTES_PER_INT);
    resultBuffer.putInt(result).rewind();

    ByteBuffer test = WiresharkMessageParser.parseToByteBuffer(str);
    assertTrue(resultBuffer.equals(test));
  }

  /** Testing of a validate parsing process of multiple lines message. */
  @Test
  public void testParseMultiLineMessageToByteBuffer() {

    String str = "43 00 02 02 69 74 69 61";

    int[] result = { 1124073986, 1769236833 };
    ByteBuffer resultBuffer = ByteBuffer.allocate(result.length
        * Constants.BYTES_PER_INT);

    for (int n : result) {
      resultBuffer.putInt(n);
    }
    resultBuffer.rewind();

    ByteBuffer test = WiresharkMessageParser.parseToByteBuffer(str);
    assertTrue(resultBuffer.equals(test));
  }
}
