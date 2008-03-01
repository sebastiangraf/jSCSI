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
 * $Id: TextResponseParserTest.java 2500 2007-03-05 13:29:08Z lemke $
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
 * Testing the correctness of the TextResponseParser.
 * 
 * @author Volker Wildi
 * 
 */
public class TextResponseParserTest extends ProtocolDataUnitTest {

  /**
   * Valid Test Case with the following expected values.
   * 
   * <blockquote>
   * 
   * Immediate Flag = <code>false</code><br/>
   * 
   * Operation Code = <code>TEXT_RESPONSE</code><br/>
   * 
   * Final Flag = <code>true</code><br/>
   * 
   * Continue Flag = <code>false</code><br/>
   * 
   * TotalAHSLength = <code>0x00000000</code><br/>
   * 
   * DataSegmentLength = <code>0x0000004E</code><br/>
   * 
   * InitiatorTaskTag = <code>0x03000000</code><br/>
   * 
   * TargetTaskTag = <code>0xFFFFFFFF</code><br/>
   * 
   * StatSN = <code>0x00000002</code><br/>
   * 
   * ExpCmdSN = <code>0x00000000</code><br/>
   * 
   * MaxCmdSN = <code>0x00000000</code><br/><br/>
   * 
   * <b>Key-Values:</b><br/>
   * 
   * TARGET_NAME =
   * <code>iqn.1987-05.com.cisco.00.58031e1d068ac226d385847592c0b670.IBM-Disk</code><br/>
   * 
   * </blockquote>
   */
  private static final String TEST_CASE_1 = "24 80 00 00 00 00 00 4e 00 00 00 00 00 00 00 00 "
      + "03 00 00 00 ff ff ff ff 00 00 00 02 00 00 00 00 "
      + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
      + "54 61 72 67 65 74 4e 61 6d 65 3d 69 71 6e 2e 31 "
      + "39 38 37 2d 30 35 2e 63 6f 6d 2e 63 69 73 63 6f "
      + "2e 30 30 2e 35 38 30 33 31 65 31 64 30 36 38 61 "
      + "63 32 32 36 64 33 38 35 38 34 37 35 39 32 63 30 "
      + "62 36 37 30 2e 49 42 4d 2d 44 69 73 6b 00 00 00";

  /**
   * This test case validates the parsing process.
   * 
   * @throws IOException
   *           This exception should be never thrown.
   * @throws InternetSCSIException
   *           This exception should be never thrown.
   */
  @Test
  public void testDeserialize() throws IOException, InternetSCSIException,
      DigestException {

    SettingsMap expectedKeyValuePair = new SettingsMap();
    expectedKeyValuePair.add(OperationalTextKey.TARGET_NAME,
        "iqn.1987-05.com.cisco.00.58031e1d068ac226d385847592c0b670.IBM-Disk");

    super.setUp(TEST_CASE_1);
    super.testDeserialize(false, true, OperationCode.TEXT_RESPONSE, 0x00000000,
        0x0000004E, 0x03000000);
    super.testDataSegment(expectedKeyValuePair);

    assertTrue(recognizedParser instanceof TextResponseParser);

    TextResponseParser parser = (TextResponseParser) recognizedParser;

    assertFalse(parser.isContinueFlag());
    assertEquals(0x0000000000000000L, parser.getLogicalUnitNumber());

    assertEquals(0xFFFFFFFF, parser.getTargetTransferTag());
    assertEquals(0x00000002, parser.getStatusSequenceNumber());
    assertEquals(0x00000000, parser.getExpectedCommandSequenceNumber());
    assertEquals(0x00000000, parser.getMaximumCommandSequenceNumber());
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
  public void testSerialize() throws InternetSCSIException, IOException,
      DigestException {

    super.setUp(TEST_CASE_1);

    ByteBuffer expectedResult = WiresharkMessageParser
        .parseToByteBuffer(TEST_CASE_1);
    assertTrue(expectedResult.equals(protocolDataUnit.serialize()));
  }

}
