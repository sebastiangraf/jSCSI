/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * AdditionalHeaderSegmentTest.java 2500 2007-03-05 13:29:08Z lemke $
 */

package org.jscsi.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.core.utils.WiresharkMessageParser;
import org.jscsi.parser.AdditionalHeaderSegment.AdditionalHeaderSegmentType;
import org.jscsi.parser.exception.InternetSCSIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing the correctness of the AdditionalHeaderSegment.
 * 
 * @author Volker Wildi
 */
public class AdditionalHeaderSegmentTest extends ProtocolDataUnitTest {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** Specific field of the TEST_CASE_1. */
  private static final String TEST_CASE_1_SPECIFIC_FIELD = "00 CA 25 26 C4";

  /**
   * Valid Test Case with the following expected values. <blockquote> AHSLength
   * = <code>0x0005</code><br/> AHSType =
   * <code>AdditionalHeaderSegmentType.EXPECTED_BIDIRECTIONAL_READ_DATA_LENGTH</code>
   * <br/> Expected Read-Data Length = <code>0xCA2526C4</code><br/>
   * </blockquote>
   */
  private static final String TEST_CASE_1 = "00 05 02" + " "
      + TEST_CASE_1_SPECIFIC_FIELD;

  private AdditionalHeaderSegment additionalHeaderSegment;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @Before
  public final void setUp() {

    additionalHeaderSegment = new AdditionalHeaderSegment();
  }

  @After
  public final void tearDown() {

    additionalHeaderSegment = null;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This test case validates the parsing process.
   * 
   * @throws IOException
   *           This exception should be never thrown.
   * @throws InternetSCSIException
   *           This exception should be never thrown.
   */
  @Test
  public final void testDeserialize1() throws InternetSCSIException {

    additionalHeaderSegment.deserialize(WiresharkMessageParser
        .parseToByteBuffer(TEST_CASE_1), 0);

    ByteBuffer expectedReadDataLength = WiresharkMessageParser
        .parseToByteBuffer(TEST_CASE_1_SPECIFIC_FIELD);

    assertEquals((short) 0x0005, additionalHeaderSegment.getLength());
    assertEquals(
        AdditionalHeaderSegmentType.EXPECTED_BIDIRECTIONAL_READ_DATA_LENGTH,
        additionalHeaderSegment.getType());
    assertTrue(expectedReadDataLength.equals(additionalHeaderSegment
        .getSpecificField()));

  }

  /**
   * This test case validates the serialization process.
   * 
   * @throws InternetSCSIException
   * @throws InternetSCSIException
   *           This exception should be never thrown.
   * @throws IOException
   *           This exception should be never thrown.
   */
  @Test
  public final void testSerialize1() throws InternetSCSIException {

    ByteBuffer expectedResult = WiresharkMessageParser
        .parseToByteBuffer(TEST_CASE_1);
    additionalHeaderSegment.deserialize(expectedResult, 0);
    ByteBuffer testSerialize = ByteBuffer.allocate(8);
    assertEquals(8, additionalHeaderSegment.serialize(testSerialize, 0));
    assertTrue(expectedResult.equals(testSerialize));
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
