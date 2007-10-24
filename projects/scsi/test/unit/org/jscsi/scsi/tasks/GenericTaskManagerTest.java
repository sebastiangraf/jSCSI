package org.jscsi.scsi.tasks;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.jscsi.scsi.exceptions.TaskSetException;
import org.jscsi.scsi.lu.GenericTaskManager;
import org.junit.Test;

// TODO: Describe class or interface
public class GenericTaskManagerTest extends TaskManagerTest
{
   
   @Test
   public void testStaticInsertion_HSO() throws InterruptedException
   {
      DOMConfigurator.configure(System.getProperty("log4j.configuration"));
      
      List<TestTask> taskSet = new ArrayList<TestTask>();
      new HeadOfQueueTask(taskSet, 0);
      new SimpleTask(taskSet, 0);
      Task last = new OrderedTask(taskSet, 100);
      
      GenericTaskManager manager = new GenericTaskManager(1);  // TODO: Should we test with more threads?
      
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
      
      synchronized (last)
      {
         last.wait(10000);
      }
      
      manager.shutdown();
      thread.join();
      
   }
   
   
}


