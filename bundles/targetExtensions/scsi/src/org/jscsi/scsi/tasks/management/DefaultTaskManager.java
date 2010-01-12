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

package org.jscsi.scsi.tasks.management;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.jscsi.core.utils.NamedThreadFactory;
import org.jscsi.scsi.tasks.Task;

public class DefaultTaskManager implements TaskManager
{
   private static Logger _logger = Logger.getLogger(DefaultTaskManager.class);

   private ExecutorService executor;
   private final AtomicBoolean running = new AtomicBoolean(false);

   private TaskSet taskSet;

   private Thread currentThread;

   /////////////////////////////////////////////////////////////////////////////
   // constructor(s)

   public DefaultTaskManager(int numThreads, TaskSet taskSet)
   {
      executor = Executors.newFixedThreadPool(numThreads, new NamedThreadFactory("TaskExecutor"));
      this.taskSet = taskSet;
   }

   /////////////////////////////////////////////////////////////////////////////
   // TaskManager implementation

   public void run()
   {
      this.currentThread = Thread.currentThread();
      this.running.set(true);

      while (this.running.get())
      {
         Task nextTask = null;
         try
         {
            if (_logger.isDebugEnabled())
               _logger.debug("Waiting for next task...");
            nextTask = this.taskSet.take();
            if (_logger.isDebugEnabled())
               _logger.debug("next from taskset is task: " + nextTask.getCommand());
            this.executor.submit(nextTask);
            if (_logger.isDebugEnabled())
               _logger.debug("submitted for execution command: " + nextTask.getCommand());
         }
         catch (InterruptedException e)
         {
            _logger.debug("Task manager thread interrupted.");
            this.running.set(false);
         }
         catch (Exception e)
         {
            _logger.debug("Task manager experienced an unhandled exception, shutting down");
            this.running.set(false);
         }
      }
      _logger.info("Shutting down task manager.");

      this.taskSet.clear();
      executor.shutdown();
   }

   public void shutdown() throws InterruptedException
   {
      this.running.set(false);
      if (this.currentThread != null)
      {
         this.currentThread.interrupt();
         this.currentThread.join();
      }
   }
}
