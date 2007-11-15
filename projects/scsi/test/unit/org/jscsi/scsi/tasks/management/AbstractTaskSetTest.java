package org.jscsi.scsi.tasks.management;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.target.Target;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskAttribute;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests task manager implementations for proper execution ordering.
 */
public class AbstractTaskSetTest
{
   static
   {
      BasicConfigurator.configure();
   }

   public static abstract class TestTask implements Task
   {
      private static Logger _logger = Logger.getLogger(TestTask.class);
      
      private Command command;
      private TargetTransportPort port;
      private long delay;
      private Boolean done = false;
      private Thread thread;
      
      public TestTask( Nexus nexus, TaskAttribute attribute, long delay )
      {
         this.port = null;
         this.delay = delay;
         this.command = new Command( nexus, (CDB)null, attribute, 0, 0 );
         _logger.debug("constructed TestTask: " + this);
      }
      
      public TestTask( TargetTransportPort port, Nexus nexus, TaskAttribute attribute, long delay )
      {
         this.port = port;
         this.delay = delay;
         this.command = new Command( nexus, (CDB)null, attribute, 0, 0 );
         _logger.debug("constructed TestTask: " + this);
      }
      
      /**
       * Returns <code>true</code> if the task has finished executing; <code>false</code>
       * otherwise.
       */
      public boolean isDone()
      {
         synchronized ( this )
         {
            return this.done;
         }
      }
      
      
      
      public boolean abort()
      {
         if ( this.thread == null )
         {
            return false;
         }
         else
         {
            thread.interrupt();
            return true;
         } 
      }

      /**
       * Returns <code>true</code> if the task was executed in the proper order;
       * <code>false</code> otherwise.
       */
      public abstract boolean isProper();
      
      /**
       * Returns reason for improper execution.
       */
      public abstract String reason();
      
      /**
       * Checks for proper execution in a static task set. If tasks are added to a Task Manager's
       * queue set after execution has begun this method may cause improper results to be returned
       * from {@link #isProper()}.
       * 
       */
      protected abstract void checkProperExecution();
      
      /**
       * Resets task completion state.
       */
      public void reset()
      {
         this.done = false;
      }
      
      
      public void run()
      {
         assert this.done == false: "This task has already been executed!";
         
         this.thread = Thread.currentThread();

         _logger.debug("executing task: " + this);
         this.checkProperExecution();
         
         try
         {
            _logger.debug("sleeping for " + this.delay + ": " + this);
            Thread.sleep(this.delay);
            _logger.debug("done sleeping: " + this);
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
         
         
         synchronized ( this )
         {
            _logger.debug("marking task as done: " + this);
            this.done = true;
            this.notifyAll();
         }
         _logger.debug("leaving run method: " + this);
      }
      
      public Command getCommand()
      {
         return this.command;
      }

      public TargetTransportPort getTargetTransportPort()
      {
         return this.port;
      }      

      public InquiryDataRegistry getInquiryDataRegistry()
      {
         // TODO Auto-generated method stub
         return null;
      }

      public ModePageRegistry getModePageRegistry()
      {
         // TODO Auto-generated method stub
         return null;
      }
   }
   
   public static class SimpleTask extends TestTask
   {
      private static Logger _logger = Logger.getLogger(HeadOfQueueTask.class);
      
      private List<TestTask> taskSet;
      private int index;
      private Boolean properStart = false;
      private String reason;
      
      public SimpleTask( Nexus nexus, List<TestTask> taskSet, long delay )
      {
         this(null, nexus, taskSet, delay);
      }
      
      public SimpleTask( TargetTransportPort port, Nexus nexus, List<TestTask> taskSet, long delay )
      {
         super(port, nexus, TaskAttribute.SIMPLE, delay);
         
         synchronized ( taskSet )
         {
            this.index = taskSet.size();
            taskSet.add(this);
         }
         this.taskSet = taskSet;
      }
      
