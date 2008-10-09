/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * OperationCode.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>OperationCode</h1>
 * <p>
 * This enumeration defines all valid operation codes, which are conform to
 * iSCSI Protocol (RFC 3720).
 * <p>
 * <table border="1">
 * <tr>
 * <td>0x00</td>
 * <td>NOP-Out</td>
 * </tr>
 * <tr>
 * <td>0x01</td>
 * <td>SCSI Command (encapsulates a SCSI Command Descriptor Block)</td>
 * </tr>
 * <tr>
 * <td>0x02</td>
 * <td>SCSI Task Management function request</td>
 * </tr>
 * <tr>
 * <td>0x03</td>
 * <td>Login Request</td>
 * </tr>
 * <tr>
 * <td>0x04</td>
 * <td>Text Request</td>
 * </tr>
 * <tr>
 * <td>0x05</td>
 * <td>SCSI Data-Out (for WRITE operations)</td>
 * </tr>
 * <tr>
 * <td>0x06</td>
 * <td>Logout Request</td>
 * </tr>
 * <tr>
 * <td>0x10</td>
 * <td>SNACK Request</td>
 * </tr>
 * <tr>
 * <td>0x1c-0x1e</td>
 * <td>Vendor specific codes</td>
 * </tr>
 * <tr>
 * <td>0x20</td>
 * <td>NOP-In</td>
 * </tr>
 * <tr>
 * <td>0x21</td>
 * <td>SCSI Response - contains SCSI status and possibly sense information or
 * other response information.</td>
 * </tr>
 * <tr>
 * <td>0x22</td>
 * <td>SCSI Task Management function response</td>
 * </tr>
 * <tr>
 * <td>0x23</td>
 * <td>Login Response</td>
 * </tr>
 * <tr>
 * <td>0x24</td>
 * <td>Text Response</td>
 * </tr>
 * <tr>
 * <td>0x25</td>
 * <td>SCSI Data-In - for READ operations.</td>
 * </tr>
 * <tr>
 * <td>0x26</td>
 * <td>Logout Response</td>
 * </tr>
 * <tr>
 * <td>0x31</td>
 * <td>Ready To Transfer (R2T) - sent by target when it is ready to receive
 * data.</td>
 * </tr>
 * <tr>
 * <td>0x32</td>
 * <td>Asynchronous Message - sent by target to indicate certain special
 * conditions.</td>
 * </tr>
 * <tr>
 * <td>0x3c-0x3e</td>
 * <td>Vendor specific codes</td>
 * </tr>
 * <tr>
 * <td>0x3f</td>
 * <td>Reject</td>
 * </tr>
 * </table>
 * <p>
 * Not Supported: 0x1c-0x1e Vendor specific codes
 * 
 * @author Volker Wildi
 */
public enum OperationCode {

  // --------------------------------------------------------------------------
  // The initiator operation codes
  // --------------------------------------------------------------------------

  /**
   * This request/response pair may be used by an initiator and target as a
   * "ping" mechanism to verify that a connection/session is still active and
   * all of its components are operational. Such a ping may be triggered by the
   * initiator or target. The triggering party indicates that it wants a reply
   * by setting a value different from the default <code>0xffffffff</code> in
   * the corresponding Initiator/Target Transfer Tag. NOP-In/NOP-Out may also be
   * used "unidirectional" to convey to the initiator/target command, status or
   * data counter values when there is no other "carrier" and there is a need to
   * update the initiator/ target.
   */
  NOP_OUT((byte) 0x00),

  /**
   * This request carries the SCSI CDB and all the other SCSI execute command
   * procedure call (see [SAM2]) IN arguments such as task attributes, Expected
   * Data Transfer Length for one or both transfer directions (the latter for
   * bidirectional commands), and Task Tag (as part of the I_T_L_x nexus). The
   * I_T_L nexus is derived by the initiator and target from the LUN field in
   * the request and the I_T nexus is implicit in the session identification. In
   * addition, the SCSI-command PDU carries information required for the proper
   * operation of the iSCSI protocol - the command sequence number (
   * <code>CmdSN</code>) for the session and the expected status number (
   * <code>ExpStatSN</code>) for the connection. All or part of the SCSI output
   * (write) data associated with the SCSI command may be sent as part of the
   * SCSI-Command PDU as a data segment.
   */
  SCSI_COMMAND((byte) 0x01),

