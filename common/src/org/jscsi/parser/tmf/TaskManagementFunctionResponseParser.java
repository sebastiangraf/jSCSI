/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * TaskManagementFunctionResponseParser.java 2500 2007-03-05 13:29:08Z lemke $
 */

package org.jscsi.parser.tmf;

import java.util.HashMap;
import java.util.Map;

import org.jscsi.core.utils.Utils;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.Constants;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.TargetMessageParser;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.exception.InternetSCSIException;

/**
 * This class parses a Task Management Function Response message defined in the
 * iSCSI Standard (RFC3720).
 * <p>
 * <h4>Task Management Actions on Task Sets</h4> The execution of ABORT TASK SET
 * and CLEAR TASK SET Task Management function requests consists of the
 * following sequence of events in the specified order on each of the entities.
 * <p>
 * The initiator:
 * <p>
 * <ol type="a">
 * <li>Issues ABORT TASK SET/CLEAR TASK SET request.</li>
 * <li>Continues to respond to each target transfer tag received for the
 * affected task set.</li>
 * <li>Receives any responses for the tasks in the affected task set (may
 * process them as usual because they are guaranteed to be valid).</li>
 * <li>Receives the task set management response, thus concluding all the tasks
 * in the affected task set.</li> </ol>
 * <p>
 * The target:
 * <p>
 * <ol type="a">
 * <li>Receives the ABORT TASK SET/CLEAR TASK SET request.</li>
 * <li>Waits for all target transfer tags to be responded to and for all
 * affected tasks in the task set to be received.</li>
 * <li>Propagates the command to and receives the response from the target SCSI
 * layer.</li>
 * <li>Takes note of last-sent StatSN on each of the connections in the iSCSI
 * sessions (one or more) sharing the affected task set, and waits for
 * acknowledgement of each StatSN (may solicit for acknowledgement by way of a
 * NOP-In). If some tasks originate from non-iSCSI I_T_L nexi then the means by
 * which the target insures that all affected tasks have returned their status
 * to the initiator are defined by the specific protocol.</li>
 * <li>Sends the task set management response to the issuing initiator.</li>
 * </ol>
 * <p>
 * <h4>TotalAHSLength and DataSegmentLength</h4> For this PDU TotalAHSLength and
 * DataSegmentLength MUST be <code>0</code>.
 * 
 * @author Volker Wildi
 */
