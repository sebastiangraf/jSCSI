package org.jscsi.scsi.tasks;

import org.jscsi.scsi.exceptions.TaskSetException;

public interface TaskSet
{
   void submitTask(Task task) throws TaskSetException;
}
