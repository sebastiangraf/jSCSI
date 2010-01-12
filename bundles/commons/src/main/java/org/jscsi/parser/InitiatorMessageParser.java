/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * InitiatorMessageParser.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.parser;

import org.jscsi.core.utils.Utils;
import org.jscsi.parser.exception.InternetSCSIException;

/**
 * <h1>InitiatorMessageParser</h1>
 * <p>
 * This abstract class is the base class of all initiator message parsers
 * defined in the iSCSI Protocol (RFC3720). This class defines some methods,
 * which are common in all parsers to simplify the parsing process.
 * 
 * @author Volker Wildi
 */
public abstract class InitiatorMessageParser extends AbstractMessageParser {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Command Sequence Number.
   * <p>
   * Enables ordered delivery across multiple connections in a single session.
   */
  protected int commandSequenceNumber;

  /**
   * Expected Status Sequence Number.
   * <p>
   * Command responses up to
   * <code>expectedStatusSequenceNumber - 1 (mod 2**32)</code> have been
   * received (acknowledges status) on the connection.
   */
  protected int expectedStatusSequenceNumber;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Default constructor, creates a new, empty LogoutRequestParser object.
   * 
   * @param initProtocolDataUnit
   *          The reference <code>ProtocolDataUnit</code> instance, which
   *          contains this <code>InitiatorMessageParser</code> object.
   */
  public InitiatorMessageParser(final ProtocolDataUnit initProtocolDataUnit) {

    super(initProtocolDataUnit);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  public String getShortInfo() {

    return "-> " + getClass().getSimpleName() + ": cmdSN: "
        + getCommandSequenceNumber() + ", expStatSN: "
        + getExpectedStatusSequenceNumber();
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {

    final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

    Utils.printField(sb, "CommandSequenceNumber", commandSequenceNumber, 1);
    Utils.printField(sb, "ExpectedStatusSequenceNumber",
        expectedStatusSequenceNumber, 1);

    return sb.toString();
  }

  /** {@inheritDoc} */
  @Override
  public void clear() {

    super.clear();

    commandSequenceNumber = 0;
    expectedStatusSequenceNumber = 0;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This <code>AbstractMessageParser</code> instance affects the incrementation
   * of the <code>Command Sequence Number</code>.
   * 
   * @return <code>true</code>, if the counter has to be incremented. Else
   *         <code>false</code>.
   */
  @Override
  public boolean incrementSequenceNumber() {

    return !protocolDataUnit.getBasicHeaderSegment().isImmediateFlag();
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Returns the Command Sequence Number of this
   * <code>InitiatorMessageParser</code> object.
   * 
   * @return The Command Sequence Number.
   */
  public final int getCommandSequenceNumber() {

    return commandSequenceNumber;
  }

  /**
   * Returns the Expected Status Sequence Number of this
   * <code>InitiatorMessageParser</code> object.
   * 
   * @return The Expected Status Sequence Number.
   */
  public final int getExpectedStatusSequenceNumber() {

    return expectedStatusSequenceNumber;
  }

  /**
   * Sets the Command Sequence Number of this
   * <code>InitiatorMessageParser</code> object to the given value.
   * 
   * @param initCmdSN
   *          The new Command Sequence Number.
   */
  public void setCommandSequenceNumber(final int initCmdSN) {

    commandSequenceNumber = initCmdSN;
  }

  /**
   * Sets the Expected Status Sequence Number of this
   * <code>InitiatorMessageParser</code> object to the given value.
   * 
   * @param initExpStatSN
   *          The new Expected Status Sequence Number.
   */
  public final void setExpectedStatusSequenceNumber(final int initExpStatSN) {

    expectedStatusSequenceNumber = initExpStatSN;
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

    commandSequenceNumber = line;
  }

  /** {@inheritDoc} */
  @Override
  protected final void deserializeBytes28to31(final int line)
      throws InternetSCSIException {

    expectedStatusSequenceNumber = line;
  }

  /** {@inheritDoc} */
  @Override
  protected void deserializeBytes32to35(final int line)
      throws InternetSCSIException {

    Utils.isReserved(line);
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

    return commandSequenceNumber;
  }

  /** {@inheritDoc} */
  @Override
  protected int serializeBytes28to31() {

    return expectedStatusSequenceNumber;
  }

  /** {@inheritDoc} */
  @Override
  protected int serializeBytes32to35() {

    return Constants.RESERVED_INT;
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

}
