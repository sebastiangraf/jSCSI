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
 * $Id: LogoutResponseParserTest.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.logout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;

import org.jscsi.core.utils.WiresharkMessageParser;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnitTest;
import org.jscsi.parser.exception.InternetSCSIException;
import org.junit.Test;

/**
 * Testing the correctness of the LogoutResponseParser.
 * 
 * @author Volker Wildi
 * 
 */
public class LogoutResponseParserTest extends ProtocolDataUnitTest {

  /**
   * Valid Test Case with the following expected values.
   * 
   * <blockquote>
   * 
   * Immediate Flag = <code>false</code><br/>
   * 
   * Operation Code = <code>LOGOUT_RESPONSE</code><br/>
   * 
   * Final Flag (Transit Flag) = <code>true</code><br/>
   * 
   * Continue Flag = <code>false</code><br/>
   * 
   * TotalAHSLength = <code>0x00000000</code><br/>
   * 
   * DataSegmentLength = <code>0x00000000</code><br/>
   * 
   * InitiatorTaskTag = <code>0x0001A2CC</code><br/>
   * 
   * TSIH = <code>0x0000</code><br/>
   * 
   * CID = <code>0x0000</code><br/>
   * 
   * Response = <code>CONNECTION_CLOSED_SUCCESSFULLY</code><br/>
   * 
   * StatSN = <code>0x0001A2CD</code><br/>
   * 
   * ExpCmdSN = <code>0x0001A2CC</code><br/>
   * 
   * MaxCmdSN = <code>0x0001A2EC</code><br/><br/>
   * 
   * Time2Wait = <code>0x0000</code><br/>
   * 
   * Time2Retain = <code>0x0000</code><br/>
   * 
   * </blockquote>
   */
  private static final String TEST_CASE_1 = "26 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
      + "00 01 a2 cc 00 00 00 00 00 01 a2 cd 00 01 a2 cc "
      + "00 01 a2 ec 00 00 00 00 00 00 00 00 00 00 00 00";

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
    super.testDeserialize(false, true, OperationCode.LOGOUT_RESPONSE,
        0x00000000, 0x00000000, 0x0001A2CC);

    assertTrue(recognizedParser instanceof LogoutResponseParser);

    LogoutResponseParser parser = (LogoutResponseParser) recognizedParser;

    super.testDataSegment("");

    assertEquals(LogoutResponse.CONNECTION_CLOSED_SUCCESSFULLY, parser
        .getResponse());
    assertEquals(0x0001A2CD, parser.getStatusSequenceNumber());
    assertEquals(0x0001A2CC, parser.getExpectedCommandSequenceNumber());
    assertEquals(0x0001A2EC, parser.getMaximumCommandSequenceNumber());
    assertEquals((short) 0x0000, parser.getTime2Wait());
    assertEquals((short) 0x0000, parser.getTime2Retain());
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
