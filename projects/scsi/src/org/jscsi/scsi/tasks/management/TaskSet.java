package org.jscsi.scsi.tasks.management;

import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;

import org.jscsi.scsi.tasks.Task;

public interface TaskSet extends BlockingQueue<Task>
{
   
   /**
    * Retrieves and removes the task specified by the given task tag. Task tag is
    * the <code>Q</code> in the I_T_L_Q nexus. The caller obtains a reference to the
    * removed task. The caller must attempt to abort task manually if applicable.
    * <p>
    * This method should be used when removing a task outside the normal execution flow. One
    * such usage is in processing SAM-2 <code>ABORT TASK</code> and <code>CLEAR TASK SET</code>
    * task management functions.
    * 
    * @param taskTag The tag of the task to remove.
    * @return The first task in the set with the given task tag; Null if no tasks with that
    *    tag are currently in the set.
    * @throws NoSuchElementException If a task with the given task tag does not exist.
    */
   Task remove(long taskTag) throws NoSuchElementException, InterruptedException;
}
