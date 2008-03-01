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
 * $Id: LogoutResponseParser.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.logout;

import org.jscsi.core.utils.Utils;
import org.jscsi.parser.Constants;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.TargetMessageParser;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.exception.InternetSCSIException;

/**
 * <h1>LogoutResponseParser</h1>
 * <p>
 * This class parses a Logout Response message defined in the iSCSI Standard
 * (RFC3720).
 * <p>
 * The Logout Response is used by the target to indicate if the cleanup
 * operation for the connection(s) has completed.
 * <p>
 * After Logout, the TCP connection referred by the CID MUST be closed at both
 * ends (or all connections must be closed if the logout reason was session
 * close).
 * <p>
 * <h4>TotalAHSLength and DataSegmentLength</h4>
 * For this PDU TotalAHSLength and DataSegmentLength MUST be <code>0</code>.
 * <p>
 * 
 * @author Volker Wildi
 */
public final class LogoutResponseParser extends TargetMessageParser {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The logout response code. */
  private LogoutResponse response;

  /** Time in seconds to wait for task reassignment. */
  private short time2Wait;

  /** Time in seconds to wait maximal for allegiance reassignment. */
  private short time2Retain;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Default constructor, creates a new, empty <code>LogoutResponseParser</code>
   * object.
   * 
   * @param initProtocolDataUnit
   *          The reference <code>ProtocolDataUnit</code> instance, which
   *          contains this <code>LogoutResponseParser</code> subclass object.
   */
  public LogoutResponseParser(final ProtocolDataUnit initProtocolDataUnit) {

    super(initProtocolDataUnit);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  public final String toString() {

    final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

    Utils.printField(sb, "Response", response.value(), 1);
    sb.append(super.toString());
    Utils.printField(sb, "Time2Wait", time2Wait, 1);
    Utils.printField(sb, "Time2Retain", time2Retain, 1);

    return sb.toString();
  }

  /** {@inheritDoc} */
  @Override
  public final DataSegmentFormat getDataSegmentFormat() {

    return DataSegmentFormat.NONE;
  }

  /** {@inheritDoc} */
  @Override
  public final void clear() {

    super.clear();

    response = null;
    time2Wait = 0;
    time2Retain = 0;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Returns the Logout Response of this <code>LogoutResponseParser</code>
   * object.
   * 
   * @return The Logout Response of this <code>LogoutResponseParser</code>
   *         object.
   * @see LogoutResponse
   */
  public final LogoutResponse getResponse() {

    return response;
  }

  /**
   * If the Logout response code is <code>0</code> and if the operational
   * ErrorRecoveryLevel is <code>2</code>, this is the maximum amount of
   * time, in seconds, after the initial wait (Time2Wait), the target waits for
   * the allegiance reassignment for any active task after which the task state
   * is discarded. If the Logout response code is <code>0</code> and if the
   * operational ErrorRecoveryLevel is less than <code>2</code>, this field
   * is to be ignored.
   * <p>
   * This field is invalid if the Logout response code is <code>1</code>.
   * <p>
   * If the Logout response code is <code>2</code> or <code>3</code>, this
   * field specifies the maximum amount of time, in seconds, after the initial
   * wait (Time2Wait), the target waits for a new implicit or explicit logout.
   * <p>
   * If it is the last connection of a session, the whole session state is
   * discarded after Time2Retain.
   * <p>
   * If <code>Time2Retain</code> is <code>0</code>, the target has already
   * discarded the connection (and possibly the session) state along with the
   * task states. No reassignment or Logout is required in this case.
   * 
   * @return The Time2Retain of this <code>LogoutResponseParser</code> object.
   */
  public final short getTime2Retain() {

    return time2Retain;
  }

  /**
   * If the Logout Response code is <code>0</code> and if the operational
   * ErrorRecoveryLevel is <code>2</code>, this is the minimum amount of
   * time, in seconds, to wait before attempting task reassignment. If the
   * Logout Response code is <code>0</code> and if the operational
   * ErrorRecoveryLevel is less than <code>2</code>, this field is to be
   * ignored.
   * <p>
   * This field is invalid if the Logout Response code is <code>1</code>.
   * <p>
   * If the Logout response code is <code>2</code> or <code>3</code>, this
   * field specifies the minimum time to wait before attempting a new implicit
   * or explicit logout.
   * <p>
   * If <code>Time2Wait</code> is <code>0</code>, the reassignment or a new
   * Logout may be attempted immediately.
   * 
   * @return The Time2Wait of this <code>LogoutResponseParser</code> object.
   */
  public final short getTime2Wait() {

    return time2Wait;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  protected final void deserializeBytes1to3(final int line)
      throws InternetSCSIException {

    Utils.isReserved(line & Constants.SECOND_BYTE_MASK);
    response = LogoutResponse
        .valueOf((byte) (line & Constants.THIRD_BYTE_MASK));
    Utils.isReserved(line & Constants.FOURTH_BYTE_MASK);
  }

  /** {@inheritDoc} */
  @Override
  protected final void deserializeBytes40to43(final int line)
      throws InternetSCSIException {

    time2Wait = (short) ((line & Constants.FIRST_TWO_BYTES_MASK) >>> Constants.TWO_BYTES_SHIFT);
    time2Retain = (short) (line & Constants.LAST_TWO_BYTES_MASK);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  protected final void checkIntegrity() throws InternetSCSIException {

    String exceptionMessage;

    do {

      Utils.isReserved(logicalUnitNumber);

      if (response == LogoutResponse.CID_NOT_FOUND) {
        if (time2Wait != 0 || time2Retain != 0) {
          exceptionMessage = ("Time fields are invalid with this response code.");
          break;
        }
      }

      // message is checked correctly
      return;
    } while (false);

    throw new InternetSCSIException(exceptionMessage);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  protected final int serializeBytes1to3() {

    return response.value() << Constants.ONE_BYTE_SHIFT;
  }

  /** {@inheritDoc} */
  @Override
  protected final int serializeBytes40to43() {

    int line = time2Retain;
    line |= time2Wait << Constants.TWO_BYTES_SHIFT;

    return line;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
