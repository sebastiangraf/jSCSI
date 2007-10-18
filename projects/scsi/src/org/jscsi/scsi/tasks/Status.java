
package org.jscsi.scsi.tasks;

import java.util.HashMap;
import java.util.Map;

/**
 * This enumerations defines all valid statuses, which are defined in the iSCSI
 * Standard (RFC 3720) and the SCSI Architecture Model 2 [SAM2].
 * <p>
 * <table border="1">
 * <tr>
 * <th>Status Code</th>
 * <th>Status</th>
 * <th>Task Ended</th>
 * <th>Service Response</th>
 * </tr>
 * 
 * <tr>
 * <td>00h</td>
 * <td>{@link #GOOD}</td>
 * <td>Yes</td>
 * <td>{@link org.jscsi.scsi.transport.ServiceResponse#TASK_COMPLETE}</td>
 * </tr>
 * <tr>
 * <td>02h</td>
 * <td>{@link #CHECK_CONDITION}</td>
 * <td>Yes</td>
 * <td>{@link org.jscsi.scsi.transport.ServiceResponse#TASK_COMPLETE}</td>
 * </tr>
 * <tr>
 * <td>04h</td>
 * <td>{@link #CONDITION_MET}</td>
 * <td>Yes</td>
 * <td>{@link org.jscsi.scsi.transport.ServiceResponse#TASK_COMPLETE}</td>
 * </tr>
 * <tr>
 * <td>08h</td>
 * <td>{@link #BUSY}</td>
 * <td>Yes</td>
 * <td>{@link org.jscsi.scsi.transport.ServiceResponse#TASK_COMPLETE}</td>
 * </tr>
 * <tr>
 * <td>10h</td>
 * <td>{@link #INTERMEDIATE}</td>
 * <td>No</td>
 * <td>{@link org.jscsi.scsi.transport.ServiceResponse#LINKED_COMMAND_COMPLETE}</td>
 * </tr>
 * <tr>
 * <td>14h</td>
 * <td>{@link #INTERMEDIATE_CONDITION_MET}</td>
 * <td>No</td>
 * <td>{@link org.jscsi.scsi.transport.ServiceResponse#LINKED_COMMAND_COMPLETE}</td>
 * </tr>
 * <tr>
 * <td>18h</td>
 * <td>{@link #RESERVATION_CONFLICT}</td>
 * <td>Yes</td>
 * <td>{@link org.jscsi.scsi.transport.ServiceResponse#TASK_COMPLETE}</td>
 * </tr>
 * <tr>
 * <td>22h</td>
 * <td colspan="3">Obsolete</td>
 * </tr>
 * <tr>
 * <td>28h</td>
 * <td>{@link #TASK_SET_FULL}</td>
 * <td>Yes</td>
 * <td>{@link org.jscsi.scsi.transport.ServiceResponse#TASK_COMPLETE}</td>
 * </tr>
 * <tr>
 * <td>30h</td>
 * <td>{@link #ACA_ACTIVE}</td>
 * <td>Yes</td>
 * <td>{@link org.jscsi.scsi.transport.ServiceResponse#TASK_COMPLETE}</td>
 * </tr>
 * <tr>
 * <td>40h</td>
 * <td>{@link #TASK_ABORTED}</td>
 * <td>Yes</td>
 * <td>{@link org.jscsi.scsi.transport.ServiceResponse#TASK_COMPLETE}</td>
 * </tr>
 * </table>
 * <p>
 * All other codes are reserved.
 * 
 * @author Volker Wildi
 */
