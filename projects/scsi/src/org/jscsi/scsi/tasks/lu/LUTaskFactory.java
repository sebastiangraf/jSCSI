
package org.jscsi.scsi.tasks.lu;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.Inquiry;
import org.jscsi.scsi.protocol.cdb.ModeSense10;
import org.jscsi.scsi.protocol.cdb.ModeSense6;
import org.jscsi.scsi.protocol.cdb.RequestSense;
import org.jscsi.scsi.protocol.cdb.TestUnitReady;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.protocol.sense.exceptions.InvalidCommandOperationCodeException;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.transport.TargetTransportPort;

public class LUTaskFactory implements TaskFactory
{
   private static Logger _logger = Logger.getLogger(LUTaskFactory.class);

   private static Map<Class<? extends CDB>, Class<? extends LUTask>> tasks =
      new HashMap<Class<? extends CDB>, Class<? extends LUTask>>();

   private ModePageRegistry modePageRegistry;
   private InquiryDataRegistry inquiryDataRegistry;

   static
   {
      LUTaskFactory.tasks.put(Inquiry.class, InquiryTask.class);
      LUTaskFactory.tasks.put(ModeSense6.class, ModeSenseTask.class);
      LUTaskFactory.tasks.put(ModeSense10.class, ModeSenseTask.class);
      LUTaskFactory.tasks.put(RequestSense.class, RequestSenseTask.class);
      LUTaskFactory.tasks.put(TestUnitReady.class, TestUnitReadyTask.class);
   }

   public LUTaskFactory(
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      this.modePageRegistry = modePageRegistry;
      this.inquiryDataRegistry = inquiryDataRegistry;
   }

   public Task getInstance(TargetTransportPort port, Command command)
   throws IllegalRequestException
   {
      Class<? extends LUTask> taskClass =
         tasks.get(command.getCommandDescriptorBlock().getClass());

      if (taskClass != null)
      {
         try
         {
            return taskClass.newInstance().load(port, command, modePageRegistry, inquiryDataRegistry);
         }
         catch (InstantiationException e)
         {
            _logger.error("sense exception when instantiating task from command");
            throw new InvalidCommandOperationCodeException();
         }
         catch (IllegalAccessException e)
         {
            _logger.error("sense exception when instanting task from command");
            
            throw new InvalidCommandOperationCodeException();
         }
      }
      else
      {
         throw new InvalidCommandOperationCodeException();
      }
   }

   public boolean respondsTo(Class<? extends CDB> cls)
   {
      return tasks.containsKey(cls);
   }

   public String toString()
   {
      return "<LUTask>";
   }
}