public final class TaskManagementFunctionResponseParser extends
    TargetMessageParser {

  /**
   * This enumeration defines all valid response code, which are defined in the
   * iSCSI Standard (RFC 3720).
   * 
   * @author Volker Wildi
   */
  public static enum ResponseCode {

    /** Function complete. */
    FUNCTION_COMPLETE((byte) 0),
    /** Task does not exist. */
    TASK_DOES_NOT_EXIST((byte) 1),
    /** LUN does not exist. */
    LUN_DOES_NOT_EXIST((byte) 2),
    /** Task still allegiant. */
    TASK_STILL_ALLEGIANT((byte) 3),
    /** Task allegiance reassignment not supported. */
    TASK_ALLEGIANCE_REASSIGNMENT_NOT_SUPPORTED((byte) 4),
    /** Task management function not supported. */
    TASK_MANAGEMENT_FUNCTION_NOT_SUPPORTED((byte) 5),
    /** Function authorization failed. */
    FUNCTION_AUTHORIZATION_FAILED((byte) 6),
    /** Function rejected. */
    FUNCTION_REJECTED((byte) 255);

    private byte value;

    private static Map<Byte, ResponseCode> mapping;

    static {
      ResponseCode.mapping = new HashMap<Byte, ResponseCode>();
      for (ResponseCode s : values()) {
        ResponseCode.mapping.put(s.value, s);
      }
    }

    private ResponseCode(final byte newValue) {

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
    public static final ResponseCode valueOf(final byte value) {

      return ResponseCode.mapping.get(value);
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The response code. */
  private ResponseCode response;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Default constructor, creates a new, empty
   * <code>TaskManagementFunctionResponseParser</code> object.
   * 
   * @param initProtocolDataUnit
   *          The reference <code>ProtocolDataUnit</code> instance, which
   *          contains this <code>TaskManagementFunctionResponseParser</code>
   *          subclass object.
   */
  public TaskManagementFunctionResponseParser(
      final ProtocolDataUnit initProtocolDataUnit) {

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
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * The target provides a Response, which may take on the following values:
   * <br/>
   * <table border = "1">
   * <tr>
   * <th>Response Code</th> <th>Description</th>
   * </tr>
   * <tr>
   * <td>0</td> <td>Function complete. </td>
   * </tr>
   * <tr>
   * <td>1</td> <td>Task does not exist. </td>
   * </tr>
   * <tr>
   * <td>2</td> <td>LUN does not exist. </td>
   * </tr>
   * <tr>
   * <td>3</td> <td>Task still allegiant. </td>
   * </tr>
   * <tr>
   * <td>4</td> <td>Task allegiance reassignment not supported.</td>
   * </tr>
   * <tr>
   * <td>5</td> <td>Task management function not supported. </td>
   * </tr>
   * <tr>
   * <td>6</td> <td>Function authorization failed. </td>
   * </tr>
   * <tr>
   * <td>255</td> <td>Function rejected. </td>
   * </tr>
   * </table>
   * <br/><br/> All other values are reserved. <br/><br/> For a discussion on
   * usage of response codes <code>3</code> and <code>4</code>, see Section
   * 6.2.2 Allegiance Reassignment.<br/><br/> For the TARGET COLD RESET and
   * TARGET WARM RESET functions, the target cancels all pending operations
   * across all Logical Units known to the issuing initiator. For the TARGET
   * COLD RESET function, the target MUST then close all of its TCP connections
   * to all initiators (terminates all sessions).<br/><br/> The mapping of the
   * response code into a SCSI service response code value, if needed, is
   * outside the scope of this document. However, in symbolic terms Response
   * values 0 and 1 map to the SCSI service response of FUNCTION COMPLETE. All
   * other Response values map to the SCSI service response of FUNCTION
   * REJECTED. If a Task Management function response PDU does not arrive before
   * the session is terminated, the SCSI service response is SERVICE DELIVERY OR
   * TARGET FAILURE.<br/><br/> The response to ABORT TASK SET and CLEAR TASK SET
   * MUST only be issued by the target after all of the commands affected have
   * been received by the target, the corresponding task management functions
   * have been executed by the SCSI target, and the delivery of all responses
   * delivered until the task management function completion have been confirmed
   * (acknowledged through ExpStatSN) by the initiator on all connections of
   * this session. For the exact timeline of events, refer to Section 10.6.2
   * Task Management Actions on Task Sets.<br/><br/> For the ABORT TASK
   * function, <ol type="a"> <li>If the Referenced Task Tag identifies a valid
   * task leading to a successful termination, then targets must return the
   * "Function complete" response.</li> <li>If the Referenced Task Tag does not
   * identify an existing task, but if the CmdSN indicated by the RefCmdSN field
   * in the Task Management function request is within the valid CmdSN window
   * and less than the CmdSN of the Task Management function request itself,
   * then targets must consider the CmdSN received and return the
   * "Function complete" response.</li> <li>If the Referenced Task Tag does not
   * identify an existing task and if the CmdSN indicated by the RefCmdSN field
   * in the Task Management function request is outside the valid CmdSN window,
   * then targets must return the "Task does not exist" response.</li> </ol>
   * 
   * @return The response code of this
   *         <code>TaskManagementFunctionResponseParser</code> object.
   */
  public final ResponseCode getResponse() {

    return response;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  protected final void deserializeBytes1to3(final int line)
      throws InternetSCSIException {

    Utils.isReserved(line & Constants.SECOND_BYTE_MASK);
    response = ResponseCode.valueOf((byte) (line & Constants.THIRD_BYTE_MASK));
    Utils.isReserved(line & Constants.FOURTH_BYTE_MASK);
  }

  /** {@inheritDoc} */
  @Override
  protected final void deserializeBytes20to23(final int line)
      throws InternetSCSIException {

    Utils.isReserved(line);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  protected final void checkIntegrity() throws InternetSCSIException {

    String exceptionMessage;

    do {
      BasicHeaderSegment bhs = protocolDataUnit.getBasicHeaderSegment();
      if (bhs.getTotalAHSLength() != 0) {
        exceptionMessage = "TotalAHSLength must be 0!";
        break;
      }

      if (bhs.getDataSegmentLength() != 0) {
        exceptionMessage = "DataSegmentLength must be 0!";
        break;
      }

      Utils.isReserved(logicalUnitNumber);

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

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
