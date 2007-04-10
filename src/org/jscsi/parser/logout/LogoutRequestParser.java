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
 * $Id: LogoutRequestParser.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.logout;

import java.util.HashMap;
import java.util.Map;

import org.jscsi.parser.Constants;
import org.jscsi.parser.InitiatorMessageParser;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.utils.Utils;

/**
 * <h1>LogoutRequestParser</h1>
 * <p>
 * This class parses a Logout Request message defined in the iSCSI Standard
 * (RFC3720).
 * <p>
 * The Logout Request is used to perform a controlled closing of a connection.
 * <p>
 * An initiator MAY use a Logout Request to remove a connection from a session
 * or to close an entire session.
 * <p>
 * After sending the Logout Request PDU, an initiator MUST NOT send any new
 * iSCSI requests on the closing connection. If the Logout Request is intended
 * to close the session, new iSCSI requests MUST NOT be sent on any of the
 * connections participating in the session.
 * <p>
 * When receiving a Logout Request with the reason code of "close the
 * connection" or "close the session", the target MUST terminate all pending
 * commands, whether acknowledged via ExpCmdSN or not, on that connection or
 * session respectively.
 * <p>
 * When receiving a Logout Request with the reason code "remove connection for
 * recovery", the target MUST discard all requests not yet acknowledged via
 * ExpCmdSN that were issued on the specified connection, and suspend all
 * data/status/R2T transfers on behalf of pending commands on the specified
 * connection.
 * <p>
 * The target then issues the Logout Response and half-closes the TCP connection
 * (sends FIN). After receiving the Logout Response and attempting to receive
 * the FIN (if still possible), the initiator MUST completely close the
 * logging-out connection. For the terminated commands, no additional responses
 * should be expected.
 * <p>
 * A Logout for a CID may be performed on a different transport connection when
 * the TCP connection for the CID has already been terminated. In such a case,
 * only a logical "closing" of the iSCSI connection for the CID is implied with
 * a Logout.
 * <p>
 * All commands that were not terminated or not completed (with status) and
 * acknowledged when the connection is closed completely can be reassigned to a
 * new connection if the target supports connection recovery.
 * <p>
 * If an initiator intends to start recovery for a failing connection, it MUST
 * use the Logout Request to "clean-up" the target end of a failing connection
 * and enable recovery to start, or the Login Request with a non-zero TSIH and
 * the same CID on a new connection for the same effect (see Section 10.14.3
 * CID). In sessions with a single connection, the connection can be closed and
 * then a new connection reopened. A connection reinstatement login can be used
 * for recovery (see Section 5.3.4 Connection Reinstatement).
 * <p>
 * A successful completion of a Logout Request with the reason code of "close
 * the connection" or "remove the connection for recovery" results at the target
 * in the discarding of unacknowledged commands received on the connection being
 * logged out. These are commands that have arrived on the connection being
 * logged out, but have not been delivered to SCSI because one or more commands
 * with a smaller CmdSN has not been received by iSCSI. See Section 3.2.2.1
 * Command Numbering and Acknowledging. The resulting holes the in command
 * sequence numbers will have to be handled by appropriate recovery (see Chapter
 * 6) unless the session is also closed.
 * <p>
 * The entire logout discussion in this section is also applicable for an
 * implicit Logout realized via a connection reinstatement or session
 * reinstatement. When a Login Request performs an implicit Logout, the implicit
 * Logout is performed as if having the reason codes specified below:
 * <p>
 * <table border="1">
 * <tr>
 * <th>Reason code</th>
 * <th>Type of implicit Logout</th>
 * </tr>
 * <tr>
 * <td>0</td>
 * <td>session reinstatement</td>
 * </tr>
 * <tr>
 * <td>1</td>
 * <td>connection reinstatement when the operational ErrorRecoveryLevel < 2</td>
 * </tr>
 * <tr>
 * <td>2</td>
 * <td>connection reinstatement when the operational ErrorRecoveryLevel = 2</td>
 * </tr>
 * </table>
 * <p>
 * <h4>TotalAHSLength and DataSegmentLength</h4>
 * For this PDU TotalAHSLength and DataSegmentLength MUST be <code>0</code>.
 * <p>
 * <h4>ExpStatSN</h4>
 * This is the last ExpStatSN value for the connection to be closed.
 * <p>
 * <h4>Implicit termination of tasks</h4>
 * A target implicitly terminates the active tasks due to the iSCSI protocol in
 * the following cases:
 * <p>
 * <ol type="a">
 * <li>When a connection is implicitly or explicitly logged out with the reason
 * code of "Close the connection" and there are active tasks allegiant to that
 * connection. </li>
 * <li>When a connection fails and eventually the connection state times out
 * (state transition M1 in Section 7.2.2 State Transition Descriptions for
 * Initiators and Targets) and there are active tasks allegiant to that
 * connection.</li>
 * <li>When a successful recovery Logout is performed while there are active
 * tasks allegiant to that connection, and those tasks eventually time out after
 * the Time2Wait and Time2Retain periods without allegiance reassignment.</li>
 * <li>When a connection is implicitly or explicitly logged out with the reason
 * code of "Close the session" and there are active tasks in that session.</li>
 * </ol>
 * If the tasks terminated in any of the above cases are SCSI tasks, they must
 * be internally terminated as if with CHECK CONDITION status. This status is
 * only meaningful for appropriately handling the internal SCSI state and SCSI
 * side effects with respect to ordering because this status is never
 * communicated back as a terminating status to the initiator. However
 * additional actions may have to be taken at SCSI level depending on the SCSI
 * context as defined by the SCSI standards (e.g., queued commands and ACA, in
 * cases a), b), and c), after the tasks are terminated, the target MUST report
 * a Unit Attention condition on the next command processed on any connection
 * for each affected I_T_L nexus with the status of CHECK CONDITION, and the
 * ASC/ASCQ value of 47h/7Fh - "SOME COMMANDS CLEARED BY ISCSI PROTOCOL EVENT" -
 * etc. - see [SAM2] and [SPC3]).
 * 
 * <p>
 * 
 * @author Volker Wildi
 * 
 */
