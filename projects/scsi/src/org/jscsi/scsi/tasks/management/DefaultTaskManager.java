
package org.jscsi.scsi.tasks.management;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import org.jscsi.core.util.NamedThreadFactory;
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