public enum Status {
  /**
   * This status indicates that the device server has successfully completed the
   * task.
   */
  GOOD((byte) 0x00),
  /**
   * This status indicates that an CA or ACA condition has occurred (see 5.9.1).
   * Autosense data may be delivered (see 5.9.4.3)[SAM2].
   */
  CHECK_CONDITION((byte) 0x02),
  /**
   * This status shall be returned whenever the requested operation specified by
   * an unlinked command is satisfied (see the PRE-FETCH commands in the SBC
   * standard).
   */
  CONDITION_MET((byte) 0x04),
  /**
   * This status indicates that the logical unit is busy. This status shall be
   * returned whenever a logical unit is temporarily unable to accept a command.
   * The recommended application client recovery action is to issue the command
   * again at a later time. If the UA_INTLCK_CTRL field in the Control mode page
   * contains 11b (see SPC-3), termination of a command with BUSY status shall
   * cause an unit attention condition to be established for the SCSI initiator
   * port that sent the command with an additional sense code of PREVIOUS BUSY
   * STATUS unless a PREVIOUS BUSY STATUS unit attention condition already
   * exists.
   */
  BUSY((byte) 0x08),
  /**
   * This status or INTERMEDIATE-CONDITION MET shall be returned for each
   * successfully completed command in a series of linked commands (except the
   * last command), unless the command is termi- nated with CHECK CONDITION,
   * RESERVATION CONFLICT, TASK SET FULL, or BUSY status. If INTERME- DIATE or
   * INTERMEDIATE-CONDITION MET status is not returned, the series of linked
   * commands is terminated and the task is ended. This status is the equivalent
   * of GOOD status for linked commands.
   */
  INTERMEDIATE((byte) 0x10),
  /**
   * This status is returned whenever the requested operation specified by a
   * linked command is satisfied (see the PRE-FETCH commands in the SBC
   * standard), unless the command is termi- nated with CHECK CONDITION,
   * RESERVATION CONFLICT, TASK SET FULL, or BUSY status. If INTERME- DIATE or
   * INTERMEDIATE-CONDITION MET status is not returned, the series of linked
   * commands is terminated and the task is ended.
   */
  INTERMEDIATE_CONDITION_MET((byte) 0x14),
  /**
   * This status shall be returned whenever a SCSI initiator port attempts to
   * access a logical unit or an element of a logical unit in a way that
   * conflicts with an existing reservation. (See the RESERVE, RELEASE,
   * PERSISTENT RESERVE OUT and PERSISTENT RESERVE IN commands in SPC-2).
   * <p>
   * If the UA_INTLCK_CTRL field in the Control mode page contains 11b (see
   * SPC-3), termination of a command with RESERVATION CONFLICT status shall
   * cause an unit attention condition to be established for the SCSI initiator
   * port that sent the command with an additional sense code of PREVIOUS
   * RESERVATION CONFLICT STATUS unless a PREVIOUS RESERVATION CONFLICT STATUS
   * unit attention condition already exists.
   */
  RESERVATION_CONFLICT((byte) 0x18),
  /**
   * This status shall be implemented if the logical unit supports the creation
   * of tagged tasks (see 4.10)[SAM2]. This status shall not be implemented if
   * the logical unit does not support the creation of tagged tasks.
   * <p>
   * When the logical unit has at least one task in the task set for a SCSI
   * initiator port and a lack of task set resources prevents accepting a
   * received tagged task from that SCSI initiator port into the task set, TASK
   * SET FULL shall be returned. When the logical unit has no task in the task
   * set for a SCSI initiator port and a lack of task set resources prevents
   * accepting a received tagged task from that SCSI initiator port into the
   * task set, BUSY should be returned.
   * <p>
   * When the logical unit has at least one task in the task set and a lack of
   * task set resources prevents accepting a received untagged task into the
   * task set, BUSY should be returned.
   * <p>
   * The logical unit should allow at least one command in the task set for each
   * supported SCSI initiator port that has identified itself to the SCSI target
   * port by a SCSI transport protocol specific procedure or by the successful
   * transmission of a command.
   * <p>
   * If the UA_INTLCK_CTRL field in the Control mode page contains 11b (see
   * SPC-3), termination of a command with TASK SET FULL status shall cause an
   * unit attention condition to be established for the SCSI initiator port that
   * sent the command with an additional sense code of PREVIOUS TASK SET FULL
   * STATUS unless a PREVIOUS TASK SET FULL STATUS unit attention condition
   * already exists.
   */
  TASK_SET_FULL((byte) 0x28),
  /**
   * This status shall be returned when an ACA exists within a task set and a
   * SCSI initiator port issues a command for that task set when at least one of
   * the following is true:
   * <ol type="a">
   * <li>There is a task with the ACA attribute (see 7.5.4)[SAM2] in the task
   * set;</li>
   * <li>The SCSI initiator port issuing the command did not cause the ACA
   * condition; or</li>
   * <li>The task created to process the command did not have the ACA attribute
   * and the NACA bit was set to one in the CDB CONTROL byte of the faulting
   * command (see 5.9.1)[SAM2].</li>
   * </ol>
   * The SCSI initiator port may reissue the command after the ACA condition has
   * been cleared.
   */
  ACA_ACTIVE((byte) 0x30),
  /**
   * This status shall be returned when a task is aborted by another SCSI
   * initiator port and the Control mode page TAS bit is set to one (see
   * 5.7.3)[SAM2].
   */
  TASK_ABORTED((byte) 0x40);

  private final byte value;

  private static Map<Byte, Status> mapping;

  private Status(final byte newValue) {

    if (Status.mapping == null) {
       Status.mapping = new HashMap<Byte, Status>();
    }

    Status.mapping.put(newValue, this);
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
  public static final Status valueOf(final byte value) {

    return Status.mapping.get(value);
  }
}
