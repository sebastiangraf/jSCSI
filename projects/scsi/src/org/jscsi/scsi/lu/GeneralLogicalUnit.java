
package org.jscsi.scsi.lu;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.tasks.management.GeneralTaskManager;
import org.jscsi.scsi.transport.TargetTransportPort;

// TODO: Describe class or interface
public class GeneralLogicalUnit extends AbstractLogicalUnit
{
   private static int NUM_TASK_THREADS = 1;

   public GeneralLogicalUnit(
         TaskFactory taskFactory,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      super(taskFactory, new GeneralTaskManager(NUM_TASK_THREADS), modePageRegistry,
            inquiryDataRegistry);
   }

   public void enqueue(TargetTransportPort port, Command command)
   {
      // TODO Auto-generated method stub

   }
}
