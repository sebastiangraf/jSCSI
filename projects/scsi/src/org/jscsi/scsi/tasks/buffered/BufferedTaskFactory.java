package org.jscsi.scsi.tasks.buffered;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.ModeSense10;
import org.jscsi.scsi.protocol.cdb.ModeSense6;
import org.jscsi.scsi.protocol.cdb.Read10;
import org.jscsi.scsi.protocol.cdb.Read12;
import org.jscsi.scsi.protocol.cdb.Read16;
import org.jscsi.scsi.protocol.cdb.Read6;
import org.jscsi.scsi.protocol.cdb.ReadCapacity10;
import org.jscsi.scsi.protocol.cdb.ReadCapacity16;
import org.jscsi.scsi.protocol.cdb.RequestSense;
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
import org.jscsi.scsi.tasks.generic.ModeSenseTask;
import org.jscsi.scsi.transport.TargetTransportPort;

public class BufferedTaskFactory implements TaskFactory
{
   private static Map<Class<? extends CDB>, Class<? extends BufferedTask>> _tasks =
         new HashMap<Class<? extends CDB>, Class<? extends BufferedTask>>();

   private ByteBuffer file;
   private int blockLength;
   private ModePageRegistry modePageRegistry;
   private InquiryDataRegistry inquiryDataRegistry;

   static
   {
      BufferedTaskFactory._tasks.put(Read6.class, BufferedReadTask.class);
      BufferedTaskFactory._tasks.put(Read10.class, BufferedReadTask.class);
      BufferedTaskFactory._tasks.put(Read12.class, BufferedReadTask.class);
      BufferedTaskFactory._tasks.put(Read16.class, BufferedReadTask.class);
      BufferedTaskFactory._tasks.put(Write6.class, BufferedWriteTask.class);
      BufferedTaskFactory._tasks.put(Write10.class, BufferedWriteTask.class);
      BufferedTaskFactory._tasks.put(Write12.class, BufferedWriteTask.class);
      BufferedTaskFactory._tasks.put(Write16.class, BufferedWriteTask.class);
      BufferedTaskFactory._tasks.put(ReadCapacity10.class, BufferedReadCapacity10Task.class);
      BufferedTaskFactory._tasks.put(ReadCapacity16.class, BufferedReadCapacity16Task.class);
      BufferedTaskFactory._tasks.put(RequestSense.class, BufferedRequestSenseTask.class);
   }

   public BufferedTaskFactory(
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
      
      // check for basic commands
      switch (command.getCommandDescriptorBlock().getOperationCode())
      {
         case ModeSense6.OPERATION_CODE:
         case ModeSense10.OPERATION_CODE:
            return new ModeSenseTask(port, command, modePageRegistry, inquiryDataRegistry);
      }
      
      
      Class<? extends BufferedTask> taskClass =
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
