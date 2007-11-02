package org.jscsi.scsi.target;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jscsi.core.exceptions.NotImplementedException;
import org.jscsi.scsi.exceptions.TaskSetException;
import org.jscsi.scsi.lu.LogicalUnit;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.protocol.sense.exceptions.LogicalUnitNotSupportedException;
import org.jscsi.scsi.tasks.Status;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.tasks.TaskRouter;
import org.jscsi.scsi.tasks.management.DefaultTaskManager;
import org.jscsi.scsi.tasks.target.TargetTaskFactory;
import org.jscsi.scsi.transport.TargetTransportPort;

public class DefaultTaskRouter implements TaskRouter
{
   private static Logger _logger = Logger.getLogger(DefaultTaskRouter.class);
   
   private static int DEFAULT_TARGET_TASK_MANAGER_THREAD_COUNT = 1;
   

   ////////////////////////////////////////////////////////////////////////////
   // data members

   private DefaultTaskManager _targetTaskManager;
   private Map<Long, LogicalUnit> _logicalUnitMap;
   private TaskFactory _taskFactory;


   ////////////////////////////////////////////////////////////////////////////
   // constructor(s)
   
   
   public DefaultTaskRouter(ModePageRegistry modePageRegistry)
   {
      this(new TargetTaskFactory(modePageRegistry));
   }
   
   public DefaultTaskRouter(TaskFactory taskFactory)
   {
      _logicalUnitMap = new ConcurrentHashMap<Long, LogicalUnit>();
      _targetTaskManager = new DefaultTaskManager(DEFAULT_TARGET_TASK_MANAGER_THREAD_COUNT);
      _taskFactory = taskFactory;
   }


   ////////////////////////////////////////////////////////////////////////////
   // TaskRouter implementation
   
   public void enqueue(TargetTransportPort port, Command command)
   {
      long lun = command.getNexus().getLogicalUnitNumber();

      if (lun < 0)
      {
         try
         {
            _targetTaskManager.submitTask(_taskFactory.getInstance(port, command));
         }
         // thrown by the TaskManager
         catch (TaskSetException e)
         {
            _logger.error("error when submitting task to TaskManager: " + e);
            // TODO: wrap this exception and pass along as SenseData
            // perhaps TaskManager itself should throw a SenseException
            throw new RuntimeException("unhandled task set exception", e);
         }
         // thrown by the TaskFactory
         catch (IllegalRequestException e)
         {
            _logger.error("error when parsing command: " + e);
            port.writeResponse(
                  command.getNexus(),
                  command.getCommandReferenceNumber(),
                  Status.CHECK_CONDITION,
                  ByteBuffer.wrap(e.encode()));
         }
      }
      else if ( _logicalUnitMap.containsKey(lun) )
      {
         _logicalUnitMap.get(lun).enqueue(port, command);
      }
      else
      {
         port.writeResponse(
               command.getNexus(),
               command.getCommandReferenceNumber(),
               Status.CHECK_CONDITION,
               ByteBuffer.wrap((new LogicalUnitNotSupportedException()).encode()));
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
      lu.start();
      _logicalUnitMap.put(id, lu);
      _logger.debug("registering logical unit: " + lu + " (id: " + id + ")");
   }

   public void removeLogicalUnit(long id) throws Exception
   {
      LogicalUnit discardedLU = _logicalUnitMap.remove(id);
      discardedLU.stop();
      _logger.debug("removing logical unit: " + discardedLU);
   }
}
