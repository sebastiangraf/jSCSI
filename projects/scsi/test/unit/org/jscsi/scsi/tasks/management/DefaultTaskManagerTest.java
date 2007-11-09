package org.jscsi.scsi.tasks.management;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.jscsi.scsi.exceptions.TaskSetException;
import org.jscsi.scsi.tasks.Task;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DefaultTaskManagerTest extends GeneralTaskManagerTest
{
   
   public static final int MANAGER_THREAD_COUNT = 1; // 1 thread is an arbitrary value
   
   
   @Before
   public void setUp() throws Exception
   {
      DOMConfigurator.configure(System.getProperty("log4j.configuration"));
   }

   @After
   public void tearDown() throws Exception
   {
   }

   
   private static void executeTaskSet( List<TestTask> taskSetList ) throws InterruptedException
   {
      TaskSet taskSet = new DefaultTaskSet(16);
      DefaultTaskManager manager = new DefaultTaskManager(MANAGER_THREAD_COUNT, taskSet);
      
      for ( Task t : taskSetList )
      {
         taskSet.put(t);
      }
      
      Thread thread = new Thread(manager);
      thread.start();
      
      Task last = taskSetList.get(taskSet.size()-1);
      
      synchronized ( last )
      {
         //last.wait(10000);
         last.wait();
      }
      
      manager.shutdown();
      thread.join();
      
   }
   
   private static void checkTaskSet( List<TestTask> taskSet )
   {
      for ( int i = 0; i < taskSet.size(); i++ )
      {
         TestTask t = taskSet.get(i);

         if ( !t.isDone() )
         {
            fail("Task " + i + " not executed: " + t.getClass().getName() );
         }
         else
         {
            assertTrue("Task " + i + " failed (" + t.isProper() + "): " + t.reason() + ": " + t.getClass().getName(), t.isProper() );
         }
      }
   }
   
   
   @Test
   public void testStaticInsertion_HSO() throws InterruptedException
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      new HeadOfQueueTask(taskSet, 0);
      new SimpleTask(taskSet, 0);
      new OrderedTask(taskSet, 100);
      
      
      executeTaskSet( taskSet );
      checkTaskSet( taskSet );
   }

   @Test
   public void testStaticInsertion_HSSO() throws InterruptedException
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      new HeadOfQueueTask(taskSet, 0);
      new SimpleTask(taskSet, 0);
      new SimpleTask(taskSet, 0);
      new OrderedTask(taskSet, 100);
      
      executeTaskSet( taskSet );
      checkTaskSet( taskSet );
   }
   
   @Test
   public void testStaticInsertion_HOOS() throws InterruptedException
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      new HeadOfQueueTask(taskSet, 0);
      new OrderedTask(taskSet, 0);
      new OrderedTask(taskSet, 0);
      new SimpleTask(taskSet, 100);
      
      executeTaskSet( taskSet );
      checkTaskSet( taskSet );
   }
   
   @Test
   public void testStaticInsertion_HHSS() throws InterruptedException
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      new HeadOfQueueTask(taskSet, 0);
      new HeadOfQueueTask(taskSet, 0);
      new SimpleTask(taskSet, 0);
      new SimpleTask(taskSet, 100);
      
      executeTaskSet( taskSet );
      checkTaskSet( taskSet );
   }
   
   @Test
   public void testStaticInsertion_SOSSOHH() throws InterruptedException
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      new SimpleTask(taskSet, 0);
      new OrderedTask(taskSet, 0);
      new SimpleTask(taskSet, 0);
      new SimpleTask(taskSet, 0);
      new OrderedTask(taskSet, 0);
      new HeadOfQueueTask(taskSet, 0);
      new HeadOfQueueTask(taskSet, 100);
      
      executeTaskSet( taskSet );
      checkTaskSet( taskSet );
   }
   
   @Test
   public void testStaticInsertion_OSOOHH() throws InterruptedException
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      new OrderedTask(taskSet, 0);
      new SimpleTask(taskSet, 0);
      new OrderedTask(taskSet, 0);
      new OrderedTask(taskSet, 0);
      new HeadOfQueueTask(taskSet, 0);
      new HeadOfQueueTask(taskSet, 100);
      
      executeTaskSet( taskSet );
      checkTaskSet( taskSet );
   }
   
   @Test
   public void testStaticInsertion_SOSSO() throws InterruptedException
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      new SimpleTask(taskSet, 0);
      new OrderedTask(taskSet, 0);
      new SimpleTask(taskSet, 0);
      new SimpleTask(taskSet, 0);
      new OrderedTask(taskSet, 100);
      
      executeTaskSet( taskSet );
      checkTaskSet( taskSet );
   }
   
   @Test
   public void testStaticInsertion_OSSO() throws InterruptedException
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      new OrderedTask(taskSet, 0);
      new SimpleTask(taskSet, 0);
      new SimpleTask(taskSet, 0);
      new OrderedTask(taskSet, 100);
      
      executeTaskSet( taskSet );
      checkTaskSet( taskSet );
   }
   
   @Test
   public void testStaticInsertion_SSO() throws InterruptedException
   {
      List<TestTask> taskSet = new ArrayList<TestTask>();
      new SimpleTask(taskSet, 0);
      new SimpleTask(taskSet, 0);
      new OrderedTask(taskSet, 100);
      
      executeTaskSet( taskSet );
      checkTaskSet( taskSet );
   }
}
