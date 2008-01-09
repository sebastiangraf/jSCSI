
package org.jscsi.scsi.target;

import org.apache.log4j.Logger;
import org.jscsi.scsi.lu.LogicalUnit;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.tasks.TaskRouter;
import org.jscsi.scsi.tasks.management.DefaultTaskRouter;
import org.jscsi.scsi.tasks.management.TaskManagementFunction;
import org.jscsi.scsi.tasks.management.TaskServiceResponse;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;

public class DefaultTarget extends AbstractTarget
{
   private static Logger _logger = Logger.getLogger(DefaultTarget.class);
   
   private TaskRouter taskRouter;

   public DefaultTarget(String targetName)
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

   public void registerLogicalUnit(long lun, LogicalUnit lu)
   {
      this.taskRouter.registerLogicalUnit(lun, lu);
   }

   public LogicalUnit removeLogicalUnit(long lun)
   {
      return this.taskRouter.removeLogicalUnit(lun);
   }

   public synchronized void start()
   {
      _logger.debug("Starting task router for target " + this.getTargetName());
      this.taskRouter.start();
   }

   public synchronized void stop()
   {
      _logger.debug("Stopping task router for target " + this.getTargetName());
      this.taskRouter.stop();
   }
}
