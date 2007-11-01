
package org.jscsi.scsi.target;

import org.jscsi.scsi.lu.GeneralLogicalUnit;
import org.jscsi.scsi.lu.LogicalUnit;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.StaticInquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.StaticModePageRegistry;
import org.jscsi.scsi.tasks.TaskRouter;
import org.jscsi.scsi.tasks.target.TargetTaskFactory;
import org.jscsi.scsi.transport.TargetTransportPort;

public class DefaultTarget extends AbstractTarget
{
   private TaskRouter _taskRouter;

   public DefaultTarget(String targetName) throws Exception
   {
      this.setTargetName(targetName);

      this._taskRouter =
            new GeneralTaskRouter(new TargetTaskFactory(), new StaticModePageRegistry(),
                  new StaticInquiryDataRegistry());
      this._taskRouter.registerLogicalUnit(0, new GeneralLogicalUnit());
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
