package org.jscsi.scsi.tasks.management;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.sense.exceptions.OverlappedCommandsAttemptedException;
import org.jscsi.scsi.tasks.Status;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskAttribute;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;

/**
 * A SAM-2 task set implementation providing a single task set for all I_T nexuses.
 * <p>
 * Because this implementation tracks outstanding tasks solely by task tag it will not
 * provide reliable service for multiple initiator connections. Further, the ABORT TASK SET
 * task management function has the same results as the CLEAR TASK SET function.
 */
public class DefaultTaskSet implements TaskSet
{
   private static Logger _logger = Logger.getLogger(DefaultTaskSet.class);

   // Treated as a decrementing counter
   private int capacity;
   
   private final Lock lock = new ReentrantLock();
   private final Condition notEmpty = lock.newCondition();
   private final Condition notFull = lock.newCondition();

   // Task set members
   private final List<TaskContainer> queue;  // Dormant task queue
   private final Map<Long,TaskContainer> tasks; // Tag-to-Task map with 'null' key as the untagged task
   
   /*
    * The "tasks" map contains a map of task tags to currently live tasks. The map contains all
    * enabled and dormant tasks.
    * 
    * All tasks are wrapped in a "task container" which takes care of notifying this task set when
    * the task is completed. This notification (implemented using the finished() method) is
    * synchronous. The container object also provides a poll() method which the task set uses to
    * determine if the task is ready to be enabled. The task set will not return a task by
    * its poll() or take() until the task is unblocked.
    * 
    * The "queue" contains all dormant tasks. Tasks are removed from the queue before being
    * returned by poll() or take(). Tasks are removed from the task map once they are finished
    * executing.
    * 
    * Note that tagged and untagged tasks can be differentiated by an invalid task tag (-1) on
    * the nexus, indicating an I_T_L Nexus instead of an I_T_L_Q nexus. Untagged tasks are always
    * treated as SIMPLE tasks.
    */
   
   /**
    * Constructs a task set capable of enqueuing the indicated number of tasks.
    */
   public DefaultTaskSet(int capacity)
   {
      this.capacity = capacity;
      this.queue = new ArrayList<TaskContainer>(capacity);
      this.tasks = new HashMap<Long, TaskContainer>(capacity);
   }
   
   
   /**
    * Called by {@link TaskContainer#run()} when execution of the task is complete.
    */
   private void finished(Long taskTag)
   {
      lock.lock(); // task execution thread is finished now, so we don't check interrupts
      try
      {
         this.tasks.remove(taskTag); // 'null' task tag is the untagged task
         this.capacity++;
         this.notFull.signalAll();
      }
      finally
      {
         lock.unlock();
      }
   }
   
   
      
   /**
    * Used to encapsulate tasks. Notifies the task set when task execution is complete.
    * <p>
    * The task container is also used to determine when a task is ready to be enabled. The
    * constructor will examine the task set's tasks map and remember which tasks must be cleared
    * before this task can execute.
    */
   private class TaskContainer implements Task
   {
      
      private Task task;
      private List<Task> blockingTasks;
      
      /**
       * Creates a task container for the given task. Uses the current enabled list and task
       * queue to determine which tasks will block this task.
       * 
       * @param task The task this container will encapsulate.
       */
      public TaskContainer(Task task) throws InterruptedException
      {
         this.task = task;
         this.blockingTasks = this.findBlockingTasks();
      }

      public Command getCommand()
      {
         return this.task.getCommand();
      }

      public TargetTransportPort getTargetTransportPort()
      {
         return this.task.getTargetTransportPort();
      }

      public void run()
      {
         this.task.run();
         long taskTag = this.task.getCommand().getNexus().getTaskTag();
         finished( taskTag > -1 ? taskTag : null ); // untagged tasks have a Q value of -1 (invalid)
      }
      
      public boolean abort()
      {
         return this.task.abort();
      }

