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
package org.jscsi.parser;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.datasegment.DataSegmentFactory;
import org.jscsi.parser.datasegment.IDataSegment;
import org.jscsi.parser.datasegment.SettingsMap;
import org.jscsi.utils.WiresharkMessageParser;
import org.testng.annotations.AfterMethod;

/**
 * Base class of all parsers tests.
 * 
 * @author Volker Wildi, University of Konstanz
 */
public abstract class ProtocolDataUnitTest {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The used Protocol Data Unit for this test. */
    protected ProtocolDataUnit protocolDataUnit;

    /** The used parser for this Protocol Data Unit. */
    protected AbstractMessageParser recognizedParser;

    /** Size in bytes of an chunk of this object. */
    protected int chunkSize;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Default constructor to create a new, empty ProtocolDataUnitTest object. */
    protected ProtocolDataUnitTest() {

        protocolDataUnit = new ProtocolDataUnitFactory().create("None", "None");
        chunkSize = 8192;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * @param immediateFlag
     *            The expected value of the immediate flag.
     * @param finalFlag
     *            The expected value of the final flag.
     * @param opCode
     *            The expected value of the operation code.
     * @param totalAHSLength
     *            The expected value of the total AHS length.
     * @param dataSegmentLength
     *            The expected value of the data segment length.
     * @param initiatorTaskTag
     *            The expected value of the initiator task tag.
     * @throws IOException
     *             This exception should be never thrown.
     * @throws InternetSCSIException
     *             This exception should be never thrown.
     */
    protected void testDeserialize(final boolean immediateFlag, final boolean finalFlag,
        final OperationCode opCode, final int totalAHSLength, final int dataSegmentLength,
        final int initiatorTaskTag) throws IOException, InternetSCSIException {

        testBasicHeaderSegment(immediateFlag, finalFlag, opCode, totalAHSLength, dataSegmentLength,
            initiatorTaskTag);
    }

    /**
     * Tests a given <code>SettingsMap</code> object against the parsed <code>IDataSegment</code> object
     * contained in <code>protocolDataUnit</code> .
     * 
     * @param expectedKeyValuePair
     *            <code>SettingsMap</code> instance, which contains all
     *            parameters.
     */
    protected void testDataSegment(final SettingsMap expectedKeyValuePair) {

        testDataSegment(expectedKeyValuePair.asByteBuffer());
    }

    /**
     * Tests the given Ethereal log against the parsed <code>IDataSegment</code> object contained in
     * <code>protocolDataUnit</code>.
     * 
     * @param etherealLog
     *            A String, which contains an Ethereal log.
     */
    protected final void testDataSegment(final String etherealLog) {

        testDataSegment(WiresharkMessageParser.parseToByteBuffer(etherealLog));
    }

    /**
     * Tests the given Ethereal log against the parsed <code>IDataSegment</code> object contained in
     * <code>protocolDataUnit</code>.
     * 
     * @param etherealLog
     *            A <code>ByteBuffer</code>, which contains an Ethereal log.
     */
    protected final void testDataSegment(final ByteBuffer etherealLog) {

        IDataSegment dataSegment =
            DataSegmentFactory.create(etherealLog, recognizedParser.getDataSegmentFormat(), chunkSize);

        testDataSegment(dataSegment);
    }

    /**
     * Tests the <code>IDataSegment</code> instance to the parsed data segment
     * of the <code>protocolDataUnit</code> instance.
     * 
     * @param expectedDataSegment
     *            Uses this data segment for comparsion.
     * 
     */
    protected final void testDataSegment(final IDataSegment expectedDataSegment) {

        IDataSegment testDataSegment =
            DataSegmentFactory.create(protocolDataUnit.getDataSegment(), recognizedParser
                .getDataSegmentFormat(), chunkSize);
        assertTrue(expectedDataSegment.equals(testDataSegment));
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This methods should be invoked to read in the byte representation with
     * the given file content.
     * 
     * @param fileContent
     *            The content of the test case to read from.
     * @throws InternetSCSIException
     *             This exception should be never thrown.
     * @throws IOException
     *             This exception should be never thrown.
     * @throws DigestException
     *             This exception should be never thrown.
     */
    protected void setUp(final String fileContent) throws InternetSCSIException, IOException, DigestException {

        if (protocolDataUnit != null) {
            protocolDataUnit.deserialize(WiresharkMessageParser.parseToByteBuffer(fileContent));
        }
    }

    /**
     * This method resets the settings of this ProtocolDataUnit object.
     */
    @AfterMethod
    public void tearDown() {

        protocolDataUnit.clear();
        chunkSize = 8192;
        recognizedParser = null;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Tests the fields, which are common in all parsers.
     * 
     * @param immediateFlag
     *            The expected value of the immediate flag.
     * @param finalFlag
     *            The expected value of the final flag.
     * @param opCode
     *            The expected value of the operation code.
     * @param totalAHSLength
     *            The expected value of the total AHS length.
     * @param dataSegmentLength
     *            The expected value of the data segment length.
     * @param initiatorTaskTag
     *            The expected value of the initiator task tag.
     */
    protected void testBasicHeaderSegment(final boolean immediateFlag, final boolean finalFlag,
        final OperationCode opCode, final int totalAHSLength, final int dataSegmentLength,
        final int initiatorTaskTag) {

        BasicHeaderSegment bhs = protocolDataUnit.getBasicHeaderSegment();

        // test BHS fields
        assertEquals(immediateFlag, bhs.isImmediateFlag());
        assertEquals(finalFlag, bhs.isFinalFlag());
        assertEquals(opCode, bhs.getOpCode());
        assertEquals((byte)totalAHSLength, bhs.getTotalAHSLength());
        assertEquals(dataSegmentLength, bhs.getDataSegmentLength());
        assertEquals(initiatorTaskTag, bhs.getInitiatorTaskTag());

        recognizedParser = bhs.getParser();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