  /**
   * The Task Management function request provides an initiator with a way to
   * explicitly control the execution of one or more SCSI Tasks or iSCSI
   * functions. The PDU carries a function identifier (which task management
   * function to perform) and enough information to unequivocally identify the
   * task or task-set on which to perform the action, even if the task(s) to act
   * upon has not yet arrived or has been discarded due to an error. The
   * referenced tag identifies an individual task if the function refers to an
   * individual task. The I_T_L nexus identifies task sets. In iSCSI the I_T_L
   * nexus is identified by the LUN and the session identification (the session
   * identifies an I_T nexus). For task sets, the <code>CmdSN</code> of the Task
   * Management function request helps identify the tasks upon which to act,
   * namely all tasks associated with a LUN and having a CmdSN preceding the
   * Task Management function request <code>CmdSN</code>. For a Task Management
   * function, the coordination between responses to the tasks affected and the
   * Task Management function response is done by the target.
   */
  SCSI_TM_REQUEST((byte) 0x02),

  /**
   * Login Requests and Responses are used exclusively during the Login Phase of
   * each connection to set up the session and connection parameters. (The Login
   * Phase consists of a sequence of login requests and responses carrying the
   * same Initiator Task Tag.) A connection is identified by an arbitrarily
   * selected connection-ID (<code>CID</code>) that is unique within a session.
   * Similar to the Text Requests and Responses, Login Requests/Responses carry
   * key=value text information with a simple syntax in the data segment. The
   * Login Phase proceeds through several stages (security negotiation,
   * operational parameter negotiation) that are selected with two binary coded
   * fields in the header -- the "current stage" (<code>CSG</code>) and the
   * "next stage" (<code>NSG</code> ) with the appearance of the latter being
   * signaled by the "transit" flag (<code>T</code>). The first Login Phase of a
   * session plays a special role, called the leading login, which determines
   * some header fields (e.g., the version number, the maximum number of
   * connections, and the session identification). The CmdSN initial value is
   * also set by the leading login. StatSN for each connection is initiated by
   * the connection login. A login request may indicate an implied logout
   * (cleanup) of the connection to be logged in (a connection restart) by using
   * the same Connection ID (CID) as an existing connection, as well as the same
   * session identifying elements of the session to which the old connection was
   * associated.
   */
  LOGIN_REQUEST((byte) 0x03),

  /**
   * Text requests and responses are designed as a parameter negotiation vehicle
   * and as a vehicle for future extension. In the data segment, Text
   * Requests/Responses carry text information using a simple "key=value"
   * syntax. Text Request/Responses may form extended sequences using the same
   * Initiator Task Tag. The initiator uses the F (Final) flag bit in the text
   * request header to indicate its readiness to terminate a sequence. The
   * target uses the F (Final) flag bit in the text response header to indicate
   * its consent to sequence termination. Text Request and Responses also use
   * the Target Transfer Tag to indicate continuation of an operation or a new
   * beginning. A target that wishes to continue an operation will set the
   * Target Transfer Tag in a Text Response to a value different from the
   * default 0xffffffff. An initiator willing to continue will copy this value
   * into the Target Transfer Tag of the next Text Request. If the initiator
   * wants to restart the current target negotiation (start fresh) will set the
   * Target Transfer Tag to 0xffffffff. Although a complete exchange is always
   * started by the initiator, specific parameter negotiations may be initiated
   * by the initiator or target.
   */
  TEXT_REQUEST((byte) 0x04),

  /**
   * SCSI Data-Out and SCSI Data-In are the main vehicles by which SCSI data
   * payload is carried between initiator and target. Data payload is associated
   * with a specific SCSI command through the Initiator Task Tag. For target
   * convenience, outgoing solicited data also carries a Target Transfer Tag
   * (copied from R2T) and the LUN. Each PDU contains the payload length and the
   * data offset relative to the buffer address contained in the SCSI execute
   * command procedure call. In each direction, the data transfer is split into
   * "sequences". An end-of-sequence is indicated by the F bit. An outgoing
   * sequence is either unsolicited (only the first sequence can be unsolicited)
   * or consists of all the Data-Out PDUs sent in response to an R2T. Input
   * sequences are built to enable the direction switching for bidirectional
   * commands. For input, the target may request positive acknowledgement of
   * input data. This is limited to sessions that support error recovery and is
   * implemented through the A bit in the SCSI Data-In PDU header. Data-In and
   * Data-Out PDUs also carry the DataSN to enable the initiator and target to
   * detect missing PDUs (discarded due to an error). In addition, StatSN is
   * carried by the Data-In PDUs. To enable a SCSI command to be processed while
   * involving a minimum number of messages, the last SCSI Data-In PDU passed
   * for a command may also contain the status if the status indicates
   * termination with no exceptions (no sense or response involved).
   */
  SCSI_DATA_OUT((byte) 0x05),

  /**
   * Logout Requests and Responses are used for the orderly closing of
   * connections for recovery or maintenance. The logout request may be issued
   * following a target prompt (through an asynchronous message) or at an
   * initiators initiative. When issued on the connection to be logged out, no
   * other request may follow it.
   */
  LOGOUT_REQUEST((byte) 0x06),

