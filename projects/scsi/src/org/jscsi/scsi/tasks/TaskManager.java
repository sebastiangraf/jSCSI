
package org.jscsi.scsi.tasks;

public interface TaskManager extends Runnable
{
   void run();
   void shutdown();
}
