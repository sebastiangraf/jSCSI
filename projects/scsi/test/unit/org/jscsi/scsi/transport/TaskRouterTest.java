
package org.jscsi.scsi.transport;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jscsi.scsi.lu.LogicalUnit;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.ReportLuns;
import org.jscsi.scsi.protocol.cdb.TestUnitReady;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.SenseData;
import org.jscsi.scsi.protocol.sense.exceptions.LogicalUnitNotSupportedException;
import org.jscsi.scsi.target.Target;
import org.jscsi.scsi.tasks.Status;
import org.jscsi.scsi.tasks.TaskAttribute;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

// TODO: Describe class or interface
public abstract class TaskRouterTest
{
   
   public abstract TaskRouter getTaskRouterInstance();
   
   
   public interface SuccessCallback
   {
      public void success();
      
      public void failure(String reason);
   }
   
   public static class TestTargetTransportPort implements TargetTransportPort
   {
      private static Logger _logger = Logger.getLogger(TestTargetTransportPort.class);

      private Nexus expectedNexus;
      private Status expectedStatus;
      private ByteBuffer expectedSenseData;
      private SuccessCallback callback;
      
      
      public TestTargetTransportPort(
            Nexus expectedNexus,
            Status expectedStatus,
            ByteBuffer expectedSenseData,
            SuccessCallback callback)
      {
         this.expectedNexus = expectedNexus;
         this.expectedStatus = expectedStatus;
         this.expectedSenseData = expectedSenseData;
         this.callback = callback;
      }

      public boolean readData(Nexus nexus, ByteBuffer output) { return false; }
      public void registerTarget(Target target) {}
      public void removeTarget(String targetName) throws Exception {}
      public void terminateDataTransfer(Nexus nexus) {}
      public boolean writeData(Nexus nexus, ByteBuffer input) { return false; }
      

      public void writeResponse(Nexus nexus, Status status, ByteBuffer senseData)
      {
         if ( ! nexus.equals(this.expectedNexus) )
         {
            this.callback.failure("Response nexus not equal to expected nexus");
            return;
         }
         if ( status != this.expectedStatus )
         {
            this.callback.failure("Response status not equal to expected status");
            return;
         }
         
         try
         {
            SenseData expected = SenseData.decode(this.expectedSenseData);
            SenseData actual = SenseData.decode(senseData);
            
            if ( expected.getKCQ() != actual.getKCQ() )
            {
               this.callback.failure("Response sense data contains unexpected KCQ");
               return;
            }
            
         }
         catch (BufferUnderflowException e)
         {
            this.callback.failure("Could not decode sense data: " + e.getMessage());
            return;
         }
         catch (IOException e)
         {
            this.callback.failure("Could not decode sense data: " + e.getMessage());
            return;
         }
         
         this.callback.success();
      }
      
   }
   
   public static Command getTestUnitReadyCommand( Nexus nexus )
   {
      return new Command( nexus, new TestUnitReady(), TaskAttribute.SIMPLE, 0, 0 );
   }
   
   public static Command getReportLunsCommand( Nexus nexus )
   {
      return new Command( nexus, new ReportLuns(), TaskAttribute.SIMPLE, 0, 0 );
   }
   
   
   public static class TestLogicalUnit implements LogicalUnit
   {
      private Nexus nexus;
      private SuccessCallback callback;
      

      public TestLogicalUnit(
            String target,
            String initiator,
            long logicalUnitNumber,
            SuccessCallback callback )
      {
         this.nexus = new Nexus( target, initiator, logicalUnitNumber );
         this.callback = callback;
      }
     
      public TestLogicalUnit(
            Nexus nexus,
            SuccessCallback callback)
      {
         this.nexus = nexus;
         this.callback = callback;
      }

      public void enqueue(TargetTransportPort port, Command command)
      {
         if ( !this.nexus.equals(command.getNexus()) )
         {
            this.callback.success();
         }
         else
         {
            this.callback.failure("Enqueued command has improper nexus");
         }
      }