  /**
   * With the SNACK Request, the initiator requests retransmission of
   * numbered-responses or data from the target. A single SNACK request covers a
   * contiguous set of missing items, called a run, of a given type of items.
   * The type is indicated in a type field in the PDU header. The run is
   * composed of an initial item (StatSN, DataSN, R2TSN) and the number of
   * missed Status, Data, or R2T PDUs. For long Data-In sequences, the target
   * may request (at predefined minimum intervals) a positive acknowledgement
   * for the data sent. A SNACK request with a type field that indicates ACK and
   * the number of Data-In PDUs acknowledged conveys this positive
   * acknowledgement.
   */
  SNACK_REQUEST((byte) 0x10),

  // --------------------------------------------------------------------------
  // The target operation codes
  // --------------------------------------------------------------------------

  /**
   * This request/response pair may be used by an initiator and target as a
   * "ping" mechanism to verify that a connection/session is still active and
   * all of its components are operational. Such a ping may be triggered by the
   * initiator or target. The triggering party indicates that it wants a reply
   * by setting a value different from the default 0xffffffff in the
   * corresponding Initiator/Target Transfer Tag. NOP-In/NOP-Out may also be
   * used "unidirectional" to convey to the initiator/target command, status or
   * data counter values when there is no other "carrier" and there is a need to
   * update the initiator/ target.
   */
  NOP_IN((byte) 0x20),

  /**
   * The SCSI-Response carries all the SCSI execute-command procedure call (see
   * [SAM2]) OUT arguments and the SCSI execute-command procedure call return
   * value. The SCSI-Response contains the residual counts from the operation,
   * if any, an indication of whether the counts represent an overflow or an
   * underflow, and the SCSI status if the status is valid or a response code (a
   * non-zero return value for the execute-command procedure call) if the status
   * is not valid. For a valid status that indicates that the command has been
   * processed, but resulted in an exception (e.g., a SCSI CHECK CONDITION), the
   * PDU data segment contains the associated sense data. The use of Autosense
   * ([SAM2]) is REQUIRED by iSCSI. Some data segment content may also be
   * associated (in the data segment) with a non-zero response code. In
   * addition, the SCSI-Response PDU carries information required for the proper
   * operation of the iSCSI protocol:
   * <ul>
   * <li>The number of Data-In PDUs that a target has sent (to enable the
   * initiator to check that all have arrived).</li> <li>StatSN - the Status
   * Sequence Number on this connection</li> <li> ExpCmdSN - the next Expected
   * Command Sequence Number at the target. </li> <li>MaxCmdSN - the maximum
   * CmdSN acceptable at the target from this initiator.</li>
   * </ul>
   */
  SCSI_RESPONSE((byte) 0x21),

  /**
   * The Task Management function response carries an indication of function
   * completion for a Task Management function request including how it was
   * completed (response and qualifier) and additional information for failure
   * responses. After the Task Management response indicates Task Management
   * function completion, the initiator will not receive any additional
   * responses from the affected tasks.
   */
  SCSI_TM_RESPONSE((byte) 0x22),

  /**
   * Login Requests and Responses are used exclusively during the Login Phase of
   * each connection to set up the session and connection parameters. (The Login
   * Phase consists of a sequence of login requests and responses carrying the
   * same Initiator Task Tag.) A connection is identified by an arbitrarily
   * selected connection-ID (<code>CID</code>) that is unique within a session.
   * Similar to the Text Requests and Responses, Login Requests/Responses carry
   * key=value text information with a simple syntax in the data segment. The
   * Login Phase proceeds through several stages (security negotiation,
   * operational parameter negotiation) that are selected with two binary coded
   * fields in the header -- the "current stage" (<code>CSG</code>) and the
   * "next stage" (<code>NSG</code> ) with the appearance of the latter being
   * signaled by the "transit" flag (<code>T</code>). The first Login Phase of a
   * session plays a special role, called the leading login, which determines
   * some header fields (e.g., the version number, the maximum number of
   * connections, and the session identification). The CmdSN initial value is
   * also set by the leading login. StatSN for each connection is initiated by
   * the connection login. A login request may indicate an implied logout
   * (cleanup) of the connection to be logged in (a connection restart) by using
   * the same Connection ID (<code>CID</code>) as an existing connection, as
   * well as the same session identifying elements of the session to which the
   * old connection was associated.
   */
  LOGIN_RESPONSE((byte) 0x23),

