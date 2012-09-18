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
 * Testing the correctness of the TaskManagementFunctionResponseParser.
 * 
 * @author Volker Wildi
 */
public class TaskManagementFunctionResponseTest extends ProtocolDataUnitTest {

    private static final String TEST_CASE_1 = "e2 d3 3d c7 ef 54 95 6b 3d f7 c7 d7 ea 5d 7f ae "
        + "ed 3b c6 c6 e4 05 ec 1c bd ff c5 ec 5b 70 0a e7 "
        + "2f d6 f1 1a 4d be d8 ec 5b 2f be bc 4c 99 dc 80";

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
        super
            .testDeserialize(false, true, OperationCode.SCSI_TM_RESPONSE, 0x00000000, 0x00000000, 0xECF407ED);

        assertTrue(recognizedParser instanceof TaskManagementFunctionResponseParser);

        TaskManagementFunctionResponseParser parser = (TaskManagementFunctionResponseParser)recognizedParser;

        assertEquals(0, parser.getResponse());
        assertEquals(0xA6C84985, parser.getStatusSequenceNumber());
        assertEquals(0xB86232D6, parser.getExpectedCommandSequenceNumber());
        assertEquals(0x60126FF5, parser.getMaximumCommandSequenceNumber());
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
