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
 * $Id: LogoutRequestParserTest.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.logout;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.DigestException;

import org.jscsi.parser.ProtocolDataUnitTest;
import org.jscsi.parser.exception.InternetSCSIException;
import org.junit.Test;

/**
 * Testing the correctness of the LogoutRequestParser.
 * 
 * @author Volker Wildi
 * 
 */
public class LogoutRequestParserTest extends ProtocolDataUnitTest {

  /**
   * This Test Case violates the iSCSI Protocol Standard (RFC3720) and throws an
   * InternetSCSIException. The reason is, that the CID field must be zero, if
   * close session is requested.
   */
  private static final String TEST_CASE_1 = "46 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
      + "00 01 a2 cc 00 01 00 00 00 01 a2 cc 00 01 a2 cd "
      + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

  /**
   * This test case validates the recognition of an false Protocol Data Unit.
   * 
   * @throws IOException
   *           This exception should be never thrown.
   */
  @Test
  public void testDeserialize1() throws IOException, DigestException {

    try {
      super.setUp(TEST_CASE_1);
      // should never happen
      assertTrue(false);
    } catch (InternetSCSIException e) {
      assertTrue(true);
    }
  }

}
