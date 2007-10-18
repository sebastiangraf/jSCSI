
package org.jscsi.scsi.tasks;

import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;

public interface Task
{
   public void execute();

   public Command getCommand();
   
   public Nexus getNexus();
   
   public TargetTransportPort getTargetTransportPort();
}
