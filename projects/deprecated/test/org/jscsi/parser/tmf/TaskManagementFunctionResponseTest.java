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
 * $Id: TaskManagementFunctionResponseTest.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.tmf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;

import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnitTest;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.utils.WiresharkMessageParser;
import org.junit.Ignore;

/**
 * Testing the correctness of the TaskManagementFunctionResponseParser.
 * 
 * @author Volker Wildi
 * 
 */
public class TaskManagementFunctionResponseTest extends ProtocolDataUnitTest {

  private static final String TEST_CASE_1 = "e2 d3 3d c7 ef 54 95 6b 3d f7 c7 d7 ea 5d 7f ae "
      + "ed 3b c6 c6 e4 05 ec 1c bd ff c5 ec 5b 70 0a e7 "
      + "2f d6 f1 1a 4d be d8 ec 5b 2f be bc 4c 99 dc 80";

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
    super.testDeserialize(false, true, OperationCode.SCSI_TM_RESPONSE,
        0x00000000, 0x00000000, 0xECF407ED);

    assertTrue(recognizedParser instanceof TaskManagementFunctionResponseParser);

    TaskManagementFunctionResponseParser parser = (TaskManagementFunctionResponseParser) recognizedParser;

    assertEquals(0, parser.getResponse());
    assertEquals(0xA6C84985, parser.getStatusSequenceNumber());
    assertEquals(0xB86232D6, parser.getExpectedCommandSequenceNumber());
    assertEquals(0x60126FF5, parser.getMaximumCommandSequenceNumber());
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
