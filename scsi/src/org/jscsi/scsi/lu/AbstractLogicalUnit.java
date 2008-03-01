//Cleversafe open-source code header - Version 1.1 - December 1, 2006
//
//Cleversafe Dispersed Storage(TM) is software for secure, private and
//reliable storage of the world's data using information dispersal.
//
//Copyright (C) 2005-2007 Cleversafe, Inc.
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
//USA.
//
//Contact Information:
// Cleversafe, 10 W. 35th Street, 16th Floor #84,
// Chicago IL 60616
// email: licensing@cleversafe.org
//
//END-OF-HEADER
//-----------------------
//@author: John Quigley <jquigley@cleversafe.com>
//@date: January 1, 2008
//---------------------

package org.jscsi.scsi.lu;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.tasks.management.TaskManager;
import org.jscsi.scsi.tasks.management.TaskServiceResponse;
import org.jscsi.scsi.tasks.management.TaskSet;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;

//TODO: Describe class or interface
public abstract class AbstractLogicalUnit implements LogicalUnit
{
   private static Logger _logger = Logger.getLogger(AbstractLogicalUnit.class);

   private TaskSet taskSet;
   private TaskManager taskManager;
   private TaskFactory taskFactory;

   private Thread manager;

   /////////////////////////////////////////////////////////////////////////////
   // constructors

   protected AbstractLogicalUnit()
   {
   }

   protected AbstractLogicalUnit(TaskSet taskSet, TaskManager taskManager, TaskFactory taskFactory)
   {
      this.taskSet = taskSet;
      this.taskManager = taskManager;
      this.taskFactory = taskFactory;
   }

   /////////////////////////////////////////////////////////////////////////////
   // operations

   public void enqueue(TargetTransportPort port, Command command)
   {
      if (_logger.isDebugEnabled())
      {
         _logger.debug("enqueuing command: " + command + ", associated with TargetTransportPort: "
               + port);
      }
      try
      {
         Task task = this.getTaskFactory().getInstance(port, command);
         assert task != null : "improper task factory implementation returned null task";
         if (_logger.isDebugEnabled())
         {
            _logger.debug("successfully constructed task: " + task);
         }
         this.taskSet.offer(task); // non-blocking, task set sends any errors to transport port
      }
      catch (IllegalRequestException e)
      {
         port.writeResponse(command.getNexus(), command.getCommandReferenceNumber(),
               Status.CHECK_CONDITION, ByteBuffer.wrap(e.encode()));
      }
   }

   public void start()
   {
      this.manager = new Thread(this.taskManager, "TaskManager");
      this.manager.start();
   }

   public void stop()
   {
      _logger.debug("Signalling LU task manager to stop");
      this.manager.interrupt();
      try
      {
         _logger.debug("Waiting for LU task manager to terminate");
         this.manager.join();
         _logger.debug("LU task manger finished");
      }
      catch (InterruptedException e)
      {
         _logger.warn("Interrupted while waiting for LU task manager to finish");
      }
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

   // TODO: nomenclature of this is strange, this is aborting a task, abortTask() above
   //       is simply attempting to remove a task from the scheduler
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

   protected void setTaskSet(TaskSet taskSet)
   {
      this.taskSet = taskSet;
   }

   public TaskManager getTaskManager()
   {
      return taskManager;
   }

   protected void setTaskManager(TaskManager taskManager)
   {
      this.taskManager = taskManager;
   }

   public TaskFactory getTaskFactory()
   {
      return taskFactory;
   }

   protected void setTaskFactory(TaskFactory taskFactory)
   {
      this.taskFactory = taskFactory;
   }

   @Override
   public String toString()
   {
      return "<LogicalUnit task: " + this.taskFactory + ">";
   }
}
