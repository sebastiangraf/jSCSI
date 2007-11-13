package org.jscsi.scsi.tasks.management;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.jscsi.scsi.tasks.Task;

public class DefaultTaskManager implements TaskManager
{   
   private static Logger _logger = Logger.getLogger(TaskSet.class);
   
   
   private ExecutorService _executor;
   private final AtomicBoolean running = new AtomicBoolean(false);
   
   private TaskSet taskSet;
   
   private Thread currentThread;
   

   /////////////////////////////////////////////////////////////////////////////
   // constructor(s)
   
   public DefaultTaskManager(int numThreads, TaskSet taskSet)
   {
      _executor = Executors.newFixedThreadPool(numThreads);
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
            nextTask = this.taskSet.take();
            this._executor.submit(nextTask);
            _logger.debug("TaskManager executed task: " + nextTask);
         }
         catch (InterruptedException e)
         {
            _logger.debug("Task manager thread interrupted.");
            this.running.set(false);
         }
      }
      _logger.info("Shutting down task manager.");
      // stop processing tasks:
      //  - clear task set
      //  - wait for any outstanding tasks to finish executing
      this.taskSet.clear();
      _executor.shutdown();
   }
   
   public void shutdown() throws InterruptedException
   {
      this.running.set(false);
      if ( this.currentThread != null )
      {
         this.currentThread.interrupt();
         this.currentThread.join();
      }
   }
   
   
}