      @Override
      public String toString()
      {
         StringBuilder str = new StringBuilder();
         str.append("TaskContainer(")
            .append(this.task.toString())
            .append(",[");
         for ( Task t : this.blockingTasks )
         {
            str.append(t.toString()).append(",");
         }
         if ( this.blockingTasks.size() > 0 )
            str.deleteCharAt(str.length()-1);
         str.append("])");
         return str.toString();
      }

      /**
       * Waits until this task is no longer blocked. Blocking conditions are as specified in SAM-2.
       * 
       * @param timeout How long to wait before giving up, in units of <code>unit</code>.
       * @param unit A <code>TimeUnit</code> determining how to interpret the <code>timeout</code>
       *    parameter.
       * @return This task, or <code>null</code> if the specified waiting time elapsed.
       * @throws InterruptedException
       */
      public synchronized Task poll(long timeout, TimeUnit unit) throws InterruptedException
      {
         lock.lockInterruptibly();
         try
         {
            timeout = unit.toNanos(timeout);
            while ( this.blocked() )
            {
               if ( timeout > 0 )
               {
                  // "notFull" is notified whenever a task is finished. We wait on that before
                  // checking if this task is still blocked.
                  timeout = notFull.awaitNanos(timeout);
               }
               else
               {
                  return null; // a timeout occurred
               }
            }
            return this;
         }
         finally
         {
            lock.unlock();
         }
      }
      
      /**
       * @return True if this task is not ready to be executed. Results comply with SAM-2 task
       * management specifications.
       */
      private synchronized boolean blocked() throws InterruptedException
      {
         lock.lockInterruptibly();
         
         try
         {
            for ( Task t : this.blockingTasks )
            {
               if ( tasks.containsValue(t) )
               {
                  return true;
               }
            }
            return false;
         }
         finally
         {
            lock.unlock();
         }
      }
      
      /**
       * @return A list of tasks this task is blocking on.
       */
      private synchronized List<Task> findBlockingTasks() throws InterruptedException
      {
         lock.lockInterruptibly();
         
         try
         {
            List<Task> blockedTasks = new LinkedList<Task>();
            
            switch (this.task.getCommand().getTaskAttribute())
            {
               case SIMPLE:
                  // A task having the SIMPLE attribute shall not enter the enabled state until
                  // all older head of queue and older ordered tasks have ended.
                  for ( Task t : tasks.values() )
                  {
                     TaskAttribute attr = t.getCommand().getTaskAttribute();
                     if ( attr == TaskAttribute.HEAD_OF_QUEUE || attr == TaskAttribute.ORDERED )
                     {
                        blockedTasks.add(t);
                     }
                  }
                  break;
               case HEAD_OF_QUEUE:
                  // A task having the ORDERED attribute shall not enter the enabled state until
                  // all older tasks in the task set have ended.
                  blockedTasks.addAll(tasks.values());
                  break;
               case ORDERED:
                  // A task having the HEAD OF QUEUE attribute shall be accepted into the task
                  // set in the enabled state.
                  break;
               case ACA:
                  throw new RuntimeException("unsupported task attribute: ACA");
            }
            
            return blockedTasks;
         }
         finally
         {
            lock.unlock();
         }

      }
      
   }
   
   
   /////////////////////////////////////////////////////////////////////////////////////////////////
   
   /*
    * @see TaskSet#remove(Nexus)
    */
   public boolean remove(Nexus nexus)
         throws NoSuchElementException, InterruptedException, IllegalArgumentException
   {
      if ( nexus.getTaskTag() == -1 ) // invalid Q
         throw new IllegalArgumentException("must provide an I_T_L_Q nexus for abort");
      
      lock.lockInterruptibly();
      try
      {
         TaskContainer task = this.tasks.remove(nexus.getTaskTag());
         if ( task == null )
            throw new NoSuchElementException(
                  "Task with tag " + nexus.getTaskTag() + " not in task set");
         
         this.queue.remove(task); // removes the task from the queue if present
         this.capacity++;
         this.notFull.signalAll();
         
         return task.abort();
      }
      finally
      {
         lock.unlock();
      }
   }
   
