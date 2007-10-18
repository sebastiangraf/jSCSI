package org.jscsi.scsi.tasks.file;

import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.transport.TargetTransportPort;

public class FileTaskFactory implements TaskFactory
{

   public Task getInstance(TargetTransportPort port, Command command, ByteBuffer output)
   {
      // TODO Auto-generated method stub
      return null;
   }

}
