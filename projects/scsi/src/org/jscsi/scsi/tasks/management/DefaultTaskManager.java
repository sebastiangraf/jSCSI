package org.jscsi.scsi.tasks.management;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.jscsi.scsi.exceptions.TaskSetException;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskAttribute;

public class DefaultTaskManager implements TaskManager, TaskSet
{
   
   private static Logger _logger = Logger.getLogger(TaskSet.class);
   
   
   private ExecutorService _executor;
   private final AtomicBoolean running = new AtomicBoolean(false);
   
   private TaskSet taskSet;
   
   private Thread currentThread = Thread.currentThread();
   
   
   
   
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
   
   public void shutdown()
   {
      this.running.set(false);
      this.currentThread.interrupt();
   }
   
   
}