   public void clear()
   {
      try
      {
         lock.lockInterruptibly();
      }
      catch (InterruptedException e)
      {
         // thread is shutting down, no reason to actually clear the task set.
         // TODO: Is the above statement okay?
         return;
      }
      
      try
      {
         for ( TaskContainer task : this.tasks.values() )
         {
            task.abort();
         }
         this.tasks.clear();
         this.queue.clear();
         this.capacity = this.queue.size();
         this.notFull.notifyAll();
      }
      finally
      {
         lock.unlock();
      }
   }
   
   /*
    * @see org.jscsi.scsi.tasks.management.TaskSet#clear(org.jscsi.scsi.transport.Nexus)
    */
   public void clear(Nexus nexus) throws InterruptedException, IllegalArgumentException
   {
      // We don't check for I_T_L nexus because it makes no difference to this implementation
      
      lock.lockInterruptibly();
      try
      {
         for ( TaskContainer task : this.tasks.values() )
         {
            task.abort();
         }
         this.tasks.clear();
         this.queue.clear();
         this.capacity = this.queue.size();
         this.notFull.notifyAll();
      }
      finally
      {
         lock.unlock();
      }
   }

   public void abort(Nexus nexus) throws InterruptedException, IllegalArgumentException
   {
      // NOTE: This implementation does not properly implement the ABORT TASK SET function.
      this.clear(nexus);
   }

   /////////////////////////////////////////////////////////////////////////////////////////////////
   



   /**
    * Attempts to insert the given task into the set. When an insertion failure is indicated
    * the caller must not attempt to take any additional action. I.e., the originating target
    * transport port is notified of a failure within the method. Thus, failure notifications
    * are for information purposes only.
    * <p>
    * The following conditions can cause an insertion failure:
    * <ol>
    *    <li>An additional untagged task added before the first untagged task has been cleared.</li>
    *    <li>A tagged task with a duplicate tag added (an overlapping command condition).</li>
    *    <li>The task set is full.</li>
    * </ol>
    * <p>
    * Normally if the task set if full the thread will block until the timeout has expired.
    * If timeout is set to zero or the after the timeout period the set is still full a
    * {@link Status#TASK_SET_FULL} status will be sent to the target transport port and 
    * <code>false</code> will be returned.
    * 
    * @param task The task to insert into the set.
    * @param timeout How long to wait before giving up, in units of <code>unit</code>.
    * @param unit A <code>TimeUnit</code> determining how to interpret the <code>timeout</code>
    *    parameter.
    */
   public boolean offer(Task task, long timeout, TimeUnit unit) throws InterruptedException
   {
      if (task == null)
         throw new NullPointerException("task set does not take null objects");
      
      lock.lockInterruptibly();
      
      try
      {
         // check capacity
         timeout = unit.toNanos(timeout);
         while (this.capacity <= 0)
         {
            if (timeout > 0)
            {
               timeout = this.notFull.awaitNanos(timeout);
            }
            else
            {
               // on timeout, we write TASK SET FULL to the transport port
               lock.unlock(); // we don't want to block on transport port operations
               Command command = task.getCommand();
               task.getTargetTransportPort().writeResponse(
                     command.getNexus(),
                     command.getCommandReferenceNumber(), 
                     Status.TASK_SET_FULL, 
                     null);
               return false;
            }
         }
         
         // Check that untagged tasks have the SIMPLE task attribute
         long taskTag = task.getCommand().getNexus().getTaskTag();
         if (task.getCommand().getTaskAttribute() != TaskAttribute.SIMPLE)
         {
            throw new RuntimeException("Transport layer should have set untagged task as SIMPLE");
         }
         
         // check for duplicate task tags
         if (this.tasks.containsKey(taskTag)) // 'null' key is the untagged task
         {
            // Note that we treat two untagged tasks as an overlapped command condition.
            // FIXME: Is treating overlapping untagged tasks in this way actually proper?
            lock.unlock(); // we don't want to block on transport port operations
            Command command = task.getCommand();
            task.getTargetTransportPort().writeResponse(
                  command.getNexus(),
                  command.getCommandReferenceNumber(), 
                  Status.CHECK_CONDITION,
                  ByteBuffer.wrap((new OverlappedCommandsAttemptedException(true)).encode()));
            return false;
         }
         
         // wrap task in a task container
         TaskContainer container = new TaskContainer(task);
         
         // add task to the queue and map
         this.tasks.put(taskTag > -1 ? taskTag : null, container); // -1 Q value is 'untagged'
         
         if ( task.getCommand().getTaskAttribute() == TaskAttribute.HEAD_OF_QUEUE )
         {
            this.queue.add(0, container);
         }
         else
         {
            this.queue.add(container);
         }
         
         this.capacity--;
         this.notEmpty.signalAll();
         return true;
         
      }
      finally
      {
         lock.unlock();
      }
   }

