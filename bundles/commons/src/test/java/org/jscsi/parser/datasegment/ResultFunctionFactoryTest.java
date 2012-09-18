/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jscsi.parser.datasegment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.util.NoSuchElementException;

import org.jscsi.parser.datasegment.ResultFunctionFactory.ChooseResultFunction;

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
        final IResultFunction resultFunction = resultFunctionFactory.create("Choose");

        assertTrue(resultFunction instanceof ChooseResultFunction);
        assertEquals("CRC32CDigest", resultFunction.result("CRC32CDigest,None", "MD5,CRC32CDigest,None"));
    }

    @Test
    public final void testValidChoose2() {

        final ResultFunctionFactory resultFunctionFactory = new ResultFunctionFactory();
        final IResultFunction resultFunction = resultFunctionFactory.create("Choose");

        assertTrue(resultFunction instanceof ChooseResultFunction);
        assertEquals("None", resultFunction.result("CRC32,None", "MD5,CRC32CDigest,None"));

    }

    @Test(expectedExceptions = NoSuchElementException.class)
    public final void testInvalidChoose() {

        final ResultFunctionFactory resultFunctionFactory = new ResultFunctionFactory();
        final IResultFunction resultFunction = resultFunctionFactory.create("Choose");

        assertTrue(resultFunction instanceof ChooseResultFunction);
        resultFunction.result("CRC32", "MD5,CRC32CDigest");
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
