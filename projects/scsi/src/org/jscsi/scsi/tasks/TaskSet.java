package org.jscsi.scsi.tasks;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jscsi.scsi.exceptions.TaskSetException;

public class TaskSet
{
   private List<Task> _headOfQueue; // Queue of Head of Queue tasks
   private List<Task> _soTaskQueue; // Simple/Ordered (SO) Task Queue
   private TaskAttribute _soTaskState = null; // Simple/Ordered Task Queue state
   private Map<Integer,Integer> _simplePriorityMap;
   private Object _taskAdded;

   ////////////////////////////////////////////////////////////////////////////
   // interface
   
   public void Taskset()
   {
      _headOfQueue = Collections.synchronizedList(new LinkedList<Task>());
      _simplePriorityMap = Collections.synchronizedMap(new HashMap<Integer, Integer>());
      _taskAdded = new Object();
   }
   
   public void submitTask(Task task, TaskAttribute attribute)
   throws TaskSetException
   {
      if (attribute == TaskAttribute.HEAD_OF_QUEUE)
      {
         this._addHeadOfQueue(task);
      }
      else
      {
         this._addSOTask(task, attribute);
      }
   }
   
   ////////////////////////////////////////////////////////////////////////////
   // protected queue accessors
   
   /**
    * Non-blocking method for acquiring the next queued task.
    * 
    * 
    * @return Task
    */
   protected Task _popTask() throws InterruptedException
   {
      return _getNextQueuedTask(0);
   }
   
   protected Task _popTask(long timeout) throws InterruptedException
   {
      return _getNextQueuedTask(timeout);
   }
   
   /**
    * 
    * @param timeout Time to wait 
    * @return
    */
   private Task _getNextQueuedTask(long timeout) throws InterruptedException
   {
      Task retval = null;
      
      if (_headOfQueue.size() == 0 && _soTaskQueue.size() == 0)
         _taskAdded.wait(timeout);
         
      if (_headOfQueue.size() > 0)
      {
         retval = _headOfQueue.remove(0);
      }
      else if (_soTaskQueue.size() > 0)
      {
         retval = _soTaskQueue.remove(0);
      }
      return retval;
   }
   
   private void _addSOTask(Task task, TaskAttribute attribute) throws TaskSetException
   {
      if (attribute == TaskAttribute.SIMPLE && _soTaskState != TaskAttribute.SIMPLE)
      {
         _soTaskQueue.add(task);
         _simplePriorityMap.clear();
      }
      else if (attribute == TaskAttribute.SIMPLE && _soTaskState == TaskAttribute.SIMPLE)
      {
         int priority = task.getPriority();
         
         if (priority == 0)
         {
            // TODO: see SAM:8.7
            // Should inspect I_T_L nexus "SET PRIORITY", and define vendor-specific
            // behavior should "SET PRIORITY" not be available or set.  For now, that
            // vendor behavior is to simply enqueue the task at the tail.
            _soTaskQueue.add(task);
         }
         else
         {
            // TODO: implement task prioritized queuing here
            _soTaskQueue.add(task);
         }
      }
      else
      {
         _soTaskQueue.add(task);
      }
      _soTaskState = attribute;
      _taskAdded.notify();
   }
   
   private void _addHeadOfQueue(Task task)
   {
      _headOfQueue.add(0, task);
      _taskAdded.notify();
   }
}