   /**
    * Attempts to insert the given task into the set. Equivalent to 
    * <code>offer(task, 0, TimeUnit.SECONDS)</code>. In other words, a <code>TASK SET FULL</code>
    * status will be immediately returned to the target transport port when the set is full.
    * <p>
    * @param task The task to insert into the set.
    * @returns True if the task was insertion to the queue; False if insertion failed or the
    *    thread was interrupted.
    */
   public boolean offer(Task task)
   {
      try
      {
         return this.offer(task, 0, TimeUnit.SECONDS);
      }
      catch (InterruptedException e)
      {
         return false;
      }
   }
   
   
   
   

   public boolean add(Task task)
   {
      if ( this.offer(task) )
      {
         return true;
      }
      else
      {
         throw new IllegalStateException("task set full");
      }
   }


   /**
    * Adds the specified element to this queue. This method deviates from the interface
    * specification in that it does not wait if the task set is full. This is because such waiting
    * would not be ideal.
    * <p>
    * Normally insertion failures can happen for a variety of reasons
    * (see {@link #offer(Task, long, TimeUnit)}). This method will not communicate a failure with
    * the caller. However, this is not always bad because the error will be written to the
    * target transport port in any case.
    */
   public void put(Task task) throws InterruptedException
   {
      this.offer(task);
   }


   /**
    * Retrieves and removes the task at the head of the queue. Blocks on both an empty set and
    * all blocking boundaries specified in SAM-2.
    * <p>
    * The maximum wait time is twice the timeout. This occurs because first we wait for the
    * set to be not empty, then we wait for the task to be unblocked.
    */
   public Task poll(long timeout, TimeUnit unit) throws InterruptedException
   {
      lock.lockInterruptibly();
      
      try
      {
         // wait for the set to be not empty
         timeout = unit.toNanos(timeout);
         while ( this.queue.size() == 0 )
         {
            if (timeout > 0)
            {
               // "notEmpty" is notified whenever a task is added to the set
               timeout = notEmpty.awaitNanos(timeout);
            }
            else
            {
               return null;
            }
         }
         
         // get the first task and wait for it to be unblocked.
         TaskContainer container = this.queue.get(0);
         if ( container.poll(timeout, unit) != null )
         {
            this.queue.remove(0);
            return container;
         }
         else
         {
            return null;
         }
      }
      finally
      {
         lock.unlock();
      }
   }


   public Task take() throws InterruptedException
   {
      Task task = null;
      while (task == null)
      {
         task = this.poll(10, TimeUnit.SECONDS);
      }
      return task;
   }


   /**
    * Removes all available elements from this task set. Any tasks that are blocked will not
    * be removed. Will cease draining if the thread is interrupted.
    */
   public int drainTo(Collection<? super Task> c)
   {
      if ( c == this )
         throw new IllegalArgumentException("cannot drain task set into itself");
      if ( c == null )
         throw new NullPointerException("target collection must not be null");
      
      int count = 0;
      while (true)
      {
         try
         {
            Task t = this.poll(0, TimeUnit.SECONDS);
            if ( t == null )
               break;
            else
               c.add(t);
         }
         catch (InterruptedException e)
         {
            break;
         }
      }
      return count;
   }