public final class LogoutRequestParser extends InitiatorMessageParser {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This enumeration defines all the Logout Reasons Codes, which are allowed in
   * a iSCSI Logout Request message (RFC3720). All other values are reserved.
   * <p>
   * All other values are reserved.
   * <p>
   */
  public static enum LogoutReasonCode {

    /**
     * Close the session. All commands associated with the session (if any) are
     * terminated.
     */
    CLOSE_SESSION((byte) 0),

    /**
     * Close the connection. All commands associated with connection (if any)
     * are terminated.
     */
    CLOSE_CONNECTION((byte) 1),

    /**
     * Remove the connection for recovery. Connection is closed and all commands
     * associated with it, if any, are to be prepared for a new allegiance.
     */
    CONNECTION_RECOVERY((byte) 2);

    private final byte value;

    private static Map<Byte, LogoutReasonCode> mapping;

    private LogoutReasonCode(final byte newValue) {

      if (LogoutReasonCode.mapping == null) {
        LogoutReasonCode.mapping = new HashMap<Byte, LogoutReasonCode>();
      }

      LogoutReasonCode.mapping.put(newValue, this);
      value = newValue;
    }

    /**
     * Returns the value of this enumeration.
     * 
     * @return The value of this enumeration.
     */
    public final byte value() {

      return value;
    }

    /**
     * Returns the constant defined for the given <code>value</code>.
     * 
     * @param value
     *          The value to search for.
     * @return The constant defined for the given <code>value</code>. Or
     *         <code>null</code>, if this value is not defined by this
     *         enumeration.
     */
    public static final LogoutReasonCode valueOf(final byte value) {

      return LogoutReasonCode.mapping.get(value);
    }

  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** Reason code indicates the reason for the logout. */
  private LogoutReasonCode reasonCode;

