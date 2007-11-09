package org.jscsi.scsi.target;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jscsi.core.exceptions.NotImplementedException;
import org.jscsi.scsi.exceptions.TaskSetException;
import org.jscsi.scsi.lu.LogicalUnit;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.protocol.sense.exceptions.LogicalUnitNotSupportedException;
import org.jscsi.scsi.tasks.Status;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.tasks.TaskRouter;
import org.jscsi.scsi.tasks.management.DefaultTaskManager;
import org.jscsi.scsi.tasks.management.DefaultTaskSet;
import org.jscsi.scsi.tasks.management.TaskManagementFunction;
import org.jscsi.scsi.tasks.management.TaskManager;
import org.jscsi.scsi.tasks.management.TaskServiceResponse;
import org.jscsi.scsi.tasks.management.TaskSet;
import org.jscsi.scsi.tasks.target.TargetTaskFactory;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;

public class DefaultTaskRouter implements TaskRouter
{
   private static Logger _logger = Logger.getLogger(DefaultTaskRouter.class);
   
   private static int DEFAULT_TARGET_THREAD_COUNT = 1;
   private static int DEFAULT_TARGET_QUEUE_LENGTH = 25;
   

   ////////////////////////////////////////////////////////////////////////////
   // data members

   private TaskSet targetTaskSet;
   private TaskManager targetTaskManager;
   private Map<Long, LogicalUnit> logicalUnitMap;
   private TaskFactory taskFactory;


   ////////////////////////////////////////////////////////////////////////////
   // constructor(s)
   
   
   public DefaultTaskRouter()
   {
      this.logicalUnitMap = new ConcurrentHashMap<Long, LogicalUnit>();
      this.taskFactory = new TargetTaskFactory(this.logicalUnitMap.keySet());
      this.targetTaskSet = new DefaultTaskSet(DEFAULT_TARGET_QUEUE_LENGTH);
      this.targetTaskManager = new DefaultTaskManager(DEFAULT_TARGET_THREAD_COUNT, targetTaskSet);
   }
   
   public DefaultTaskRouter(int targetQueueLength, int targetThreadCount)
   {
      this.logicalUnitMap = new ConcurrentHashMap<Long, LogicalUnit>();
      this.taskFactory = new TargetTaskFactory(this.logicalUnitMap.keySet());
      this.targetTaskSet = new DefaultTaskSet(targetQueueLength);
      this.targetTaskManager = new DefaultTaskManager(targetThreadCount, targetTaskSet);
   }
   
   public DefaultTaskRouter(
         TaskFactory targetTaskFactory,
         TaskSet targetTaskSet,
         TaskManager targetTaskManager)
   {
      this.logicalUnitMap = new ConcurrentHashMap<Long, LogicalUnit>();
      this.taskFactory = targetTaskFactory;
      this.targetTaskManager = targetTaskManager;
      this.targetTaskSet = targetTaskSet;
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
            Task task = this.taskFactory.getInstance(port, command);
            assert task != null : "improper task factory implementation returned null task";
            this.targetTaskSet.offer(task); // non-blocking, sends any errors to transport port
            _logger.debug("successfully enqueued target command with TaskRouter: " + command);
         }
         catch (IllegalRequestException e)
         {
            _logger.error("error when parsing command: " + e);
            port.writeResponse(
                  command.getNexus(),
                  command.getCommandReferenceNumber(),
                  Status.CHECK_CONDITION,
                  ByteBuffer.wrap(e.encode()) );
         }
      }
      else if ( logicalUnitMap.containsKey(lun) )
      {
         logicalUnitMap.get(lun).enqueue(port, command);
         _logger.debug("successfully enqueued command to logical unit with TaskRouter: " + command);
      }
      else
      {
         port.writeResponse(
               command.getNexus(),
               command.getCommandReferenceNumber(),
               Status.CHECK_CONDITION,
               ByteBuffer.wrap((new LogicalUnitNotSupportedException()).encode()));
      }
   }

   
   
   public TaskServiceResponse execute(Nexus nexus, TaskManagementFunction function)
   {
      long lun = nexus.getLogicalUnitNumber();
      switch (function)
      {
         case ABORT_TASK:
            // Need an I_T_L_Q nexus with a valid LUN
            if ( lun < 0 || nexus.getTaskTag() < 0 || ! this.logicalUnitMap.containsKey(lun) )
               return TaskServiceResponse.FUNCTION_REJECTED;
            else
               return this.logicalUnitMap.get(lun).abortTask(nexus);
            
         case ABORT_TASK_SET:
            // Need an I_T_L nexus with a valid LUN
            if (lun < 0 || ! this.logicalUnitMap.containsKey(lun))
               return TaskServiceResponse.FUNCTION_REJECTED;
            else
               return this.logicalUnitMap.get(lun).abortTaskSet(nexus);
         
         case CLEAR_TASK_SET:
            // Need an I_T_L nexus with a valid LUN
            if (lun < 0 || ! this.logicalUnitMap.containsKey(lun))
               return TaskServiceResponse.FUNCTION_REJECTED;
            else
               return this.logicalUnitMap.get(lun).clearTaskSet(nexus);
            
         case LOGICAL_UNIT_RESET:
            // Need an I_T_L nexus with a valid LUN
            if (lun < 0 || ! this.logicalUnitMap.containsKey(lun))
               return TaskServiceResponse.FUNCTION_REJECTED;
            else
               return this.logicalUnitMap.get(lun).reset();
            
         case TARGET_RESET:
            // reset all logical units and abort target task set
            for ( LogicalUnit lu : this.logicalUnitMap.values() )
            {
               lu.reset();
            }
            this.targetTaskSet.clear();
            return TaskServiceResponse.FUNCTION_COMPLETE;
            
         case CLEAR_ACA:
            return TaskServiceResponse.FUNCTION_REJECTED;
            
         case WAKEUP:
            return TaskServiceResponse.FUNCTION_REJECTED;
            
         default:
            return TaskServiceResponse.FUNCTION_REJECTED;
      }
   }

   public void nexusLost()
   {
      // reset all logical units and abort target task set
      for ( LogicalUnit lu : this.logicalUnitMap.values() )
      {
         lu.reset();
      }
      this.targetTaskSet.clear();
   }

   public void registerLogicalUnit(long id, LogicalUnit lu) throws Exception
   {
      lu.start();
      logicalUnitMap.put(id, lu);
      _logger.debug("registering logical unit: " + lu + " (id: " + id + ")");
   }

   public LogicalUnit removeLogicalUnit(long id) throws Exception
   {
      LogicalUnit discardedLU = logicalUnitMap.remove(id);
      discardedLU.stop();
      _logger.debug("removing logical unit: " + discardedLU);
      return discardedLU;
   }
}
