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
 * $Id: AbstractMessageParser.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.parser;

import java.nio.ByteBuffer;

import org.jscsi.core.utils.Utils;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.exception.InternetSCSIException;

/**
 * <h1>AbstractMessageParser</h1>
 * <p>
 * Abstract class from which each parser (initiator or target message parser)
 * for a specific Protocol Data Unit (PDU) is inherited. The version of iSCSI
 * Protocol is the RFC3720.
 * 
 * @author Volker Wildi
 */
public abstract class AbstractMessageParser {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** Bit mask to extract the first operation code specific field. */
  private static final int FIRST_SPECIFIC_FIELD_MASK = 0x007FFFFF;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * The <b>read-only</b> reference to the <code>ProtocolDataUnit</code>
   * instance, which contains this <code>AbstractMessageParser</code> type.
   */
  protected final ProtocolDataUnit protocolDataUnit;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Some opcodes operate on a specific Logical Unit. The Logical Unit Number
   * (LUN) field identifies which Logical Unit. If the opcode does not relate to
   * a Logical Unit, this field is either ignored or may be used in an opcode
   * specific way. The LUN field is 64-bits and should be formatted in
   * accordance with [SAM2]. For example, LUN[0] from [SAM2] is BHS byte
   * <code>8</code> and so on up to LUN[7] from [SAM2], which is BHS byte
   * <code>15</code>.
   */
  protected long logicalUnitNumber;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Default Contructor to create a new, empty AbstractMessageParser object.
   * 
   * @param initProtocolDataUnit
   *          The reference <code>ProtocolDataUnit</code> instance, which
   *          contains this <code>AbstractMessageParser</code> object.
   */
  public AbstractMessageParser(final ProtocolDataUnit initProtocolDataUnit) {

    protocolDataUnit = initProtocolDataUnit;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This method defines the order of the parsing process of the operation code
   * specific fields and check their integtity.
   * 
   * @param pdu
   *          Array which contains the total Protocol Data Unit.
   * @throws InternetSCSIException
   *           If any violation of the iSCSI-Standard emerge.
   */
  final void deserializeBasicHeaderSegment(final ByteBuffer pdu)
      throws InternetSCSIException {

    deserializeBytes1to3(pdu.getInt() & FIRST_SPECIFIC_FIELD_MASK);
    pdu.position(BasicHeaderSegment.BYTES_8_11);
    deserializeBytes8to11(pdu.getInt());
    deserializeBytes12to15(pdu.getInt());

    pdu.position(BasicHeaderSegment.BYTES_20_23);
    deserializeBytes20to23(pdu.getInt());
    deserializeBytes24to27(pdu.getInt());
    deserializeBytes28to31(pdu.getInt());
    deserializeBytes32to35(pdu.getInt());
    deserializeBytes36to39(pdu.getInt());
    deserializeBytes40to43(pdu.getInt());
    deserializeBytes44to47(pdu.getInt());
  }

  /**
   * This method serializes the whole BHS to its byte representation.
   * 
   * @param dst
   *          The destination <code>ByteBuffer</code> to write to.
   * @param offset
   *          The start offset in <code>dst</code>.
   * @throws InternetSCSIException
   *           If any violation of the iSCSI-Standard emerge.
   */
  final void serializeBasicHeaderSegment(final ByteBuffer dst, final int offset)
      throws InternetSCSIException {

    dst.position(offset);
    dst.putInt(offset, dst.getInt() | serializeBytes1to3());

    dst.position(offset + BasicHeaderSegment.BYTES_8_11);
    dst.putInt(serializeBytes8to11());
    dst.putInt(serializeBytes12to15());

    dst.position(offset + BasicHeaderSegment.BYTES_20_23);
    dst.putInt(serializeBytes20to23());
    dst.putInt(serializeBytes24to27());
    dst.putInt(serializeBytes28to31());
    dst.putInt(serializeBytes32to35());
    dst.putInt(serializeBytes36to39());
    dst.putInt(serializeBytes40to43());
    dst.putInt(serializeBytes44to47());
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * If this method returns <code>true</code>, hen it indicates that the data
   * segment data is interpreted as binary data. Else the data segment data must
   * be interpreted as Text Format.
   * 
   * @return Returns a format defined by the DataSegmentFormat enumeration.
   * @see DataSegmentFormat
   */
  public abstract DataSegmentFormat getDataSegmentFormat();

  /**
   * If this method returns <code>true</code>, it indicates that this derived
   * AbstractMessageParser can contain one or more Additional Header Segments.
   * 
   * @return Returns <code>true</code>, if this AbstractMessageParser object
   *         can contain one or more AHSs.
   * @see AdditionalHeaderSegment
   */
  public boolean canContainAdditionalHeaderSegments() {

    return false;
  }

  /**
   * If this method returns <code>true</code>, then it indicates that this
   * derived <code>AbstractMessageParser</code> instance can be protected by a
   * digest.
   * 
   * @return <code>true</code>, if the ProtocolDataUnit can be protected by a
   *         header and/or a data digest. Else <code>false</code>.
   */
  public boolean canHaveDigests() {

    return true;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Returns the short version of the used sequence numbers of this parser
   * instance.
   * 
   * @return The string with all needed sequence numbers.
   */
  public abstract String getShortInfo();

  /**
   * This method concatenate all the fields of a derived parser to allow an easy
   * generation of debug informations.
   * 
   * @return The debug formatted string.
   */
  public String toString() {

    final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);
    sb.append("ParserType: ");
    sb.append(AbstractMessageParser.class);

    return sb.toString();
  }

  /**
   * This method sets all settings to their initial values.
   */
  public void clear() {

    logicalUnitNumber = 0x0000000000000000L;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This <code>AbstractMessageParser</code> instance affects the
   * incrementation of a <code>Sequence Number</code> counter.
   * 
   * @return <code>true</code>, if the counter has to be incremented.
   */
  public abstract boolean incrementSequenceNumber();

  /**
   * Returns the Logical Unit Number (LUN) of this
   * <code>AbstractMessageParser</code> object.
   * 
   * @return The Logical Unit Number of this object.
   */
  public final long getLogicalUnitNumber() {

    return logicalUnitNumber;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Parse the bytes <code>1</code> till <code>3</code> in the Basic Header
   * Segment.
   * 
   * @param line
   *          The actual line
   * @throws InternetSCSIException
   *           If any violation of the iSCSI-Standard emerge.
   */
  protected abstract void deserializeBytes1to3(final int line)
      throws InternetSCSIException;

  /**
   * Parse the bytes <code>8</code> till <code>11</code> in the Basic Header
   * Segment.
   * 
   * @param line
   *          The actual line
   * @throws InternetSCSIException
   *           If any violation of the iSCSI-Standard emerge.
   */
  protected void deserializeBytes8to11(final int line)
      throws InternetSCSIException {

    logicalUnitNumber = Utils.getUnsignedLong(line);
    logicalUnitNumber <<= Constants.FOUR_BYTES_SHIFT;
  }

  /**
   * Parse the bytes <code>12</code> till <code>15</code> in the Basic
   * Header Segment.
   * 
   * @param line
   *          The actual line
   * @throws InternetSCSIException
   *           If any violation of the iSCSI-Standard emerge.
   */
  protected void deserializeBytes12to15(final int line)
      throws InternetSCSIException {

    logicalUnitNumber |= Utils.getUnsignedLong(line);
  }

  /**
   * Parse the bytes <code>20</code> till <code>23</code> in the Basic
   * Header Segment.
   * 
   * @param line
   *          The actual line
   * @throws InternetSCSIException
   *           If any violation of the iSCSI-Standard emerge.
   */
  protected abstract void deserializeBytes20to23(final int line)
      throws InternetSCSIException;

  /**
   * Parse the bytes <code>24</code> till <code>27</code> in the Basic
   * Header Segment.
   * 
   * @param line
   *          The actual line
   * @throws InternetSCSIException
   *           If any violation of the iSCSI-Standard emerge.
   */
  protected abstract void deserializeBytes24to27(final int line)
      throws InternetSCSIException;

  /**
   * Parse the bytes <code>28</code> till <code>31</code> in the Basic
   * Header Segment.
   * 
   * @param line
   *          The actual line
   * @throws InternetSCSIException
   *           If any violation of the iSCSI-Standard emerge.
   */
  protected abstract void deserializeBytes28to31(final int line)
      throws InternetSCSIException;

  /**
   * Parse the bytes <code>32</code> till <code>35</code> in the Basic
   * Header Segment.
   * 
   * @param line
   *          The actual line
   * @throws InternetSCSIException
   *           If any violation of the iSCSI-Standard emerge.
   */
  protected abstract void deserializeBytes32to35(final int line)
      throws InternetSCSIException;

  /**
   * Parse the bytes <code>36</code> till <code>39</code> in the Basic
   * Header Segment.
   * 
   * @param line
   *          The actual line
   * @throws InternetSCSIException
   *           If any violation of the iSCSI-Standard emerge.
   */
  protected abstract void deserializeBytes36to39(final int line)
      throws InternetSCSIException;

  /**
   * Parse the bytes <code>40</code> till <code>43</code> in the Basic
   * Header Segment.
   * 
   * @param line
   *          The actual line
   * @throws InternetSCSIException
   *           If any violation of the iSCSI-Standard emerge.
   */
  protected abstract void deserializeBytes40to43(final int line)
      throws InternetSCSIException;

  /**
   * Parse the bytes <code>44</code> till <code>47</code> in the Basic
   * Header Segment.
   * 
   * @param line
   *          The actual line
   * @throws InternetSCSIException
   *           If any violation of the iSCSI-Standard emerge.
   */
  protected abstract void deserializeBytes44to47(final int line)
      throws InternetSCSIException;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Serializes the bytes <code>1</code> till <code>3</code> in the Basic
   * Header Segment.
   * 
   * @return The serialized byte representation.
   */
  protected abstract int serializeBytes1to3();

  /**
   * Serializes the bytes <code>8</code> till <code>11</code> in the Basic
   * Header Segment.
   * 
   * @return The serialized byte representation.
   * @throws InternetSCSIException
   *           if any violation of the iSCSI Standard (RFC3720) occurs.
   */
  protected int serializeBytes8to11() throws InternetSCSIException {

    return (int) (logicalUnitNumber >>> Constants.FOUR_BYTES_SHIFT);
  }

  /**
   * Serializes the bytes <code>12</code> till <code>15</code> in the Basic
   * Header Segment.
   * 
   * @return The serialized byte representation.
   */
  protected int serializeBytes12to15() {

    return (int) (logicalUnitNumber & Constants.LAST_FOUR_BYTES_MASK);
  }

  /**
   * Serializes the bytes <code>20</code> till <code>23</code> in the Basic
   * Header Segment.
   * 
   * @return The serialized byte representation.
   */
  protected abstract int serializeBytes20to23();

  /**
   * Serializes the bytes <code>24</code> till <code>27</code> in the Basic
   * Header Segment.
   * 
   * @return The serialized byte representation.
   */
  protected abstract int serializeBytes24to27();

  /**
   * Serializes the bytes <code>28</code> till <code>31</code> in the Basic
   * Header Segment.
   * 
   * @return The serialized byte representation.
   */
  protected abstract int serializeBytes28to31();

  /**
   * Serializes the bytes <code>32</code> till <code>35</code> in the Basic
   * Header Segment.
   * 
   * @return The serialized byte representation.
   */
  protected abstract int serializeBytes32to35();

  /**
   * Serializes the bytes <code>36</code> till <code>39</code> in the Basic
   * Header Segment.
   * 
   * @return The serialized byte representation.
   */
  protected abstract int serializeBytes36to39();

  /**
   * Serializes the bytes <code>40</code> till <code>43</code> in the Basic
   * Header Segment.
   * 
   * @return The serialized byte representation.
   */
  protected abstract int serializeBytes40to43();

  /**
   * Serializes the bytes <code>44</code> till <code>47</code> in the Basic
   * Header Segment.
   * 
   * @return The serialized byte representation.
   */
  protected abstract int serializeBytes44to47();

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This method checks, if all parsed fields are valid. Because there are
   * several fields, which are reserved for future versions, and these must be
   * zero. Is this the case an exception will be thrown.
   * 
   * @throws InternetSCSIException
   *           If the integrity is violated.
   */
  protected abstract void checkIntegrity() throws InternetSCSIException;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