  /** The Connection ID. */
  private short connectionID;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Default constructor, creates a new, empty <code>LogoutRequestParser</code>
   * object.
   * 
   * @param initProtocolDataUnit
   *          The reference <code>ProtocolDataUnit</code> instance, which
   *          contains this <code>LogoutRequestParser</code> subclass object.
   */
  public LogoutRequestParser(final ProtocolDataUnit initProtocolDataUnit) {

    super(initProtocolDataUnit);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  public final String toString() {

    final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

    Utils.printField(sb, "Reson Code", reasonCode.value(), 1);
    Utils.printField(sb, "CID", connectionID, 1);
    sb.append(super.toString());

    return sb.toString();
  }

  /** {@inheritDoc} */
  @Override
  public final DataSegmentFormat getDataSegmentFormat() {

    return DataSegmentFormat.BINARY;
  }

  /** {@inheritDoc} */
  @Override
  public final void clear() {

    super.clear();

    reasonCode = LogoutReasonCode.CLOSE_SESSION;
    connectionID = 0x0000;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This is the <em>Connection ID</em> of the connection to be closed
   * (including closing the TCP stream). This field is only valid if the reason
   * code is not "close the session".
   * 
   * @return The <em>Connection ID</em> of this
   *         <code>LogoutRequestParser</code> object.
   */
  public final short getConnectionID() {

    return connectionID;
  }

  /**
   * Returns the reason for the logout.
   * 
   * @return The reason code for this logout request.
   * @see LogoutReasonCode
   */
  public final LogoutReasonCode getReasonCode() {

    return reasonCode;
  }

  /**
   * Sets the new <code>Connection ID</code> of this
   * <code>LogoutRequestParser</code> object.
   * 
   * @param newCID
   *          The new <code>Connection ID</code>.
   */
  public final void setConnectionID(final short newCID) {

    connectionID = newCID;
  }

  /**
   * Sets the <code>Reason Code</code> of this
   * <code>LogoutRequestParser</code> object.
   * 
   * @param newReasonCode
   *          The new <code> Reason Code</code>.
   */
  public final void setReasonCode(final LogoutReasonCode newReasonCode) {

    reasonCode = newReasonCode;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  protected final void deserializeBytes1to3(final int line)
      throws InternetSCSIException {

    reasonCode = LogoutReasonCode
        .valueOf((byte) ((line & Constants.SECOND_BYTE_MASK) >>> Constants.TWO_BYTES_SHIFT));
    Utils.isReserved(line & Constants.LAST_TWO_BYTES_MASK);
  }

  /** {@inheritDoc} */
  @Override
  protected final void deserializeBytes20to23(final int line)
      throws InternetSCSIException {

    connectionID = (short) ((line & Constants.FIRST_TWO_BYTES_MASK) >>> Constants.TWO_BYTES_SHIFT);
    Utils.isReserved(line & Constants.LAST_TWO_BYTES_MASK);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  protected final void checkIntegrity() throws InternetSCSIException {

    String exceptionMessage;
    do {
      Utils.isReserved(logicalUnitNumber);

      if (reasonCode == LogoutReasonCode.CLOSE_SESSION && connectionID != 0) {
        exceptionMessage = "The CID field must be zero, if close session is requested.";
        break;
      }

      if (protocolDataUnit.getBasicHeaderSegment().getTotalAHSLength() != 0) {
        exceptionMessage = "TotalAHSLength must be 0!";
        break;
      }

      if (protocolDataUnit.getBasicHeaderSegment().getDataSegmentLength() != 0) {
        exceptionMessage = "DataSegmentLength must be 0!";
        break;
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

    return reasonCode.value() << Constants.TWO_BYTES_SHIFT;
  }

  /** {@inheritDoc} */
  @Override
  protected final int serializeBytes20to23() {

    return connectionID << Constants.TWO_BYTES_SHIFT;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
