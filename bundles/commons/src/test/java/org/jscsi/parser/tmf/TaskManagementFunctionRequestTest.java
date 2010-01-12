/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * TaskManagementFunctionRequestTest.java 2500 2007-03-05 13:29:08Z lemke $
 */

package org.jscsi.parser.tmf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;

import org.jscsi.core.utils.WiresharkMessageParser;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnitTest;
import org.jscsi.parser.exception.InternetSCSIException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Testing the correctness of the TaskManagementFunctionRequestParser.
 * 
 * @author Volker Wildi
 */
public class TaskManagementFunctionRequestTest extends ProtocolDataUnitTest {

  private static final String TEST_CASE_1 = "42 72 00 00 F6 D3 7A 9F 0C B2 38 2A A3 FE 69 E3 "
      + "B5 BF D4 9D D0 4F B3 91 E4 34 2C 30 F6 04 A9 5E "
      + "7F DA F2 1E AA EA F7 5A 00 00 00 00 00 00 00 00";

  @Test
  public void test(){
	  
	  //TODO: Remove this if the tests are working
  }
  
  /**
   * This test case validates the parsing process.
   * 
   * @throws IOException
   *           This exception should be never thrown.
   * @throws InternetSCSIException
   *           This exception should be never thrown.
   */
  @Ignore
  public void testDeserialize1() throws IOException, InternetSCSIException,
      DigestException {

    super.setUp(TEST_CASE_1);
    super.testDeserialize(true, true, OperationCode.SCSI_TM_REQUEST,
        0x000000F6, 0x00000000, 0xB5BFD49D);

    assertTrue(recognizedParser instanceof TaskManagementFunctionRequestParser);

    TaskManagementFunctionRequestParser parser = (TaskManagementFunctionRequestParser) recognizedParser;

    assertEquals(0xD04FB391, parser.getReferencedTaskTag());
    assertEquals(0xE4342C30, parser.getCommandSequenceNumber());
    assertEquals(0xF604A95E, parser.getExpectedStatusSequenceNumber());
    assertEquals(0x7FDAF21E, parser.getRefCmdSN());
    assertEquals(0xAAEAF75A, parser.getExpDataSN());
  }

  /**
   * This test case validates the serialization process.
   * 
   * @throws InternetSCSIException
   *           This exception should be never thrown.
   * @throws IOException
   *           This exception should be never thrown.
   */
  @Ignore
  public void testSerialize1() throws InternetSCSIException, IOException,
      DigestException {

    super.setUp(TEST_CASE_1);

    ByteBuffer expectedResult = WiresharkMessageParser
        .parseToByteBuffer(TEST_CASE_1);
    assertTrue(expectedResult.equals(protocolDataUnit.serialize()));
  }
}
