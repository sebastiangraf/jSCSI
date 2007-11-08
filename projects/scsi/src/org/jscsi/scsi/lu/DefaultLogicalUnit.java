
package org.jscsi.scsi.lu;

import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.jscsi.scsi.exceptions.TaskSetException;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.tasks.management.TaskManager;
import org.jscsi.scsi.tasks.management.TaskServiceResponse;
import org.jscsi.scsi.tasks.management.TaskSet;
import org.jscsi.scsi.transport.TargetTransportPort;

// TODO: Describe class or interface
public abstract class DefaultLogicalUnit implements LogicalUnit
{
   private static Logger _logger = Logger.getLogger(DefaultLogicalUnit.class);

   private TaskSet taskSet;
   private TaskManager taskManager;
   private InquiryDataRegistry inquiryDataRegistry;
   private TaskFactory taskFactory;
   
   private Thread manager;

   protected DefaultLogicalUnit()
   {
   }
   
   protected DefaultLogicalUnit(
         TaskSet taskSet, 
         TaskManager taskManager, 
         InquiryDataRegistry inquiryDataRegistry)
   {
      this.taskSet = taskSet;
      this.taskManager = taskManager;
      this.inquiryDataRegistry = inquiryDataRegistry;
   }
   

   public void enqueue(TargetTransportPort port, Command command)
   {
      Task task = null;

      try
      {
         task = getTaskFactory().getInstance(port, command);
      }
      catch (IllegalRequestException e)
      {
         // FIXME: handle/throw
      }

      //TODO: check task for null
      try
      {
         taskManager.submitTask(task);
      }
      catch (TaskSetException e)
      {
         // FIXME: handle/throw
      }
   }

   public void start()
   {
      this.manager = new Thread(this.taskManager);
      this.manager.start();
   }

   public void stop()
   {
      this.manager.interrupt();
   }

   @Override
   public TaskServiceResponse abortTask(long taskTag)
   {
      try
      {
         if ( this.taskSet.remove(taskTag).abort() )
         {
            return TaskServiceResponse.FUNCTION_COMPLETE;
         }
         else
         {
            // probably failed abort() because the task was already done or is finishing
            return TaskServiceResponse.FUNCTION_REJECTED;
         }
      }
      catch (NoSuchElementException e)
      {
         return TaskServiceResponse.FUNCTION_REJECTED;
      }
      catch (InterruptedException e)
      {
         return TaskServiceResponse.SERVICE_DELIVERY_OR_TARGET_FAILURE;
      }
   }

   public TaskServiceResponse abortTaskSet()
   {
      // TODO Auto-generated method stub
      return null;
   }

   
   /////////////////////////////////////////////////////////////////////////////
   // getters/setters
   
   
   public TaskSet getTaskSet()
   {
      return taskSet;
   }

   public void setTaskSet(TaskSet taskSet)
   {
      this.taskSet = taskSet;
   }

   public TaskManager getTaskManager()
   {
      return taskManager;
   }

   public void setTaskManager(TaskManager taskManager)
   {
      this.taskManager = taskManager;
   }

   public InquiryDataRegistry getInquiryDataRegistry()
   {
      return inquiryDataRegistry;
   }

   public void setInquiryDataRegistry(InquiryDataRegistry inquiryDataRegistry)
   {
      this.inquiryDataRegistry = inquiryDataRegistry;
   }

   public TaskFactory getTaskFactory()
   {
      return taskFactory;
   }

   public void setTaskFactory(TaskFactory taskFactory)
   {
      this.taskFactory = taskFactory;
   }
}
