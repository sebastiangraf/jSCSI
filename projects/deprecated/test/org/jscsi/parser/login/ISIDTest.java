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
 * $Id: ISIDTest.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.login.ISID.Format;
import org.junit.Test;

/**
 * Testing the correctness of the ISID.
 * 
 * @author Volker Wildi
 * 
 */
public class ISIDTest {

  /**
   * Valid Test Case with the following expected values.
   * 
   * <blockquote>
   * 
   * t: <code>OUI_FORMAT</code><br/>
   * 
   * a: <code>0x00</code><br/>
   * 
   * b: <code>0x0000</code><br/>
   * 
   * c: <code>0x00</code><br/>
   * 
   * d: <code>0xABCD</code><br/>
   * 
   * </blockquote>
   */
  private static final long TEST_CASE = 0x00000000ABCD0000L;

  /**
   * This test case validates the parsing process.
   * 
   * @throws InternetSCSIException
   */
  @Test
  public void testDeserialize1() throws InternetSCSIException {

    ISID isid = new ISID();
    ISID expectedISID = new ISID(Format.OUI_FORMAT, (byte) 0x00,
        (short) 0x0000, (byte) 0x00, (short) 0xABCD);

    isid.deserialize(TEST_CASE);

    assertTrue(expectedISID.equals(isid));
  }

  /**
   * This test case validates the serialization process.
   * 
   * @throws InternetSCSIException
   */
  @Test
  public void testSerialize1() throws InternetSCSIException {

    ISID isid = new ISID(Format.OUI_FORMAT, (byte) 0x00, (short) 0x0000,
        (byte) 0x00, (short) 0xABCD);

    assertEquals(TEST_CASE, isid.serialize());
  }
}
