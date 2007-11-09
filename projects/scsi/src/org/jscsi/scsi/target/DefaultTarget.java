
package org.jscsi.scsi.target;

import org.jscsi.scsi.lu.LogicalUnit;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.mode.StaticModePageRegistry;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.tasks.TaskRouter;
import org.jscsi.scsi.tasks.management.TaskManagementFunction;
import org.jscsi.scsi.tasks.management.TaskServiceResponse;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;

public class DefaultTarget extends AbstractTarget
{
   private TaskRouter taskRouter;

   public DefaultTarget(String targetName) throws Exception
   {
      this.setTargetName(targetName);
      this.taskRouter = new DefaultTaskRouter();
   }

   @Override
   public void enqueue(TargetTransportPort port, Command command)
   {
      this.taskRouter.enqueue(port, command);
   }

   public TaskServiceResponse execute(Nexus nexus, TaskManagementFunction function)
   {
      return this.taskRouter.execute(nexus, function);
   }

   public void nexusLost()
   {
      this.taskRouter.nexusLost();
   }

   public void registerLogicalUnit(long lun, LogicalUnit lu) throws Exception
   {
      this.taskRouter.registerLogicalUnit(lun, lu);
   }

   public LogicalUnit removeLogicalUnit(long lun) throws Exception
   {
      return this.taskRouter.removeLogicalUnit(lun);
   }
   
   
   

}
