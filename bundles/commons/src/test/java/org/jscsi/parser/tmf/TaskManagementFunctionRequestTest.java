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
package org.jscsi.parser.tmf;

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
 * Testing the correctness of the TaskManagementFunctionRequestParser.
 * 
 * @author Volker Wildi
 */
public class TaskManagementFunctionRequestTest extends ProtocolDataUnitTest {

    private static final String TEST_CASE_1 = "42 72 00 00 F6 D3 7A 9F 0C B2 38 2A A3 FE 69 E3 "
        + "B5 BF D4 9D D0 4F B3 91 E4 34 2C 30 F6 04 A9 5E "
        + "7F DA F2 1E AA EA F7 5A 00 00 00 00 00 00 00 00";

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
     */
    @Test(enabled = false)
    public void testDeserialize1() throws IOException, InternetSCSIException, DigestException {

        super.setUp(TEST_CASE_1);
        super.testDeserialize(true, true, OperationCode.SCSI_TM_REQUEST, 0x000000F6, 0x00000000, 0xB5BFD49D);

        assertTrue(recognizedParser instanceof TaskManagementFunctionRequestParser);

        TaskManagementFunctionRequestParser parser = (TaskManagementFunctionRequestParser)recognizedParser;

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
     *             This exception should be never thrown.
     * @throws IOException
     *             This exception should be never thrown.
     */
    @Test(enabled = false)
    public void testSerialize1() throws InternetSCSIException, IOException, DigestException {

        super.setUp(TEST_CASE_1);

        ByteBuffer expectedResult = WiresharkMessageParser.parseToByteBuffer(TEST_CASE_1);
        assertTrue(expectedResult.equals(protocolDataUnit.serialize()));
    }
}
