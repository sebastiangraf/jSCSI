package org.jscsi.scsi.lu;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jscsi.scsi.exceptions.TaskSetException;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskAttribute;
import org.jscsi.scsi.tasks.TaskManager;
import org.jscsi.scsi.tasks.TaskSet;

public class GenericTaskManager implements TaskManager, TaskSet
{
   
   private static Logger _logger = Logger.getLogger(TaskSet.class);
   
   ////////////////////////////////////////////////////////////////////////////
   // TaskManager members
   private ExecutorService _executor;
   private boolean _running = false;
   
   
   ////////////////////////////////////////////////////////////////////////////
   // TaskSet members
   private List<Task> _headOfQueue; // Queue of Head of Queue tasks
   private List<Task> _soTaskQueue; // Simple/Ordered (SO) Task Queue
   private TaskAttribute _soTaskState = null; // Simple/Ordered Task Queue state
   private Map<Integer,Integer> _simplePriorityMap;
   private Object _taskAdded;
   
   
   /////////////////////////////////////////////////////////////////////////////
   // constructor(s)
   
   public GenericTaskManager(int numThreads)
   {
      _executor = Executors.newFixedThreadPool(numThreads);
      
      _headOfQueue = Collections.synchronizedList(new LinkedList<Task>());
      _soTaskQueue = Collections.synchronizedList(new LinkedList<Task>());
      _simplePriorityMap = Collections.synchronizedMap(new HashMap<Integer, Integer>());
      _taskAdded = new Object();
   }
   
   
   /////////////////////////////////////////////////////////////////////////////
   // TaskManager implementation
   
   public void run()
   {
      _running = true;
      Task nextTask;
      
      while (_running)
      {
         nextTask = null;
         try
         {
            nextTask = _popTask(1);
         }
         catch (InterruptedException e)
         {
            // TODO: wrap this exception and pass along to higher layer
         }
         if (nextTask != null)
         {
            _executor.submit(nextTask);
         }
      }
      _executor.shutdown();
   }
   
   public void shutdown()
   {
      _running = false;
   }
   

   ////////////////////////////////////////////////////////////////////////////
   // TaskSet implementation
   
   public void submitTask(Task task) throws TaskSetException
   {
      TaskAttribute attribute = task.getCommand().getTaskAttribute();
      if (attribute == TaskAttribute.HEAD_OF_QUEUE)
      {
         this._addHeadOfQueue(task);
      }
      else
      {
         this._addSOTask(task, attribute);
      }
      _logger.debug("TaskSet queued new task <object: " + task + ", attribute: " + attribute + ">");
   }
   
   
   ////////////////////////////////////////////////////////////////////////////
   // private TaskSet queue accessors
   
   /**
    * Non-blocking method for acquiring the next queued task.
    * 
    * 
    * @return Task
    */
   
   private Task _popTask(long timeout) throws InterruptedException
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
      {
         synchronized (_taskAdded)
         {
            _taskAdded.wait(timeout);
         }
      }

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
         int priority = task.getCommand().getTaskPriority();
         
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
      synchronized (_taskAdded)
      {
         _taskAdded.notify();
      }
   }
   
   private void _addHeadOfQueue(Task task)
   {
      _headOfQueue.add(0, task);
      synchronized ( _taskAdded )
      {
         _taskAdded.notify();
      }
   }
}
