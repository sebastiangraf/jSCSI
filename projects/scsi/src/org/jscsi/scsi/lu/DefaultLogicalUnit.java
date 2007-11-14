package org.jscsi.scsi.lu;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.tasks.Status;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.tasks.management.TaskManager;
import org.jscsi.scsi.tasks.management.TaskServiceResponse;
import org.jscsi.scsi.tasks.management.TaskSet;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;

// TODO: Describe class or interface
public abstract class DefaultLogicalUnit implements LogicalUnit
{
   private static Logger _logger = Logger.getLogger(DefaultLogicalUnit.class);
   
   private TaskSet taskSet;
   private TaskManager taskManager;
   private TaskFactory taskFactory;
   
   private Thread manager;

   protected DefaultLogicalUnit()
   {
   }
   
   protected DefaultLogicalUnit(
         TaskSet taskSet, 
         TaskManager taskManager, 
         TaskFactory taskFactory)
   {
      this.taskSet = taskSet;
      this.taskManager = taskManager;
      this.taskFactory = taskFactory;
   }

   public void enqueue(TargetTransportPort port, Command command)
   {
      _logger.debug("enqueuing command: " + command + ", associate with TargetTransportPort: " + port);
      try
      {
         Task task = this.getTaskFactory().getInstance(port, command);
         assert task != null : "improper task factory implementation returned null task";
         _logger.debug("successfully constructed task: " + task);
         this.taskSet.offer(task); // non-blocking, task set sends any errors to transport port
      }
      catch (IllegalRequestException e)
      {
         port.writeResponse(
               command.getNexus(),
               command.getCommandReferenceNumber(),
               Status.CHECK_CONDITION,
               ByteBuffer.wrap(e.encode()) );
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

   public TaskServiceResponse abortTask(Nexus nexus)
   {
      try
      {
         this.taskSet.remove(nexus);
         return TaskServiceResponse.FUNCTION_COMPLETE;
      }
      catch (NoSuchElementException e)
      {
         return TaskServiceResponse.FUNCTION_COMPLETE;
      }
      catch (InterruptedException e)
      {
         return TaskServiceResponse.SERVICE_DELIVERY_OR_TARGET_FAILURE;
      }
   }
   
   public TaskServiceResponse abortTaskSet(Nexus nexus)
   {
      try
      {
         this.taskSet.abort(nexus);
         return TaskServiceResponse.FUNCTION_COMPLETE;
      }
      catch (InterruptedException e)
      {
         return TaskServiceResponse.SERVICE_DELIVERY_OR_TARGET_FAILURE;
      }
      catch (IllegalArgumentException e)
      {
         throw e;
      }
   }

   public TaskServiceResponse clearTaskSet(Nexus nexus)
   {
      try
      {
         this.taskSet.clear(nexus);
         return TaskServiceResponse.FUNCTION_COMPLETE;
      }
      catch (InterruptedException e)
      {
         return TaskServiceResponse.SERVICE_DELIVERY_OR_TARGET_FAILURE;
      }
      catch (IllegalArgumentException e)
      {
         throw e;
      }
   }

   public void nexusLost()
   {
      this.taskSet.clear();
   }

   public TaskServiceResponse reset()
   {
      this.taskSet.clear();
      return TaskServiceResponse.FUNCTION_COMPLETE;
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

   public TaskFactory getTaskFactory()
   {
      return taskFactory;
   }

   public void setTaskFactory(TaskFactory taskFactory)
   {
      this.taskFactory = taskFactory;
   }
   
   @Override
   public String toString()
   {
      return "<DefaultLogicalUnit task: " + this.taskFactory + ">";
   }
}
