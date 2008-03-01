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
 * $Id: TextRequestParserTest.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;

import org.jscsi.core.utils.WiresharkMessageParser;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnitTest;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.datasegment.SettingsMap;
import org.jscsi.parser.exception.InternetSCSIException;
import org.junit.Test;

/**
 * Testing the correctness of the TextRequestParser.
 * 
 * @author Volker Wildi
 * 
 */
public class TextRequestParserTest extends ProtocolDataUnitTest {

  /**
   * Valid Test Case with the following expected values.
   * 
   * <blockquote>
   * 
   * Immediate Flag = <code>true</code><br/>
   * 
   * Operation Code = <code>TEXT_REQUEST</code><br/>
   * 
   * Final Flag = <code>true</code><br/>
   * 
   * Continue Flag = <code>false</code><br/>
   * 
   * TotalAHSLength = <code>0x00000000</code><br/>
   * 
   * DataSegmentLength = <code>0x00000010</code><br/>
   * 
   * InitiatorTaskTag = <code>0x03000000</code><br/>
   * 
   * TargetTaskTag = <code>0xFFFFFFFF</code><br/>
   * 
   * CmdSN = <code>0x00000000</code><br/>
   * 
   * ExpStatSN = <code>0x00000000</code><br/><br/>
   * 
   * <b>Key-Values:</b><br/>
   * 
   * SEND_TARGETS = <code>all</code><br/>
   * 
   * </blockquote>
   */
  private static final String TEST_CASE_1 = "44 80 00 00 00 00 00 10 00 00 00 00 00 00 00 00 "
      + "03 00 00 00 ff ff ff ff 00 00 00 00 00 00 00 00 "
      + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
      + "53 65 6e 64 54 61 72 67 65 74 73 3d 61 6c 6c 00";

  /**
   * This test case validates the parsing process.
   * 
   * @throws IOException
   *           This exception should be never thrown.
   * @throws InternetSCSIException
   *           This exception should be never thrown.
   */
  @Test
  public void testDeserialize1() throws IOException, InternetSCSIException,
      DigestException {

    SettingsMap expectedKeyValuePair = new SettingsMap();
    expectedKeyValuePair.add(OperationalTextKey.SEND_TARGETS, "all");

    super.setUp(TEST_CASE_1);
    super.testDeserialize(true, true, OperationCode.TEXT_REQUEST, 0x00000000,
        0x00000010, 0x03000000);
    super.testDataSegment(expectedKeyValuePair);

    assertTrue(recognizedParser instanceof TextRequestParser);

    TextRequestParser parser = (TextRequestParser) recognizedParser;

    assertFalse(parser.isContinueFlag());
    assertEquals(0x0000000000000000L, parser.getLogicalUnitNumber());

    assertEquals(0xFFFFFFFF, parser.getTargetTransferTag());
    assertEquals(0x00000000, parser.getCommandSequenceNumber());
    assertEquals(0x00000000, parser.getExpectedStatusSequenceNumber());
  }

  /**
   * This test case validates the serialization process.
   * 
   * @throws InternetSCSIException
   *           This exception should be never thrown.
   * @throws IOException
   *           This exception should be never thrown.
   */
  @Test
  public void testSerialize1() throws InternetSCSIException, IOException,
      DigestException {

    super.setUp(TEST_CASE_1);

    ByteBuffer expectedResult = WiresharkMessageParser
        .parseToByteBuffer(TEST_CASE_1);
    assertTrue(expectedResult.equals(protocolDataUnit.serialize()));
  }
}
