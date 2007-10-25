
package org.jscsi.scsi.tasks.management;

public interface TaskManager extends Runnable
{
   void run();
   void shutdown();
}
