
package org.jscsi.scsi.lu;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.tasks.management.TaskManager;
import org.jscsi.scsi.transport.TargetTransportPort;

/**
 * A SCSI Logical Unit. LUs must be registered with a target to receive tasks. The LU must implement
 * a task manager of some form. Commands will be enqueued to the LU as they are received by the
 * Target Task Router.
 * <p>
 * Some LU implementations may exhibit the following "vendor specific" behavior not conforming to
 * SAM-2 or SAM-3: The LU may required activation for a specific I_T_L nexus before tasks will be
 * processed. These implementations must return a BUSY status until that I_T_L nexus is allowed.
 * This behavior should only be implemented for Logical Units which depend on iSCSI login data
 * before the unit is ready. If an LU does not require login data it must not exhibit this data; it
 * must rely on the iSCSI transport layer to provide authentication services.
 */
public interface LogicalUnit
{
   /**
    * Used by the Task Router to enqueue a command onto the Logical Unit task queue. The Logical
    * Unit then interprets these commands into Tasks.
    * 
    * @param port
    *           The transport port where the command originated. The Logical Unit will return data
    *           directly to the transport port.
    * @param command
    *           The incoming command.
    * @param output
    *           Any incoming data; <code>null</code> if the command did not require an incoming
    *           data transfer.
    */

   void enqueue(TargetTransportPort port, Command command);

   void start();

   void stop();
   
   void nexusLost();
}