  /**
   * Text requests and responses are designed as a parameter negotiation vehicle
   * and as a vehicle for future extension. In the data segment, Text
   * Requests/Responses carry text information using a simple "key=value"
   * syntax. Text Request/Responses may form extended sequences using the same
   * Initiator Task Tag. The initiator uses the <code>F</code> (Final) flag bit
   * in the text request header to indicate its readiness to terminate a
   * sequence. The target uses the <code>F</code> (Final) flag bit in the text
   * response header to indicate its consent to sequence termination. Text
   * Request and Responses also use the Target Transfer Tag to indicate
   * continuation of an operation or a new beginning. A target that wishes to
   * continue an operation will set the Target Transfer Tag in a Text Response
   * to a value different from the default <code>0xffffffff</code>. An initiator
   * willing to continue will copy this value into the Target Transfer Tag of
   * the next Text Request. If the initiator wants to restart the current target
   * negotiation (start fresh) will set the Target Transfer Tag to
   * <code>0xffffffff</code>. Although a complete exchange is always started by
   * the initiator, specific parameter negotiations may be initiated by the
   * initiator or target.
   */
  TEXT_RESPONSE((byte) 0x24),

  /**
   * SCSI Data-Out and SCSI Data-In are the main vehicles by which SCSI data
   * payload is carried between initiator and target. Data payload is associated
   * with a specific SCSI command through the Initiator Task Tag. For target
   * convenience, outgoing solicited data also carries a Target Transfer Tag
   * (copied from R2T) and the LUN. Each PDU contains the payload length and the
   * data offset relative to the buffer address contained in the SCSI execute
   * command procedure call. In each direction, the data transfer is split into
   * "sequences". An end-of-sequence is indicated by the F bit. An outgoing
   * sequence is either unsolicited (only the first sequence can be unsolicited)
   * or consists of all the Data-Out PDUs sent in response to an R2T. Input
   * sequences are built to enable the direction switching for bidirectional
   * commands. For input, the target may request positive acknowledgement of
   * input data. This is limited to sessions that support error recovery and is
   * implemented through the A bit in the SCSI Data-In PDU header. Data-In and
   * Data-Out PDUs also carry the DataSN to enable the initiator and target to
   * detect missing PDUs (discarded due to an error). In addition, StatSN is
   * carried by the Data-In PDUs. To enable a SCSI command to be processed while
   * involving a minimum number of messages, the last SCSI Data-In PDU passed
   * for a command may also contain the status if the status indicates
   * termination with no exceptions (no sense or response involved).
   */
  SCSI_DATA_IN((byte) 0x25),

  /**
   * Logout Requests and Responses are used for the orderly closing of
   * connections for recovery or maintenance. The Logout Response indicates that
   * the connection or session cleanup is completed and no other responses will
   * arrive on the connection (if received on the logging out connection). In
   * addition, the Logout Response indicates how long the target will continue
   * to hold resources for recovery (e.g., command execution that continues on a
   * new connection) in the text key Time2Retain and how long the initiator must
   * wait before proceeding with recovery in the text key Time2Wait.
   */
  LOGOUT_RESPONSE((byte) 0x26),

  /**
   * R2T is the mechanism by which the SCSI target "requests" the initiator for
   * output data. R2T specifies to the initiator the offset of the requested
   * data relative to the buffer address from the execute command procedure call
   * and the length of the solicited data. To help the SCSI target associate the
   * resulting Data-Out with an R2T, the R2T carries a Target Transfer Tag that
   * will be copied by the initiator in the solicited SCSI Data-Out PDUs. There
   * are no protocol specific requirements with regard to the value of these
   * tags, but it is assumed that together with the LUN, they will enable the
   * target to associate data with an R2T. R2T also carries information required
   * for proper operation of the iSCSI protocol, such as:
   * <ul>
   * <li>R2TSN (to enable an initiator to detect a missing R2T)</li> <li> StatSN
   * </li> <li>ExpCmdSN</li> <li>MaxCmdSN </li>
   * </ul>
   */
  R2T((byte) 0x31),

  /**
   * Asynchronous Messages are used to carry SCSI asynchronous events (AEN) and
   * iSCSI asynchronous messages. When carrying an AEN, the event details are
   * reported as sense data in the data segment.
   */
  ASYNC_MESSAGE((byte) 0x32),

  /**
   * Reject enables the target to report an iSCSI error condition (e.g.,
   * protocol, unsupported option) that uses a Reason field in the PDU header
   * and includes the complete header of the bad PDU in the Reject PDU data
   * segment.
   */
  REJECT((byte) 0x3F);

  private final byte value;

  private static Map<Byte, OperationCode> mapping;

  static {
    OperationCode.mapping = new HashMap<Byte, OperationCode>();
    for (OperationCode s : values()) {
      OperationCode.mapping.put(s.value, s);
    }
  }

  private OperationCode(final byte newValue) {

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
  public static final OperationCode valueOf(final byte value) {

    return OperationCode.mapping.get(value);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
