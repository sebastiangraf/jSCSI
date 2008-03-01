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
 * $Id: RejectParserTest.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.reject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;

import org.jscsi.core.utils.WiresharkMessageParser;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnitTest;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.reject.RejectParser.ReasonCode;
import org.junit.Test;

/**
 * Testing the correctness of the RejectInParser.
 * 
 * @author Volker Wildi
 * 
 */
public class RejectParserTest extends ProtocolDataUnitTest {

  /**
   * Valid Test Case with the following expected values.
   * 
   * <blockquote>
   * 
   * Immediate Flag = <code>false</code><br/>
   * 
   * Operation Code = <code>REJECT</code><br/>
   * 
   * Final Flag = <code>true</code><br/>
   * 
   * Reason = <code>PROTOCOL_ERROR</code><br/>
   * 
   * TotalAHSLength = <code>0x00000000</code><br/>
   * 
   * DataSegmentLength = <code>0x00000030</code><br/>
   * 
   * LUN = <code>0x0000000000000000</code><br/>
   * 
   * InitiatorTaskTag = <code>0xFFFFFFFF</code><br/>
   * 
   * StatSN = <code>0x00000000</code><br/>
   * 
   * ExpCmdSN = <code>0x00000000</code><br/>
   * 
   * MaxCmdSN = <code>0x00000000</code><br/>
   * 
   * DataSN = <code>0x00000000</code><br/>
   * 
   * DataSegment =
   * <code>C3 00 02 02 00 00 00 95 00 00 00 00 AB CD 00 00 A8 97 69 81 00 00 00 00 00 00 01 5D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00</code><br/>
   * 
   * </blockquote>
   */
  private static final String TEST_CASE_1 = "3f 80 04 00 00 00 00 30 00 00 00 00 00 00 00 00 "
      + "ff ff ff ff 00 00 00 00 00 00 00 00 00 00 00 00 "
      + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
      + "c3 00 02 02 00 00 00 95 00 00 00 00 ab cd 00 00 "
      + "a8 97 69 81 00 00 00 00 00 00 01 5d 00 00 00 00 "
      + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

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

    super.setUp(TEST_CASE_1);
    super.testDeserialize(false, true, OperationCode.REJECT, 0x00000000,
        0x00000030, 0xFFFFFFFF);

    assertTrue(recognizedParser instanceof RejectParser);

    RejectParser parser = (RejectParser) recognizedParser;

    // test reject fields
    assertEquals(ReasonCode.PROTOCOL_ERROR, parser.getReasonCode());

    assertEquals(0x00000000, parser.getStatusSequenceNumber());
    assertEquals(0x00000000, parser.getExpectedCommandSequenceNumber());
    assertEquals(0x00000000, parser.getMaximumCommandSequenceNumber());
    assertEquals(0x00000000, parser.getDataSequenceNumber());
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
