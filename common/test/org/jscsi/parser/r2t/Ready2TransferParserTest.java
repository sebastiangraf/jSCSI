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
 * $Id: Ready2TransferParserTest.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.r2t;

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
 * Testing the correctness of the Ready2TransferParser.
 * 
 * @author Volker Wildi
 * 
 */
public class Ready2TransferParserTest extends ProtocolDataUnitTest {

  /**
   * Valid Test Case with the following expected values.
   * 
   * <blockquote>
   * 
   * Immediate Flag = <code>false</code><br/>
   * 
   * Operation Code = <code>R2T</code><br/>
   * 
   * Final Flag = <code>true</code><br/>
   * 
   * TotalAHSLength = <code>0x00000000</code><br/>
   * 
   * DataSegmentLength = <code>0x000000D9</code><br/>
   * 
   * LUN = <code>0x0000000000000000</code><br/>
   * 
   * TargetTranferTag = <code>0x00000001</code><br/>
   * 
   * StatSN = <code>0x000000DA</code><br/>
   * 
   * ExpCmdSN = <code>0x000000DA</code><br/>
   * 
   * MaxCmdSN = <code>0x000000FA</code><br/>
   * 
   * R2TSN = <code>0x00000000</code><br/>
   * 
   * BufferOffset = <code>0x00000000</code><br/>
   * 
   * DesiredDataTransferLength = <code>0x00001000</code><br/><br/>
   * 
   * </blockquote>
   */
  private static final String TEST_CASE_1 = "31 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
      + "00 00 00 d9 00 00 00 01 00 00 00 da 00 00 00 da "
      + "00 00 00 fa 00 00 00 00 00 00 00 00 00 00 10 00";

  /**
   * This test case validates the parsing process.
   * 
   * @throws IOException
   *           This exception should be never thrown.
   * @throws InternetSCSIException
   *           This exception should be never thrown.
   * @throws DigestException
   */
  @Test
  public void testDeserialize1() throws IOException, InternetSCSIException,
      DigestException {

    super.setUp(TEST_CASE_1);
    super.testDeserialize(false, true, OperationCode.R2T, 0x00000000,
        0x00000000, 0x000000D9);

    assertTrue(recognizedParser instanceof Ready2TransferParser);

    Ready2TransferParser parser = (Ready2TransferParser) recognizedParser;

    assertEquals(0x0000000000000000L, parser.getLogicalUnitNumber());

    assertEquals(0x00000001, parser.getTargetTransferTag());
    assertEquals(0x000000DA, parser.getExpectedCommandSequenceNumber());
    assertEquals(0x000000FA, parser.getMaximumCommandSequenceNumber());
    assertEquals(0x000000DA, parser.getStatusSequenceNumber());
    assertEquals(0x00000000, parser.getReady2TransferSequenceNumber());
    assertEquals(0x00000000, parser.getBufferOffset());
    assertEquals(0x00001000, parser.getDesiredDataTransferLength());
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
