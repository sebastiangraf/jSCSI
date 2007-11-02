
package org.jscsi.scsi.lu;

import org.apache.log4j.Logger;
import org.jscsi.scsi.exceptions.TaskSetException;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.tasks.management.GeneralTaskManager;
import org.jscsi.scsi.tasks.management.TaskManager;
import org.jscsi.scsi.transport.TargetTransportPort;

// TODO: Describe class or interface
public class GeneralLogicalUnit implements LogicalUnit
{
   private static Logger _logger = Logger.getLogger(GeneralLogicalUnit.class);

   private static int NUM_TASK_THREADS = 1;

   private TaskFactory taskFactory;
   private ModePageRegistry modePageRegistry;
   private InquiryDataRegistry inquiryDataRegistry;
   private TaskManager taskManager;

   public GeneralLogicalUnit(
         TaskFactory taskFactory,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      this.taskFactory = taskFactory;
      this.modePageRegistry = modePageRegistry;
      this.inquiryDataRegistry = inquiryDataRegistry;

      this.taskManager = new GeneralTaskManager(NUM_TASK_THREADS);
   }

   public void enqueue(TargetTransportPort port, Command command)
   {
      Task task = null;

      try
      {
         task =
               getTaskFactory().getInstance(port, command, getModePageRegistry(),
                     getInquiryDataRegistry());
      }
      catch (IllegalRequestException e)
      {
         // FIXME: handle/throw
      }

      //TODO: check task for null
      try
      {
         getTaskManager().submitTask(task);
      }
      catch (TaskSetException e)
      {
         // FIXME: handle/throw
      }
   }

   public void startTaskManagerThread()
   {
      Thread thread = new Thread(this.taskManager);
      thread.start();
   }

   public void stopTaskManagerThread()
   {
      this.taskManager.shutdown();
   }

   public TaskFactory getTaskFactory()
   {
      return this.taskFactory;
   }

   public void setTaskFactory(TaskFactory taskFactory)
   {
      this.taskFactory = taskFactory;
   }

   public TaskManager getTaskManager()
   {
      return this.taskManager;
   }

   public void setTaskManager(TaskManager taskManager)
   {
      this.taskManager = taskManager;
   }

   public ModePageRegistry getModePageRegistry()
   {
      return this.modePageRegistry;
   }

   public void setModePageRegistry(ModePageRegistry modePageRegistry)
   {
      this.modePageRegistry = modePageRegistry;
   }

   public InquiryDataRegistry getInquiryDataRegistry()
   {
      return this.inquiryDataRegistry;
   }

   public void setInquiryDataRegistry(InquiryDataRegistry inquiryDataRegistry)
   {
      this.inquiryDataRegistry = inquiryDataRegistry;
   }
}
