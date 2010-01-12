/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * ProtocolDataUnitTest.java 2500 2007-03-05 13:29:08Z lemke $
 */

package org.jscsi.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;

import org.jscsi.core.utils.WiresharkMessageParser;
import org.jscsi.parser.datasegment.DataSegmentFactory;
import org.jscsi.parser.datasegment.IDataSegment;
import org.jscsi.parser.datasegment.SettingsMap;
import org.jscsi.parser.exception.InternetSCSIException;
import org.junit.After;

/**
 * Base class of all parsers tests.
 * 
 * @author Volker Wildi
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
   *          The expected value of the immediate flag.
   * @param finalFlag
   *          The expected value of the final flag.
   * @param opCode
   *          The expected value of the operation code.
   * @param totalAHSLength
   *          The expected value of the total AHS length.
   * @param dataSegmentLength
   *          The expected value of the data segment length.
   * @param initiatorTaskTag
   *          The expected value of the initiator task tag.
   * @throws IOException
   *           This exception should be never thrown.
   * @throws InternetSCSIException
   *           This exception should be never thrown.
   */
  protected void testDeserialize(final boolean immediateFlag,
      final boolean finalFlag, final OperationCode opCode,
      final int totalAHSLength, final int dataSegmentLength,
      final int initiatorTaskTag) throws IOException, InternetSCSIException {

    testBasicHeaderSegment(immediateFlag, finalFlag, opCode, totalAHSLength,
        dataSegmentLength, initiatorTaskTag);
  }

  /**
   * Tests a given <code>SettingsMap</code> object against the parsed
   * <code>IDataSegment</code> object contained in <code>protocolDataUnit</code>
   * .
   * 
   * @param expectedKeyValuePair
   *          <code>SettingsMap</code> instance, which contains all parameters.
   */
  protected void testDataSegment(final SettingsMap expectedKeyValuePair) {

    testDataSegment(expectedKeyValuePair.asByteBuffer());
  }

  /**
   * Tests the given Ethereal log against the parsed <code>IDataSegment</code>
   * object contained in <code>protocolDataUnit</code>.
   * 
   * @param etherealLog
   *          A String, which contains an Ethereal log.
   */
  protected final void testDataSegment(final String etherealLog) {

    testDataSegment(WiresharkMessageParser.parseToByteBuffer(etherealLog));
  }

  /**
   * Tests the given Ethereal log against the parsed <code>IDataSegment</code>
   * object contained in <code>protocolDataUnit</code>.
   * 
   * @param etherealLog
   *          A <code>ByteBuffer</code>, which contains an Ethereal log.
   */
  protected final void testDataSegment(final ByteBuffer etherealLog) {

    IDataSegment dataSegment = DataSegmentFactory.create(etherealLog,
        recognizedParser.getDataSegmentFormat(), chunkSize);

    testDataSegment(dataSegment);
  }

  /**
   * Tests the <code>IDataSegment</code> instance to the parsed data segment of
   * the <code>protocolDataUnit</code> instance.
   * 
   * @param expectedDataSegment
   *          Uses this data segment for comparsion.
   * @param etherealLog
   */
  protected final void testDataSegment(final IDataSegment expectedDataSegment) {

    IDataSegment testDataSegment = DataSegmentFactory.create(protocolDataUnit
        .getDataSegment(), recognizedParser.getDataSegmentFormat(), chunkSize);
    assertTrue(expectedDataSegment.equals(testDataSegment));
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This methods should be invoked to read in the byte representation with the
   * given file content.
   * 
   * @param fileContent
   *          The content of the test case to read from.
   * @throws InternetSCSIException
   *           This exception should be never thrown.
   * @throws IOException
   *           This exception should be never thrown.
   * @throws DigestException
   *           This exception should be never thrown.
   */
  protected void setUp(final String fileContent) throws InternetSCSIException,
      IOException, DigestException {

    if (protocolDataUnit != null) {
      protocolDataUnit.deserialize(WiresharkMessageParser
          .parseToByteBuffer(fileContent));
    }
  }

  /**
   * This method resets the settings of this ProtocolDataUnit object.
   */
  @After
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
   *          The expected value of the immediate flag.
   * @param finalFlag
   *          The expected value of the final flag.
   * @param opCode
   *          The expected value of the operation code.
   * @param totalAHSLength
   *          The expected value of the total AHS length.
   * @param dataSegmentLength
   *          The expected value of the data segment length.
   * @param initiatorTaskTag
   *          The expected value of the initiator task tag.
   */
  protected void testBasicHeaderSegment(final boolean immediateFlag,
      final boolean finalFlag, final OperationCode opCode,
      final int totalAHSLength, final int dataSegmentLength,
      final int initiatorTaskTag) {

    BasicHeaderSegment bhs = protocolDataUnit.getBasicHeaderSegment();

    // test BHS fields
    assertEquals(immediateFlag, bhs.isImmediateFlag());
    assertEquals(finalFlag, bhs.isFinalFlag());
    assertEquals(opCode, bhs.getOpCode());
    assertEquals((byte) totalAHSLength, bhs.getTotalAHSLength());
    assertEquals(dataSegmentLength, bhs.getDataSegmentLength());
    assertEquals(initiatorTaskTag, bhs.getInitiatorTaskTag());

    recognizedParser = bhs.getParser();
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
