package org.jscsi.scsi.tasks;

import org.jscsi.scsi.lu.LogicalUnit;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.tasks.management.TaskManagementFunction;
import org.jscsi.scsi.tasks.management.TaskServiceResponse;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;

/**
 * Within a SCSI Target, the Task Router sits between the Target Ports and Logical Units.
 * The Task Router has two primary responsibilities.
 * <p>
 * First, the Task Router must maintain a list of Logical Units and enqueue incoming commands
 * to the proper Logical Unit as specified by the I_T_L_x Nexus on a given command. If an invalid
 * Logical Unit is specified, the router must process the command as specified in SAM-2 or SAM-3.
 * <p>
 * Second, for I_T Nexus commands ("non-LU commands") the router must process the command as
 * specified in SPC-2, SPC-3, or other relevant standard.
 * <p>
 * Router implementations must be capable of dynamic registration and removal of Logical Units.
 * When removed, no further commands sent to a particular Logical Unit shall be forwarded to that
 * LU.
 * <p>
 * The Task Router is not responsible for processing data transport services. Commands which
 * require incoming data must be presented to the router with a byte buffer containing all
 * expected data. Logical Units enqueue return data directly to the originating Target Transport
 * Port.
 */
public interface TaskRouter
{
   
   /**
    * Register a Logical Unit with the given LUN.
    * 
    * @param number The Logical Unit Number.
    * @param lu The Logical Unit.
    * @throws Exception If the LUN is already assigned.
    */
   void registerLogicalUnit( long lun, LogicalUnit lu ) throws Exception;
   
   /**
    * Remove a Logical Unit from the task router. After removal no further commands will be
    * sent to the LU.
    * 
    * @param number The LUN.
    * @returns The Logical Unit.
    * @throws Exception If the LUN is not valid.
    */
   LogicalUnit removeLogicalUnit( long lun ) throws Exception;
   
   
   /**
    * Used by the Target Transport Port to enqueue incoming commands to the task router. The
    * router is then responsible for forwarding those commands to Logical Units or returning
    * an error condition if the appropriate LU cannot be found.
    * 
    * @param port The transport port where the command originated. This is forwarded to the LU
    *    which then returns data directly to the transport port.
    * @param command The incoming command.
    * @param output Any incoming data; <code>null</code> if the command did not require an incoming
    *    data transfer.
    */
   void enqueue( TargetTransportPort port, Command command );
   
   /**
    * Executes a task management function.
    * 
    * @param nexus Where the indicated function will be executed.
    * @param function The task management function to execute.
    * @return The result of the task management function.
    */
   TaskServiceResponse execute(Nexus nexus, TaskManagementFunction function);
   
   
   /**
    * Used by the Target Transport Port to indicate an I_T nexus loss event.
    */
   void nexusLost();
   
}
