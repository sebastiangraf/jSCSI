
package org.jscsi.scsi.tasks.lu;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.transport.TargetTransportPort;

public class LUTaskFactory implements TaskFactory
{
   private static Logger _logger = Logger.getLogger(LUTaskFactory.class);

   public Task getInstance(TargetTransportPort port, Command command)
         throws IllegalRequestException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public boolean respondsTo(Class<? extends Command> cls)
   {
      // TODO Auto-generated method stub
      return false;
   }


}


