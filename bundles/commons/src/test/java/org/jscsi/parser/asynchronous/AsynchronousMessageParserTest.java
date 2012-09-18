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
package org.jscsi.parser.asynchronous;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnitTest;
import org.jscsi.utils.WiresharkMessageParser;

/**
 * Testing the correctness of the AsynchronousMessageParser.
 * 
 * @author Volker Wildi
 */
public class AsynchronousMessageParserTest extends ProtocolDataUnitTest {

    private static final String TEST_CASE_1 = "32 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "FF FF FF FF 00 00 00 00 00 00 00 00 00 00 00 00 ";

    @Test
    public void test() {

        // TODO: Remove this if the tests are working
    }

    /**
     * This test case validates the parsing process.
     * 
     * @throws IOException
     *             This exception should be never thrown.
     * @throws InternetSCSIException
     *             This exception should be never thrown.
     * @throws DigestException
     *             This exception should be never thrown.
     */
    @Test(enabled = false)
    public void testDeserialize1() throws IOException, InternetSCSIException, DigestException {

        super.setUp(TEST_CASE_1);
        super.testDeserialize(false, true, OperationCode.ASYNC_MESSAGE, 0x00000000, 0x00000001, 0x48044B81);

        assertTrue(recognizedParser instanceof AsynchronousMessageParser);

        AsynchronousMessageParser parser = (AsynchronousMessageParser)recognizedParser;

        assertEquals(0x0000000000000000L, parser.getLogicalUnitNumber());

        assertEquals(0x00000002, parser.getExpectedCommandSequenceNumber());
        assertEquals(0x00000009, parser.getMaximumCommandSequenceNumber());
    }

    /**
     * This test case validates the serialization process.
     * 
     * @throws InternetSCSIException
     *             This exception should be never thrown.
     * @throws IOException
     *             This exception should be never thrown.
     * @throws DigestException
     *             This exception should be never thrown.
     */
    @Test(enabled = false)
    public void testSerialize() throws InternetSCSIException, IOException, DigestException {

        super.setUp(TEST_CASE_1);
        // int[] expectedResult = WiresharkMessageParser.parse(TEST_CASE_1);
        ByteBuffer expectedResult = WiresharkMessageParser.parseToByteBuffer(TEST_CASE_1);

        // assertTrue(Arrays.equals(expectedResult,
        // protocolDataUnit.serialize()));
        assertTrue(expectedResult.equals(protocolDataUnit.serialize()));
    }

}
