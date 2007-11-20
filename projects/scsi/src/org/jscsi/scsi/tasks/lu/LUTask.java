
package org.jscsi.scsi.tasks.lu;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.tasks.AbstractTask;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.transport.TargetTransportPort;

public abstract class LUTask extends AbstractTask
{
   private static Logger _logger = Logger.getLogger(LUTask.class);

   protected LUTask(String name)
   {
      super(name);
   }

   protected LUTask(
         String name,
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      super(name, targetPort, command, modePageRegistry, inquiryDataRegistry);
   }

   protected abstract void execute() throws InterruptedException, SenseException;

   public final Task loadTask(
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      super.load(targetPort, command, modePageRegistry, inquiryDataRegistry);
      return this;
   }
}
