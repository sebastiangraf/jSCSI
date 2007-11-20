
package org.jscsi.scsi.tasks.target;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.ReportLuns;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.protocol.sense.exceptions.InvalidCommandOperationCodeException;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.transport.TargetTransportPort;

public class TargetTaskFactory implements TaskFactory
{
   private static Logger _logger = Logger.getLogger(TargetTaskFactory.class);
   
   private static Map<Class<? extends CDB>, Class<? extends TargetTask>> tasks =
      new HashMap<Class<? extends CDB>, Class<? extends TargetTask>>();

   private Set<Long> logicalUnits;

   static
   {
      TargetTaskFactory.tasks.put(ReportLuns.class, ReportLunsTask.class);
   }

   public TargetTaskFactory(Set<Long> logicalUnits)
   {
      this.logicalUnits = logicalUnits;
   }

   public Task getInstance(TargetTransportPort port, Command command)
   throws IllegalRequestException
   {
      switch (command.getCommandDescriptorBlock().getOperationCode())
      {
         case ReportLuns.OPERATION_CODE:
            return new ReportLunsTask(logicalUnits, port, command, null, null);
         default:
            _logger.error(
                  "Initiator attempted to execute unsupported command: (" +
                  command.getCommandDescriptorBlock().getOperationCode() + ") " +
                  command.getCommandDescriptorBlock().getClass().getName());
            throw new InvalidCommandOperationCodeException();
      }
      
   }

   public boolean respondsTo(Class<? extends CDB> cls)
   {
      return TargetTaskFactory.tasks.containsKey(cls);
   }
}
