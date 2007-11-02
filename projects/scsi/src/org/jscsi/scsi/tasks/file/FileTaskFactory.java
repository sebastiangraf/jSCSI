
package org.jscsi.scsi.tasks.file;

import java.util.HashMap;
import java.util.Map;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CommandDescriptorBlock;
import org.jscsi.scsi.protocol.cdb.Read10;
import org.jscsi.scsi.protocol.cdb.Read12;
import org.jscsi.scsi.protocol.cdb.Read16;
import org.jscsi.scsi.protocol.cdb.Read6;
import org.jscsi.scsi.protocol.cdb.Write10;
import org.jscsi.scsi.protocol.cdb.Write12;
import org.jscsi.scsi.protocol.cdb.Write16;
import org.jscsi.scsi.protocol.cdb.Write6;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.protocol.sense.exceptions.InvalidCommandOperationCodeException;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.transport.TargetTransportPort;

public class FileTaskFactory implements TaskFactory
{
   private static Map<Class<? extends CommandDescriptorBlock>, Class<? extends FileTask>> _tasks =
         new HashMap<Class<? extends CommandDescriptorBlock>, Class<? extends FileTask>>();

   private FileDevice device;
   private ModePageRegistry modePageRegistry;
   private InquiryDataRegistry inquiryDataRegistry;

   static
   {
      FileTaskFactory._tasks.put(Read6.class, ReadFileTask.class);
      FileTaskFactory._tasks.put(Read10.class, ReadFileTask.class);
      FileTaskFactory._tasks.put(Read12.class, ReadFileTask.class);
      FileTaskFactory._tasks.put(Read16.class, ReadFileTask.class);
      FileTaskFactory._tasks.put(Write6.class, WriteFileTask.class);
      FileTaskFactory._tasks.put(Write10.class, WriteFileTask.class);
      FileTaskFactory._tasks.put(Write12.class, WriteFileTask.class);
      FileTaskFactory._tasks.put(Write16.class, WriteFileTask.class);
   }

   public FileTaskFactory(
         FileDevice device,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      this.device = device;
      this.modePageRegistry = modePageRegistry;
      this.inquiryDataRegistry = inquiryDataRegistry;
   }



   public Task getInstance(
         TargetTransportPort port,
         Command command) throws IllegalRequestException
   {
      Class<? extends FileTask> taskClass =
            _tasks.get(command.getCommandDescriptorBlock().getClass());

      if (taskClass != null)
      {
         FileTask fileTask = null;
         try
         {
            fileTask = taskClass.newInstance();
         }
         catch (InstantiationException e)
         {
            // TODO: handle
         }
         catch (IllegalAccessException e)
         {
            // TODO: handle
         }

         fileTask.setCommand(command);
         fileTask.setTargetTransportPort(port);
         fileTask.setModePageRegistry(this.modePageRegistry);
         fileTask.setInquiryDataRegistry(this.inquiryDataRegistry);
         fileTask.setFileDevice(this.device);

         return fileTask;
      }
      else
      {
         throw new InvalidCommandOperationCodeException();
      }
   }
}
