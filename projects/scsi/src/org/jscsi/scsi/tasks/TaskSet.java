
package org.jscsi.scsi.tasks;

import org.jscsi.scsi.protocol.Command;

public interface TaskSet
{
   void addTask(Command command, TaskAttribute attribute);
}


