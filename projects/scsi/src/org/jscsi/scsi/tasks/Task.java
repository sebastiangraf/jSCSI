package org.jscsi.scsi.tasks;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.transport.TargetTransportPort;

public interface Task extends Runnable
{
   public void run();
   
   public Command getCommand();
   
   /**
    * If this is not used anywhere, remove it.
    * @deprecated
    */
   public TargetTransportPort getTargetTransportPort();
   
}
