package org.jscsi.scsi.tasks.management;

import org.jscsi.scsi.exceptions.TaskSetException;
import org.jscsi.scsi.tasks.Task;

public interface TaskManager extends Runnable
{
   void run();
   void shutdown();
   void submitTask(Task task) throws TaskSetException;
}
