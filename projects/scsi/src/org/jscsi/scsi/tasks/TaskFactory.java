
package org.jscsi.scsi.tasks;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.transport.TargetTransportPort;

// TODO: Describe class or interface
public interface TaskFactory
{

   Task getInstance( TargetTransportPort port, Command command, ByteBuffer output );
   
   
}


