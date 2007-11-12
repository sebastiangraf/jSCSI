package org.jscsi.scsi.tasks.buffered;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CDB;
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
   private static Map<Class<? extends CDB>, Class<? extends FileTask>> _tasks =
         new HashMap<Class<? extends CDB>, Class<? extends FileTask>>();

   private ByteBuffer file;
   private int blockLength;
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
         ByteBuffer file,
         int blockLength,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      this.file = file;
      this.blockLength = blockLength;
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
         try
         {
            return taskClass
               .newInstance()
               .load(file, blockLength, port, command, modePageRegistry, inquiryDataRegistry);
         }
         catch (InstantiationException e)
         {
            throw new RuntimeException("Unable to instantiate class with default constructor"
                  + taskClass.getName(), e);
         }
         catch (IllegalAccessException e)
         {
            throw new RuntimeException("Unable to instantiate class with default constructor"
                  + taskClass.getName(), e);
         }
      }
      else
      {
         throw new InvalidCommandOperationCodeException();
      }
   }
}
