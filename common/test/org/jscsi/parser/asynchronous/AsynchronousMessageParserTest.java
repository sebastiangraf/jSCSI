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
 * $Id: AsynchronousMessageParserTest.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.asynchronous;

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

/**
 * Testing the correctness of the AsynchronousMessageParser.
 * 
 * @author Volker Wildi
 */
public class AsynchronousMessageParserTest extends ProtocolDataUnitTest {

  private static final String TEST_CASE_1 = "32 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
      + "FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 ";

  /**
   * This test case validates the parsing process.
   * 
   * @throws IOException
   *           This exception should be never thrown.
   * @throws InternetSCSIException
   *           This exception should be never thrown.
   * @throws DigestException
   *           This exception should be never thrown.
   */
  @Ignore
  public void testDeserialize1() throws IOException, InternetSCSIException,
      DigestException {

    super.setUp(TEST_CASE_1);
    super.testDeserialize(false, true, OperationCode.ASYNC_MESSAGE, 0x00000000,
        0x00000001, 0x48044B81);

    assertTrue(recognizedParser instanceof AsynchronousMessageParser);

    AsynchronousMessageParser parser = (AsynchronousMessageParser) recognizedParser;

    assertEquals(0x0000000000000000L, parser.getLogicalUnitNumber());

    assertEquals(0x00000002, parser.getExpectedCommandSequenceNumber());
    assertEquals(0x00000009, parser.getMaximumCommandSequenceNumber());
  }

  /**
   * This test case validates the serialization process.
   * 
   * @throws InternetSCSIException
   *           This exception should be never thrown.
   * @throws IOException
   *           This exception should be never thrown.
   * @throws DigestException
   *           This exception should be never thrown.
   */
  @Ignore
  public void testSerialize() throws InternetSCSIException, IOException,
      DigestException {

    super.setUp(TEST_CASE_1);
    // int[] expectedResult = WiresharkMessageParser.parse(TEST_CASE_1);
    ByteBuffer expectedResult = WiresharkMessageParser
        .parseToByteBuffer(TEST_CASE_1);

    // assertTrue(Arrays.equals(expectedResult, protocolDataUnit.serialize()));
    assertTrue(expectedResult.equals(protocolDataUnit.serialize()));
  }

}