      public void setModePageRegistry(ModePageRegistry modePageRegistry) {}
      
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
   
   @Test
   public void invalidLUNTest()
   {
      
      SuccessCallback transportResults = 
         new SuccessCallback()
         {
            public void failure(String reason)
            {
               failure("Expected router to return invalid LUN error: " + reason);
            }

            public void success() {}
         };
         
      SuccessCallback luResults =
         new SuccessCallback()
         {
            public void failure(String reason)
            {
               fail("Received results in invalid logical unit");
            }
            
            public void success()
            {
               fail("Received results in invalid logical unit");
            }
         };
      
      Nexus nexus = new Nexus( "initA", "targetB", 100 );
         
      TargetTransportPort ttp =
         new TestTargetTransportPort( 
               nexus,
               Status.CHECK_CONDITION,
               (new LogicalUnitNotSupportedException()).encode(),
               transportResults );
      
      LogicalUnit lu = new TestLogicalUnit( nexus, luResults );
      
      TaskRouter router = this.getTaskRouterInstance();
      
      try
      {
         router.registerLogicalUnit(nexus.getLogicalUnitNumber(), lu);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         fail("Exception occurred during Logical Unit registration: " + e.getMessage());
      }
      
      router.enqueue(ttp, TaskRouterTest.getTestUnitReadyCommand(nexus));
   }
   
   @Test
   public void validRandomLUNTest()
   {
      SuccessCallback transportResults =
         new SuccessCallback()
         {
            public void failure(String reason)
            {
               fail("Received invalid response from Task Router");
            }
            
            public void success()
            {
               fail("Received invalid response from Task Router");
            }
         };
         
      SuccessCallback luResults =
         new SuccessCallback()
         {
            public void failure(String reason)
            {
               fail(reason);
            }
            
            public void success() {}
         };
      
      Nexus nexus = new Nexus( "initA", "targetB", (new Random()).nextLong() );
      
      TargetTransportPort ttp =
         new TestTargetTransportPort(
               nexus,
               Status.GOOD,
               null,
               transportResults );
      
      TaskRouter router = this.getTaskRouterInstance();
      
      try
      {
         router.registerLogicalUnit(0, new TestLogicalUnit("initA", "targetB", 0, luResults));
         router.registerLogicalUnit( 
               nexus.getLogicalUnitNumber(), new TestLogicalUnit(nexus, luResults) );
      }
      catch (Exception e)
      {
         e.printStackTrace();
         fail("Exception occurred during Logical Unit registration: " + e.getMessage());
      }
      
      router.enqueue(ttp, TaskRouterTest.getTestUnitReadyCommand(nexus));
   }
   
   @Test
   public void validDefaultLUNTest()
   {
      SuccessCallback transportResults =
         new SuccessCallback()
         {
            public void failure(String reason)
            {
               fail("Received invalid response from Task Router");
            }
            
            public void success()
            {
               fail("Received invalid response from Task Router");
            }
         };
         
      SuccessCallback luResults =
         new SuccessCallback()
         {
            public void failure(String reason)
            {
               fail(reason);
            }
            
            public void success() {}
         };
         
         Nexus nexus = new Nexus( "initA", "targetB", 0 );
         
         TargetTransportPort ttp =
            new TestTargetTransportPort(
                  nexus,
                  Status.GOOD,
                  null,
                  transportResults );
         
         TaskRouter router = this.getTaskRouterInstance();
         
         try
         {
            router.registerLogicalUnit( 
                  nexus.getLogicalUnitNumber(), new TestLogicalUnit(nexus, luResults) );
         }
         catch (Exception e)
         {
            e.printStackTrace();
            fail("Exception occurred during Logical Unit registration: " + e.getMessage());
         }
         
         router.enqueue(ttp, TaskRouterTest.getTestUnitReadyCommand(nexus));
   }
   
   // TODO: I_T nexus task test (need interface for target task executor first)
   
   
   
   

}