   public int drainTo(Collection<? super Task> c, int maxElements)
   {
      if ( c == this )
         throw new IllegalArgumentException("cannot drain task set into itself");
      if ( c == null )
         throw new NullPointerException("target collection must not be null");
      
      int count = 0;
      while (maxElements > 0)
      {
         try
         {
            Task t = this.poll(0, TimeUnit.SECONDS);
            if ( t == null )
               break;
            else
               c.add(t);
         }
         catch (InterruptedException e)
         {
            break;
         }
         maxElements--;
      }
      return count;
   }


   public int remainingCapacity()
   {
      // we don't lock here because remainingCapacity() is not guarunteed to be correct
      return this.capacity;
   }


   public boolean retainAll(Collection<?> c)
   {
      throw new UnsupportedOperationException();
   }


   public Task element()
   {
      Task t = this.peek();
      if ( t == null )
         throw new NoSuchElementException();
      return t;
   }


   public Task peek()
   {
      lock.lock();
      try
      {
         return this.queue.get(0);
      }
      finally
      {
         lock.unlock();
      }
   }


   public Task poll()
   {
      try
      {
         return this.take();
      }
      catch (InterruptedException e)
      {
         return null;
      }
   }


   public Task remove()
   {
      Task t = this.poll();
      if ( t == null )
         throw new NoSuchElementException();
      return t;
   }


   public boolean addAll(Collection<? extends Task> c)
   {
      lock.lock();
      
      try
      {
         for ( Task o : c )
         {
            if ( ! this.offer(o) )
               return false;
         }
         return true;
      }
      finally
      {
         lock.unlock();
      }
   }


   public boolean contains(Object o)
   {
      lock.lock();
      try
      {
         return this.queue.contains(o);
      }
      finally
      {
         lock.unlock();
      }
   }


   public boolean containsAll(Collection<?> c)
   {
      lock.lock();
      
      try
      {
         for ( Object o : c )
         {
            if ( ! this.queue.contains(o) )
               return false;
         }
         return true;
      }
      finally
      {
         lock.unlock();
      }

   }


   public boolean isEmpty()
   {
      lock.lock();
      try
      {
         return this.queue.size() != 0;
      }
      finally
      {
         lock.unlock();
      }
   }


   public Iterator<Task> iterator()
   {
      return new Iterator<Task>()
      {
         private Iterator<TaskContainer> it = queue.iterator();

         public boolean hasNext()
         {
            return it.hasNext();
         }

         public Task next()
         {
            return it.next();
         }

         public void remove()
         {
            throw new UnsupportedOperationException();
         }
      };
   }


   public boolean remove(Object o)
   {
      throw new UnsupportedOperationException();
   }


   public boolean removeAll(Collection<?> c)
   {
      throw new UnsupportedOperationException();
   }


   public int size()
   {
      return this.queue.size();
   }


   public Object[] toArray()
   {
      lock.lock();
      try
      {
         Object[] objs = new Object[this.queue.size()];
         for ( int i = 0; i < objs.length; i++ )
         {
            objs[i] = this.queue.get(i);
         }
         return objs;
      }
      finally
      {
         lock.unlock();
      }
   }


   @SuppressWarnings("unchecked")
   public <T> T[] toArray(T[] a)
   {
      if (!(a instanceof Task[]))
         throw new ArrayStoreException();
      
      Task[] dst = null;
      
      if ( a.length < this.queue.size() )
         dst = new Task[this.queue.size()];
      else
         dst = (Task[])a;
      
      for ( int i = 0; i < this.queue.size(); i++ )
      {
         dst[i] = this.queue.get(i);
      }
      
      if ( dst.length > this.queue.size() + 1 )
      {
         dst[this.queue.size()] = null;
      }
      
      return (T[])dst;
   }

   
   
   
   
   
   
   
   
}


