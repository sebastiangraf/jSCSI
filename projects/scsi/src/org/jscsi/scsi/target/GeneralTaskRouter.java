package org.jscsi.scsi.target;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jscsi.core.exceptions.NotImplementedException;
import org.jscsi.scsi.exceptions.TaskSetException;
import org.jscsi.scsi.lu.LogicalUnit;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.tasks.TaskFactory;
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
   private TaskFactory _taskFactory;
   private ModePageRegistry _modeRegistry;
   private InquiryDataRegistry _inquiryRegistry;


   ////////////////////////////////////////////////////////////////////////////
   // constructor(s)
   
   public GeneralTaskRouter(TaskFactory taskFactory, ModePageRegistry modeRegistry, InquiryDataRegistry inquiryRegistry)
   {
      _logicalUnitMap = new ConcurrentHashMap<Long, LogicalUnit>();
      _manager = new GeneralTaskManager(1);
      _taskFactory = taskFactory;
      _modeRegistry = modeRegistry;
      _inquiryRegistry = inquiryRegistry;
   }


   ////////////////////////////////////////////////////////////////////////////
   // TaskRouter implementation
   
   public void enqueue(TargetTransportPort port, Command command)
   {
      long luID = command.getNexus().getLogicalUnitNumber();

      if (luID < 0)
      {
         try
         {
            _manager.submitTask(_taskFactory.getInstance(port, command, _modeRegistry, _inquiryRegistry));
         }
         // thrown by the TaskManager
         catch (TaskSetException e)
         {
            _logger.error("error when submitting task to TaskManager: " + e);
            // TODO: wrap this exception and pass along as SenseData
         }
         // thrown by the TaskFactory
         catch (IllegalRequestException e)
         {
            _logger.error("error when parsing command: " + e);
            // TODO: wrap this exception and pass along as SenseData
         }
      }
      else
      {
         _logicalUnitMap.get(luID).enqueue(port, command);
      }
      _logger.debug("successfully enqueued command with TaskRouter: " + command);
   }

   public void nexusLost()
   {
      // TODO: define behavior here
      throw new NotImplementedException("method must be implemented");
   }

   public void registerLogicalUnit(long id, LogicalUnit lu) throws Exception
   {
      _logicalUnitMap.put(id, lu);
      _logger.debug("registering logical unit: " + lu + " (id: " + id + ")");
   }

   public void removeLogicalUnit(long id) throws Exception
   {
      LogicalUnit discardedLU = _logicalUnitMap.remove(id);
      _logger.debug("removing logical unit: " + discardedLU);
   }
}
