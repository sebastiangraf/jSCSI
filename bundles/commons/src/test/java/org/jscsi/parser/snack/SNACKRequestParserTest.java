/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * SNACKRequestParserTest.java 2500 2007-03-05 13:29:08Z lemke $
 */

package org.jscsi.parser.snack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;

import org.jscsi.core.utils.WiresharkMessageParser;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnitTest;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.snack.SNACKRequestParser.SNACKType;
import org.junit.Test;

/**
 * Testing the correctness of the SNACKRequestParser.
 * 
 * @author Volker Wildi
 */
public class SNACKRequestParserTest extends ProtocolDataUnitTest {

  /**
   * Valid Test Case with the following expected values. <blockquote> Immediate
   * Flag = <code>false</code><br/> Operation Code = <code>SNACK_REQUEST</code>
   * <br/> Final Flag = <code>true</code><br/> Type =
   * <code>DATA_R2T_SNACK</code><br/w> TotalAHSLength = <code>0x00000000</code>
   * <br/> DataSegmentLength = <code>0x00000000</code><br/> LUN =
   * <code>0x0000000000000000</code><br/> InitiatorTaskTag =
   * <code>0xFFFFFFFF</code><br/> TargetTransferTag = <code>0xFFFFFFFF</code>
   * <br/> ExpCmdSN = <code>0x00000000</code><br/> BegRun =
   * <code>0x00000000</code><br/> RunLength = <code>0x00000000</code><br/>
   * </blockquote>
   */
  private static final String TEST_CASE_1 = "10 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
      + "FF FF FF FF FF FF FF FF 00 00 00 00 00 00 00 00 "
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
    super.testDeserialize(false, true, OperationCode.SNACK_REQUEST, 0x00000000,
        0x00000000, 0xFFFFFFFF);

    assertTrue(recognizedParser instanceof SNACKRequestParser);

    SNACKRequestParser parser = (SNACKRequestParser) recognizedParser;

    // test SNACK request fields
    assertEquals(SNACKType.DATA_R2T_SNACK, parser.getType());
    assertEquals(0x0000000000000000L, parser.getLogicalUnitNumber());
    assertEquals(0xFFFFFFFF, parser.getTargetTransferTag());
    assertEquals(0x0000, parser.getExpectedStatusSequenceNumber());
    assertEquals(0x0000, parser.getBegRun());
    assertEquals(0x0000, parser.getRunLength());
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
