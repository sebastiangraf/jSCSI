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
 * $Id: ResultFunctionFactoryTest.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.datasegment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;

import org.jscsi.parser.datasegment.ResultFunctionFactory.ChooseResultFunction;
import org.junit.Test;

/**
 * <h1>ResultFunctionFactoryTest</h1>
 * <p/>
 * 
 * @author Volker Wildi
 */
public final class ResultFunctionFactoryTest {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @Test
  public final void testValidChoose1() {

    final ResultFunctionFactory resultFunctionFactory = new ResultFunctionFactory();
    final IResultFunction resultFunction = resultFunctionFactory
        .create("Choose");

    assertTrue(resultFunction instanceof ChooseResultFunction);
    assertEquals("CRC32CDigest", resultFunction.result("CRC32CDigest,None",
        "MD5,CRC32CDigest,None"));
  }

  @Test
  public final void testValidChoose2() {

    final ResultFunctionFactory resultFunctionFactory = new ResultFunctionFactory();
    final IResultFunction resultFunction = resultFunctionFactory
        .create("Choose");

    assertTrue(resultFunction instanceof ChooseResultFunction);
    assertEquals("None", resultFunction.result("CRC32,None",
        "MD5,CRC32CDigest,None"));

  }

  @Test(expected = NoSuchElementException.class)
  public final void testInvalidChoose() {

    final ResultFunctionFactory resultFunctionFactory = new ResultFunctionFactory();
    final IResultFunction resultFunction = resultFunctionFactory
        .create("Choose");

    assertTrue(resultFunction instanceof ChooseResultFunction);
    resultFunction.result("CRC32", "MD5,CRC32CDigest");
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
