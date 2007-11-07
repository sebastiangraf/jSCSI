package org.jscsi.scsi.tasks.management;

import org.jscsi.scsi.exceptions.TaskSetException;
import org.jscsi.scsi.tasks.Task;

public interface TaskManager extends Runnable
{
   /**
    * Performs an orderly shutdown of the task manager. 
    * @deprecated Use {@link Thread#interrupt()} to interrupt the thread running the task manager.
    */
   void shutdown();
   
   
   
}