      @Override
      protected void checkProperExecution()
      {
         _logger.debug("Checking for proper execution order: " + this);
         synchronized ( this.taskSet )
         {
            this.properStart = true;
            for ( int i = 0; i < this.index; i++ )
            {
               TestTask t = this.taskSet.get(i);
               if ( (t instanceof HeadOfQueueTask) && (! t.isDone()) )
               {
                  this.properStart = false;
                  this.reason = "Previously inserted Head Of Queue Task not finished";
               }
               else if ( (t instanceof OrderedTask) && (! t.isDone()) )
               {
                  this.properStart = false;
                  this.reason = "Previously inserted Ordered Task not finished";
               }
            }
            for ( int i = this.index + 1; i < this.taskSet.size(); i++ )
            {
               TestTask t = this.taskSet.get(i);
               if ( (t instanceof HeadOfQueueTask) && (! t.isDone()) )
               {
                  this.properStart = false;
                  this.reason = "Later inserted Head Of Queue Task not finished";
               }
               else if ( (t instanceof OrderedTask) && t.isDone() )
               {
                  this.properStart = false;
                  this.reason = "Later inserted Ordered Task preemptively finished";
               }
            }
         }  
      }

      public boolean isProper()
      {
         return this.properStart;
      }
      
      public String reason()
      {
         return this.reason;
      }

      @Override
      public void reset()
      {
         super.reset();
         this.properStart = false;
      }      
      
      @Override
      public String toString()
      {
         long tag = this.getCommand().getNexus().getTaskTag();
         return "SimpleTask(tag=" + tag + ")";
      }

   }
   
   
   public static class HeadOfQueueTask extends TestTask
   {

      private static Logger _logger = Logger.getLogger(HeadOfQueueTask.class);
      
      private List<TestTask> taskSet;
      private int index;
      private boolean properStart = true;
      private String reason = "Unknown reason";
      
      
      public HeadOfQueueTask( Nexus nexus, List<TestTask> taskSet, long delay )
      {
         super(nexus, TaskAttribute.HEAD_OF_QUEUE, delay);
         
         synchronized ( taskSet )
         {
            this.index = taskSet.size();
            taskSet.add(this);
         }
         this.taskSet = taskSet;
      }

      public boolean isProper()
      {
         return this.properStart;
      }
      
      @Override
      protected void checkProperExecution()
      {
         _logger.debug("Checking for proper execution order: " + this);
         synchronized ( this.taskSet )
         {

            this.properStart = true;

            for ( int i = 0; i < this.index; i++ )
            {
               TestTask t = this.taskSet.get(i);
               if ( !(t instanceof HeadOfQueueTask) && t.isDone() )
               {
                  this.properStart = false;
                  this.reason = "Previously inserted task preemptively finished";
               }
            }
            for ( int i = this.index + 1; i < this.taskSet.size(); i++ )
            {
               TestTask t = this.taskSet.get(i);
               if ( !(t instanceof HeadOfQueueTask) && t.isDone() )
               {
                  this.properStart = false;
                  this.reason = "Later inserted task preemptively finished";
                  
               }
            }
         }
         if (!this.properStart)
         {
            _logger.error("Task not started properly");
         }
      }
      
      public String reason()
      {
         synchronized (this.taskSet)
         {
            return this.reason;
         }
      }
      
      @Override
      public void reset()
      {
         super.reset();
         this.properStart = false;
      }

      @Override
      public String toString()
      {
         long tag = this.getCommand().getNexus().getTaskTag();
         return "HeadOfQueueTask(tag=" + tag + ")";
      }
      
      
      
   }
   
   public static class OrderedTask extends TestTask
   {
      private static Logger _logger = Logger.getLogger(HeadOfQueueTask.class);
      
      private List<TestTask> taskSet;
      private int index;
      private Boolean properStart = false;
      private String reason;
      
      public OrderedTask( Nexus nexus, List<TestTask> taskSet, long delay )
      {
         super(nexus, TaskAttribute.ORDERED, delay);
         
         synchronized ( taskSet )
         {
            this.index = taskSet.size();
            taskSet.add(this);
         }
         this.taskSet = taskSet;
      }

