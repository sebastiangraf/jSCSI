package org.jscsi.scsi.tasks.management;


public interface TaskManager extends Runnable
{
   // TODO: Do we just want to have TaskManager implement Thread directly?
   
   /**
    * Performs an orderly shutdown of the task manager. 
    * @deprecated Use {@link Thread#interrupt()} to interrupt the thread running the task manager.
    */
   void shutdown() throws InterruptedException;
   
   
   
}
