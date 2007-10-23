package org.jscsi.scsi.tasks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskManager extends TaskSet implements Runnable
{

   // TODO: can be interrupted safely?
   
   // Tasks executed() within the actual Manager
   //   - default impl should be single threaded
   
   private ExecutorService _executor;
   private boolean _running = false;
   
   public void run()
   {
      _running = true;
      Task nextTask;
      
      while (_running)
      {
         nextTask = null;
         try
         {
            nextTask = super._popTask(1);
         }
         catch (InterruptedException e)
         {
            // TODO: wrap this exception and pass along to higher layer
         }
         if (nextTask != null)
         {
            _executor.submit(nextTask);
         }
      }
      _executor.shutdown();
   }
   
   public void shutdown()
   {
      _running = false;
   }
   
   public TaskManager(int numThreads)
   {
      super();
      _executor = Executors.newFixedThreadPool(numThreads);
   }
}