      @Override
      protected void checkProperExecution()
      {
         _logger.debug("Checking for proper execution order: " + this);
         synchronized ( this.taskSet )
         {
            this.properStart = true;
            for ( int i = 0; i < this.index; i++ )
            {
               TestTask t = this.taskSet.get(i);
               if ( ! t.isDone() )
               {
                  this.properStart = false;
                  this.reason = "Previously inserted Task not finished";
               }
            }
            for ( int i = this.index + 1; i < this.taskSet.size(); i++ )
            {
               TestTask t = this.taskSet.get(i);
               if ( (t instanceof HeadOfQueueTask) && (! t.isDone()) )
               {
                  this.properStart = false;
                  this.reason = "Later inserted Head Of Queue Task not finished";
               }
               else if ( !(t instanceof HeadOfQueueTask) && t.isDone() )
               {
                  this.properStart = false;
                  this.reason = "Later inserted Task preemptively finished";
               }
            }
         }
      }

      @Override
      public boolean isProper()
      {
         return this.properStart;
      }
      
      public String reason()
      {
         return this.reason;
      }
      
      @Override
      public void reset()
      {
         super.reset();
         this.properStart = false;
      }
      
      @Override
      public String toString()
      {
         long tag = this.getCommand().getNexus().getTaskTag();
         return "OrderedTask(tag=" + tag + ")";
      }
   }

   
   public static class TestTargetTransportPort implements TargetTransportPort
   {
      private boolean fail;
      private ByteBuffer data;
      private ByteBuffer sense;
      private Status status;
      
      public TestTargetTransportPort(ByteBuffer data, boolean failTransfer)
      {
         this.data = data;
      }
      
      public ByteBuffer getTransferData() { return data; }
      public ByteBuffer getSenseData() { return sense; }
      public Status getLastStatus() { return status; }

      
      public void registerTarget(Target target) {}
      public void removeTarget(String targetName) throws Exception {}
      public void terminateDataTransfer(Nexus nexus, long commandReferenceNumber) {}

      public boolean readData(Nexus nexus, long commandReferenceNumber, ByteBuffer output)
            throws InterruptedException
      {
         if (fail)
            return false;
         
         output.put(data);
         return true;
      }
      
      public boolean writeData(Nexus nexus, long commandReferenceNumber, ByteBuffer input)
            throws InterruptedException
      {
         this.data = ByteBuffer.allocate(input.limit());
         this.data.put(input);
         this.data.position(0);
         
         return ! fail;
      }

      public void writeResponse(
            Nexus nexus,
            long commandReferenceNumber,
            Status status,
            ByteBuffer senseData)
      {
         this.status = status;
         if ( senseData != null )
         {
            this.sense = ByteBuffer.allocate(senseData.limit());
            this.sense.put(senseData);
            this.sense.position(0);
         }
      }
      
   }
   
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
   }

   @Before
   public void setUp() throws Exception
   {
   }

   @After
   public void tearDown() throws Exception
   {
   }
      
   /*
    * Below we test the TestTask classes for detection capability. The following
    * table shows insertion orders and execution orders on those sets. Those execution
    * orders which are incorrect are marked with 'Failure'.
    * 
    * H - Head of Queue Tasks
    * O - Ordered Tasks
    * S - Simple Tasks
    * 
    *    Insertion    Execution    Result
    *    -----------  -----------  -----------
    *     H, 0         H, O
    *                  O, H         Failure
    *    -----------  -----------  -----------
    *     H, S         H, S
    *                  S, H         Failure
    *    -----------  -----------  -----------
    *     O, H         O, H         Failure
    *                  H, O
    *    -----------  -----------  -----------
    *     O, S         O, S
    *                  S, O         Failure
    *    -----------  -----------  -----------
    *     S, H         S, H         Failure
    *                  H, S 
    *    -----------  -----------  -----------
    *     S, O         S, O
    *                  O, S         Failure
    *    -----------  -----------  -----------
    *     H[1], H[2]   [1], [2]     Failure
    *                  [2], [1]     
    *    -----------  -----------  -----------
    *     S[1], S[2]   [1], [2]
    *                  [2], [1]
    *    -----------  -----------  -----------
    *     O[1], O[2]   [1], [2]
    *                  [2], [1]     Failure
    *    -----------  -----------  -----------
    */
   
