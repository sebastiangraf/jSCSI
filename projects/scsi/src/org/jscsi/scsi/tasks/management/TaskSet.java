package org.jscsi.scsi.tasks.management;

import org.jscsi.scsi.exceptions.TaskSetException;
import org.jscsi.scsi.tasks.Task;

public interface TaskSet
{
   void submitTask(Task task) throws TaskSetException;
}
