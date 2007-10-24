package org.jscsi.scsi.tasks;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.jscsi.scsi.exceptions.TaskSetException;
import org.jscsi.scsi.lu.GenericTaskManager;
import org.junit.Test;

public class GenericTaskManagerTest extends TaskManagerTest
{
   
   @Test
   public void testStaticInsertion_HSO() throws InterruptedException
   {
      DOMConfigurator.configure(System.getProperty("log4j.configuration"));
      
      List<TestTask> taskSet = new ArrayList<TestTask>();
      new HeadOfQueueTask(taskSet, 0);
      new SimpleTask(taskSet, 0);
      new OrderedTask(taskSet, 100);
      
      GenericTaskManager manager = new GenericTaskManager(1); // 10 threads is an arbitrary value here
      
      for ( Task t : taskSet )
      {
         try
         {
            manager.submitTask(t);
         }
         catch (TaskSetException e)
         {
            e.printStackTrace();
            fail("Task set exception thrown by task manager");
         }
      }
      
      Thread thread = new Thread(manager);
      thread.start();
      
      Task last = taskSet.get(taskSet.size()-1);
      
      synchronized ( last )
      {
         last.wait(10000);
      }
      
      manager.shutdown();
      thread.join();
      
      for ( int i = 0; i < taskSet.size(); i++ )
      {
         TestTask t = taskSet.get(i);
         if ( !t.isDone() )
         {
            fail("Task " + i + " not executed: " + t.getClass().getName() );
         }
         else
         {
            assertTrue( 
                  "Task " + i + " failed: " + t.reason() + ": " + t.getClass().getName(),
                  t.isProper() );
         }  
      }  
   }
}