   /**
    * @param first A first task.
    * @param second A second task which is part of the same task set as the first task.
    * @param failForward True if execution in order should fail.
    * @param failReverse True if execution in reverse should fail.
    */
   private void internalBinaryTest(
         TestTask first,
         TestTask second,
         boolean failForward,
         boolean failReverse )
   {
      first.run();
      second.run();
      
      if ( failForward )
      {
         if ( first.isProper() && second.isProper() )
         {
            fail("Both tasks executed properly; expected failure");
         }
      }
      else
      {
         assertTrue( "First task executed improperly", first.isProper()  );
         assertTrue( "Second task executed improperly", second.isProper() );
      }
      
      first.reset();
      second.reset();
      
      second.run();
      first.run();
      
      if ( failReverse )
      {
         if ( first.isProper() && second.isProper() )
         {
            fail("Both tasks executed properly; expected failure");
         }
      }
      else
      {
         assertTrue( "First task executed improperly", first.isProper()  );
         assertTrue( "Second task executed improperly", second.isProper() );
      }
      
      
   }
   
   @Test
   public void internalTest_HO()
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      Nexus nexus = new Nexus("initiator", "target", 0);
      
      internalBinaryTest(
            new HeadOfQueueTask(new Nexus(nexus, 0), taskSet, 0),
            new OrderedTask(new Nexus(nexus, 1), taskSet, 0),
            false,
            true );
   }
   
   @Test
   public void internalTest_HS()
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      Nexus nexus = new Nexus("initiator", "target", 0);
      
      internalBinaryTest(
            new HeadOfQueueTask(new Nexus(nexus, 0), taskSet, 0),
            new SimpleTask(new Nexus(nexus, 1), taskSet, 0),
            false,
            true );
   }
   
   @Test
   public void internalTest_OH()
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      Nexus nexus = new Nexus("initiator", "target", 0);
      
      internalBinaryTest(
            new OrderedTask(new Nexus(nexus, 0), taskSet, 0),
            new HeadOfQueueTask(new Nexus(nexus, 1), taskSet, 0),
            true,
            false );
   }
   
   @Test
   public void internalTest_OS()
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      Nexus nexus = new Nexus("initiator", "target", 0);
      
      internalBinaryTest(
            new OrderedTask(new Nexus(nexus, 0), taskSet, 0),
            new SimpleTask(new Nexus(nexus, 1), taskSet, 0),
            false,
            true );
   }
   
   @Test
   public void internalTest_SH()
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      Nexus nexus = new Nexus("initiator", "target", 0);
      
      internalBinaryTest(
            new SimpleTask(new Nexus(nexus, 0), taskSet, 0),
            new HeadOfQueueTask(new Nexus(nexus, 1), taskSet, 0),
            true,
            false );
   }
   
   @Test
   public void internalTest_SO()
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      Nexus nexus = new Nexus("initiator", "target", 0);
      
      internalBinaryTest(
            new SimpleTask(new Nexus(nexus, 0), taskSet, 0),
            new OrderedTask(new Nexus(nexus, 1), taskSet, 0),
            false,
            true );
   }
   
   @Test
   public void internalTest_H1H2()
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      Nexus nexus = new Nexus("initiator", "target", 0);
      
      internalBinaryTest(
            new HeadOfQueueTask(new Nexus(nexus, 0), taskSet, 0),
            new HeadOfQueueTask(new Nexus(nexus, 1), taskSet, 0),
            false,
            false );
   }
   
   @Test
   public void internalTest_S1S2()
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      Nexus nexus = new Nexus("initiator", "target", 0);
      
      internalBinaryTest(
            new SimpleTask(new Nexus(nexus, 0), taskSet, 0),
            new SimpleTask(new Nexus(nexus, 1), taskSet, 0),
            false,
            false );
   }
   
   @Test
   public void internalTest_O1O2()
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      Nexus nexus = new Nexus("initiator", "target", 0);
      
      internalBinaryTest(
            new OrderedTask(new Nexus(nexus, 0), taskSet, 0),
            new OrderedTask(new Nexus(nexus, 1), taskSet, 0),
            false,
            true );
   }
}
