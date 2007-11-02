
package org.jscsi.scsi.target;

import org.jscsi.scsi.lu.LogicalUnit;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.mode.StaticModePageRegistry;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.tasks.TaskRouter;
import org.jscsi.scsi.transport.TargetTransportPort;

public class DefaultTarget extends AbstractTarget
{
   private TaskRouter _taskRouter;

   public DefaultTarget(String targetName) throws Exception
   {
      this.setTargetName(targetName);

      this._taskRouter =
            new DefaultTaskRouter(new StaticModePageRegistry());
   }
   
   public DefaultTarget(String targetName, ModePageRegistry registry)
   {
      this.setTargetName(targetName);
      this._taskRouter = new DefaultTaskRouter(registry);
   }

   @Override
   public void enqueue(TargetTransportPort port, Command command)
   {
      this._taskRouter.enqueue(port, command);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jscsi.scsi.target.Target#nexusLost()
    */
   public void nexusLost()
   {
   }

   public void registerLogicalUnit(long id, LogicalUnit lu) throws Exception
   {
      this._taskRouter.registerLogicalUnit(id, lu);
   }

   public void removeLogicalUnit(long id) throws Exception
   {
      this._taskRouter.removeLogicalUnit(id);
      
   }
}
