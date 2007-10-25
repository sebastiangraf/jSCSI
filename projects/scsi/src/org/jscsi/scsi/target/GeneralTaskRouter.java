package org.jscsi.scsi.target;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jscsi.core.exceptions.NotImplementedException;
import org.jscsi.scsi.exceptions.TaskSetException;
import org.jscsi.scsi.lu.LogicalUnit;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskRouter;
import org.jscsi.scsi.tasks.management.GeneralTaskManager;
import org.jscsi.scsi.transport.TargetTransportPort;

public class GeneralTaskRouter implements TaskRouter
{
   private static Logger _logger = Logger.getLogger(GeneralTaskRouter.class);
   

   ////////////////////////////////////////////////////////////////////////////
   // data members

   private GeneralTaskManager _manager;
   private Map<Long, LogicalUnit> _logicalUnitMap;

   ////////////////////////////////////////////////////////////////////////////
   // constructor(s)
   
   public GeneralTaskRouter()
   {
      _logicalUnitMap = new ConcurrentHashMap<Long, LogicalUnit>();
      _manager = new GeneralTaskManager(1);
   }

   
   ////////////////////////////////////////////////////////////////////////////
   // TaskRouter implementation
   
   public void enqueue(TargetTransportPort port, Command command)
   {
      long luID = command.getNexus().getLogicalUnitNumber();
      Task task = null;

      if (luID < 0)
      {
         try
         {
            _manager.submitTask(task);
         }
         catch (TaskSetException e)
         {
            // TODO: wrap this exception and pass along as SenseData
         }
      }
      else
      {
         _logicalUnitMap.get(luID).enqueue(port, command);
      }
   }

   public void nexusLost()
   {
      // TODO: what the hell are we doing here
      throw new NotImplementedException("method must be implemented");
   }

   public void registerLogicalUnit(long id, LogicalUnit lu) throws Exception
   {
      _logicalUnitMap.put(id, lu);
   }

   public void removeLogicalUnit(long id) throws Exception
   {
      _logicalUnitMap.remove(id);
   }
}
