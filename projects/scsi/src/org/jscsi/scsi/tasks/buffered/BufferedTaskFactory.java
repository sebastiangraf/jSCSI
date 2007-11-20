
package org.jscsi.scsi.tasks.buffered;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.Inquiry;
import org.jscsi.scsi.protocol.cdb.ModeSense10;
import org.jscsi.scsi.protocol.cdb.ModeSense6;
import org.jscsi.scsi.protocol.cdb.Read10;
import org.jscsi.scsi.protocol.cdb.Read12;
import org.jscsi.scsi.protocol.cdb.Read16;
import org.jscsi.scsi.protocol.cdb.Read6;
import org.jscsi.scsi.protocol.cdb.ReadCapacity10;
import org.jscsi.scsi.protocol.cdb.ReadCapacity16;
import org.jscsi.scsi.protocol.cdb.TestUnitReady;
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
import org.jscsi.scsi.tasks.lu.InquiryTask;
import org.jscsi.scsi.tasks.lu.ModeSenseTask;
import org.jscsi.scsi.tasks.lu.TestUnitReadyTask;
import org.jscsi.scsi.transport.TargetTransportPort;

public class BufferedTaskFactory implements TaskFactory
{
   private static Map<Class<? extends CDB>, Class<? extends BufferedTask>> tasks =
         new HashMap<Class<? extends CDB>, Class<? extends BufferedTask>>();

   private ByteBuffer buffer;
   private int blockLength;
   private ModePageRegistry modePageRegistry;
   private InquiryDataRegistry inquiryDataRegistry;

   static
   {
      BufferedTaskFactory.tasks.put(Read6.class, BufferedReadTask.class);
      BufferedTaskFactory.tasks.put(Read10.class, BufferedReadTask.class);
      BufferedTaskFactory.tasks.put(Read12.class, BufferedReadTask.class);
      BufferedTaskFactory.tasks.put(Read16.class, BufferedReadTask.class);
      BufferedTaskFactory.tasks.put(Write6.class, BufferedWriteTask.class);
      BufferedTaskFactory.tasks.put(Write10.class, BufferedWriteTask.class);
      BufferedTaskFactory.tasks.put(Write12.class, BufferedWriteTask.class);
      BufferedTaskFactory.tasks.put(Write16.class, BufferedWriteTask.class);
      BufferedTaskFactory.tasks.put(ReadCapacity10.class, BufferedReadCapacityTask.class);
      BufferedTaskFactory.tasks.put(ReadCapacity16.class, BufferedReadCapacityTask.class);
   }

   public BufferedTaskFactory(
         ByteBuffer buffer,
         int blockLength,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      this.buffer = buffer;
      this.blockLength = blockLength;
      this.modePageRegistry = modePageRegistry;
      this.inquiryDataRegistry = inquiryDataRegistry;
   }

   public Task getInstance(TargetTransportPort port, Command command)
         throws IllegalRequestException
   {

      // check for basic commands
      switch (command.getCommandDescriptorBlock().getOperationCode())
      {
         case ModeSense6.OPERATION_CODE :
         case ModeSense10.OPERATION_CODE :
            return new ModeSenseTask(port, command, modePageRegistry, inquiryDataRegistry);
         case TestUnitReady.OPERATION_CODE :
            return new TestUnitReadyTask(port, command, modePageRegistry, inquiryDataRegistry);
         case Inquiry.OPERATION_CODE :
            return new InquiryTask(port, command, modePageRegistry, inquiryDataRegistry);
      }

      Class<? extends BufferedTask> taskClass =
            tasks.get(command.getCommandDescriptorBlock().getClass());

      if (taskClass != null)
      {
         try
         {
            return taskClass.newInstance().load(buffer, blockLength, port, command,
                  modePageRegistry, inquiryDataRegistry);
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

   public String toString()
   {
      return "<BufferedTask block-size: " + this.blockLength + ", backing-store: ByteBuffer>";
   }

   public boolean respondsTo(Class<? extends CDB> cls)
   {
      return tasks.containsKey(cls);
   }
}
