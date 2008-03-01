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
 * $Id: TargetMessageParser.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.parser;

import org.jscsi.core.utils.Utils;
import org.jscsi.parser.exception.InternetSCSIException;

/**
 * <h1>TargetMessageParser</h1>
 * <p>
 * This abstract class is the base class of all target message parsers defined
 * in the iSCSI Protocol (RFC3720). This class defines some methods, which are
 * common in all parsers to simplify the parsing process.
 * 
 * @author Volker Wildi
 * 
 */
public abstract class TargetMessageParser extends AbstractMessageParser {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** Status Sequence Number. */
  protected int statusSequenceNumber;

  /** Next Expected Commamd Sequence Number. */
  protected int expectedCommandSequenceNumber;

  /** Maximum Command Sequence Number. */
  protected int maximumCommandSequenceNumber;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Default constructor, creates a new, empty <code>TargetMessageParser</code>
   * object.
   * 
   * @param initProtocolDataUnit
   *          The reference <code>ProtocolDataUnit</code> instance, which
   *          contains this <code>TargetMessageParser</code> subclass object.
   */
  public TargetMessageParser(final ProtocolDataUnit initProtocolDataUnit) {

    super(initProtocolDataUnit);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  public String getShortInfo() {

    return "<- " + getClass().getSimpleName() + ": statSN: "
        + getStatusSequenceNumber() + ", expCmdSN: "
        + getExpectedCommandSequenceNumber() + ", maxCmdSN: "
        + getMaximumCommandSequenceNumber();
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {

    final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

    Utils.printField(sb, "StatusSequenceNumber", statusSequenceNumber, 1);
    Utils.printField(sb, "ExpectedCommandSequenceNumber",
        expectedCommandSequenceNumber, 1);
    Utils.printField(sb, "MaximumCommandSequenceNumber",
        maximumCommandSequenceNumber, 1);

    return sb.toString();
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {

    super.clear();

    statusSequenceNumber = 0;
    expectedCommandSequenceNumber = 0;
    maximumCommandSequenceNumber = 0;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This <code>AbstractMessageParser</code> instance affects the
   * incrementation of the <code>Expected Status Sequence Number</code>.
   * 
   * @return <code>true</code>, if the counter has to be incremented. Else
   *         <code>false</code>.
   */
  @Override
  public boolean incrementSequenceNumber() {

    return true;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Next Expected Commamd Sequence Number from this Initiator
   * <code>ExpCmdSN</code> is a Sequence Number that the target iSCSI returns
   * to the initiator to acknowledge command reception. It is used to update a
   * local variable with the same name. An <code>ExpCmdSN</code> equal to
   * <code>MaxCmdSN+1</code> indicates that the target cannot accept new
   * commands.
   * 
   * @return Expected Command Sequence Number.
   */
  public final int getExpectedCommandSequenceNumber() {

    return expectedCommandSequenceNumber;
  }

  /**
   * Maximum Command Sequence Number from this Initiator <code>MaxCmdSN</code>
   * is a Sequence Number that the target iSCSI returns to the initiator to
   * indicate the maximum <code>CmdSN</code> the initiator can send. It is
   * used to update a local variable with the same name. If
   * <code>MaxCmdSN</code> is equal to <code>ExpCmdSN-1</code>, this
   * indicates to the initiator that the target cannot receive any additional
   * commands. When <code>MaxCmdSN</code> changes at the target while the
   * target has no pending PDUs to convey this information to the initiator, it
   * MUST generate a NOP-IN to carry the new <code>MaxCmdSN</code>.
   * 
   * @return Maximum Command Sequence Number.
   */
  public final int getMaximumCommandSequenceNumber() {

    return maximumCommandSequenceNumber;
  }

  /**
   * The Status Sequence Number (StatSN) is a Sequence Number that the target
   * iSCSI layer generates per connection and that in turn, enables the
   * initiator to acknowledge status reception. <code>StatSN</code> is
   * incremented by <code>1</code> for every response/status sent on a
   * connection except for responses sent as a result of a retry or SNACK. In
   * the case of responses sent due to a retransmission request, the
   * <code>StatSN</code> MUST be the same as the first time the PDU was sent
   * unless the connection has since been restarted.
   * 
   * @return Status Sequence Number.
   */
  public final int getStatusSequenceNumber() {

    return statusSequenceNumber;
  }

  /**
   * Sets the Expected Command Sequence Number of this
   * <code>TargetMessageParser</code> object to the given value.
   * 
   * @param newExpectedCommandSequenceNumber
   *          The new Expected Command Sequence Number.
   */
  public final void setExpectedCommandSequenceNumber(
      final int newExpectedCommandSequenceNumber) {

    expectedCommandSequenceNumber = newExpectedCommandSequenceNumber;
  }

  /**
   * Sets the Maximum Command Sequence Number of this
   * <code>TargetMessageParser</code> object to the given value.
   * 
   * @param newMaximumCommandSequenceNumber
   *          The new Maximum Command Sequence Number.
   */
  public final void setMaximumCommandSequenceNumber(
      final int newMaximumCommandSequenceNumber) {

    maximumCommandSequenceNumber = newMaximumCommandSequenceNumber;
  }

  /**
   * Sets the Status Sequence Number of this <code>TargetMessageParser</code>
   * object to the given value.
   * 
   * @param newStatusSequenceNumber
   *          The new Status Sequence Number.
   */
  public final void setStatusSequenceNumber(final int newStatusSequenceNumber) {

    statusSequenceNumber = newStatusSequenceNumber;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  protected void deserializeBytes1to3(final int line)
      throws InternetSCSIException {

    Utils.isReserved(line);
  }

  /** {@inheritDoc} */
  @Override
  protected void deserializeBytes20to23(final int line)
      throws InternetSCSIException {

    Utils.isReserved(line);
  }

  /** {@inheritDoc} */
  @Override
  protected final void deserializeBytes24to27(final int line)
      throws InternetSCSIException {

    statusSequenceNumber = line;
  }

  /** {@inheritDoc} */
  @Override
  protected final void deserializeBytes28to31(final int line)
      throws InternetSCSIException {

    expectedCommandSequenceNumber = line;
  }

  /** {@inheritDoc} */
  @Override
  protected final void deserializeBytes32to35(final int line)
      throws InternetSCSIException {

    maximumCommandSequenceNumber = line;
  }

  /** {@inheritDoc} */
  @Override
  protected void deserializeBytes36to39(final int line)
      throws InternetSCSIException {

    Utils.isReserved(line);
  }

  /** {@inheritDoc} */
  @Override
  protected void deserializeBytes40to43(final int line)
      throws InternetSCSIException {

    Utils.isReserved(line);
  }

  /** {@inheritDoc} */
  @Override
  protected void deserializeBytes44to47(final int line)
      throws InternetSCSIException {

    Utils.isReserved(line);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  protected int serializeBytes1to3() {

    return Constants.RESERVED_INT;
  }

  /** {@inheritDoc} */
  @Override
  protected int serializeBytes20to23() {

    return Constants.RESERVED_INT;
  }

  /** {@inheritDoc} */
  @Override
  protected int serializeBytes24to27() {

    return statusSequenceNumber;
  }

  /** {@inheritDoc} */
  @Override
  protected int serializeBytes28to31() {

    return expectedCommandSequenceNumber;
  }

  /** {@inheritDoc} */
  @Override
  protected int serializeBytes32to35() {

    return maximumCommandSequenceNumber;
  }

  /** {@inheritDoc} */
  @Override
  protected int serializeBytes36to39() {

    return Constants.RESERVED_INT;
  }

  /** {@inheritDoc} */
  @Override
  protected int serializeBytes40to43() {

    return Constants.RESERVED_INT;
  }

  /** {@inheritDoc} */
  @Override
  protected int serializeBytes44to47() {

    return Constants.RESERVED_INT;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
