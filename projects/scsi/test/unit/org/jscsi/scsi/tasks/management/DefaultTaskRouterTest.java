
package org.jscsi.scsi.tasks.management;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.jscsi.core.exceptions.NotImplementedException;
import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.lu.LogicalUnit;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.ReportLuns;
import org.jscsi.scsi.protocol.cdb.TestUnitReady;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.SenseData;
import org.jscsi.scsi.protocol.sense.SenseDataFactory;
import org.jscsi.scsi.protocol.sense.exceptions.LogicalUnitNotSupportedException;
import org.jscsi.scsi.target.Target;
import org.jscsi.scsi.tasks.TaskAttribute;
import org.jscsi.scsi.tasks.TaskRouter;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DefaultTaskRouterTest
{
   public TaskRouter getTaskRouterInstance()
   {
      return new DefaultTaskRouter();
   }

   public interface SuccessCallback
   {
      public void success();

      public void failure(String reason);
   }

   public static class TestTargetTransportPort implements TargetTransportPort
   {
      private Nexus expectedNexus;
      private Status expectedStatus;
      private ByteBuffer expectedSenseData;
      private SuccessCallback callback;
      private SenseDataFactory senseDataFactory;

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
         this.senseDataFactory = new SenseDataFactory();
      }

      public boolean readData(Nexus nexus, long commandReferenceNumber, ByteBuffer output)
      {
         return false;
      }

      public void removeTarget(String targetName) throws Exception
      {
      }

      public void terminateDataTransfer(Nexus nexus)
      {
      }

      public boolean writeData(Nexus nexus, long commandReferenceNumber, ByteBuffer input)
      {
         return false;
      }

      public void writeResponse(
            Nexus nexus,
            long commandReferenceNumber,
            Status status,
            ByteBuffer senseData)
      {
         if (!nexus.equals(this.expectedNexus))
         {
            this.callback.failure("Response nexus not equal to expected nexus");
            return;
         }
         if (status != this.expectedStatus)
         {
            this.callback.failure("Response status not equal to expected status");
            return;
         }

         try
         {
            SenseData expected = this.senseDataFactory.decode(this.expectedSenseData);
            SenseData actual = this.senseDataFactory.decode(senseData);

            if (expected.getKCQ() != actual.getKCQ())
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

      public void terminateDataTransfer(Nexus nexus, long commandReferenceNumber)
      {
         throw new NotImplementedException("transfer termination ability must be implemented");
      }

      public void registerTarget(Target target)
      {
         // TODO Auto-generated method stub
      }

   }

   public static Command getTestUnitReadyCommand(Nexus nexus)
   {
      return new Command(nexus, new TestUnitReady(), TaskAttribute.SIMPLE, 0, 0);
   }

   public static Command getReportLunsCommand(Nexus nexus)
   {
      return new Command(nexus, new ReportLuns(), TaskAttribute.SIMPLE, 0, 0);
   }

   public static class TestLogicalUnit implements LogicalUnit
   {
      private Nexus nexus;
      private SuccessCallback callback;

      public void nexusLost()
      {
      }

      public void start()
      {
      }

      public void stop()
      {
      }

      public TestLogicalUnit(
            String target,
            String initiator,
            long logicalUnitNumber,
            SuccessCallback callback)
      {
         this.nexus = new Nexus(target, initiator, logicalUnitNumber);
         this.callback = callback;
      }

      public TestLogicalUnit(Nexus nexus, SuccessCallback callback)
      {
         this.nexus = nexus;
         this.callback = callback;
      }

      public void enqueue(TargetTransportPort port, Command command)
      {
         if (!this.nexus.equals(command.getNexus()))
         {
            this.callback.success();
         }
         else
         {
            this.callback.failure("Enqueued command has improper nexus");
         }
      }

      public void setModePageRegistry(ModePageRegistry modePageRegistry)
      {
      }

      public TaskServiceResponse abortTask(Nexus nexus)
      {
         // TODO Auto-generated method stub
         return null;
      }

      public TaskServiceResponse abortTaskSet(Nexus nexus)
      {
         // TODO Auto-generated method stub
         return null;
      }

      public TaskServiceResponse clearTaskSet(Nexus nexus)
      {
         // TODO Auto-generated method stub
         return null;
      }

      public TaskServiceResponse reset()
      {
         // TODO Auto-generated method stub
         return null;
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

   @Test
   public void invalidLUNTest()
   {

      SuccessCallback transportResults = new SuccessCallback()
      {
         public void failure(String reason)
         {
            failure("Expected router to return invalid LUN error: " + reason);
         }

         public void success()
         {
         }
      };

      SuccessCallback luResults = new SuccessCallback()
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

      Nexus nexus = new Nexus("initA", "targetB", 100);

      TargetTransportPort ttp =
            new TestTargetTransportPort(nexus, Status.CHECK_CONDITION,
                  ByteBuffer.wrap((new LogicalUnitNotSupportedException()).encode()),
                  transportResults);

      LogicalUnit lu = new TestLogicalUnit(nexus, luResults);

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

      router.enqueue(ttp, DefaultTaskRouterTest.getTestUnitReadyCommand(nexus));
   }

   @Test
   public void validRandomLUNTest()
   {
      SuccessCallback transportResults = new SuccessCallback()
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

      SuccessCallback luResults = new SuccessCallback()
      {
         public void failure(String reason)
         {
            fail(reason);
         }

         public void success()
         {
         }
      };

      Nexus nexus = new Nexus("initA", "targetB", (new Random()).nextLong());

      TargetTransportPort ttp =
            new TestTargetTransportPort(nexus, Status.GOOD, null, transportResults);

      TaskRouter router = this.getTaskRouterInstance();

      try
      {
         router.registerLogicalUnit(0, new TestLogicalUnit("initA", "targetB", 0, luResults));
         router.registerLogicalUnit(nexus.getLogicalUnitNumber(), new TestLogicalUnit(nexus,
               luResults));
      }
      catch (Exception e)
      {
         e.printStackTrace();
         fail("Exception occurred during Logical Unit registration: " + e.getMessage());
      }

      router.enqueue(ttp, DefaultTaskRouterTest.getTestUnitReadyCommand(nexus));
   }

   @Test
   public void validDefaultLUNTest()
   {
      SuccessCallback transportResults = new SuccessCallback()
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

      SuccessCallback luResults = new SuccessCallback()
      {
         public void failure(String reason)
         {
            fail(reason);
         }

         public void success()
         {
         }
      };

      Nexus nexus = new Nexus("initA", "targetB", 0);

      TargetTransportPort ttp =
            new TestTargetTransportPort(nexus, Status.GOOD, null, transportResults);

      TaskRouter router = this.getTaskRouterInstance();

      try
      {
         router.registerLogicalUnit(nexus.getLogicalUnitNumber(), new TestLogicalUnit(nexus,
               luResults));
      }
      catch (Exception e)
      {
         e.printStackTrace();
         fail("Exception occurred during Logical Unit registration: " + e.getMessage());
      }

      router.enqueue(ttp, DefaultTaskRouterTest.getTestUnitReadyCommand(nexus));
   }

   // TODO: I_T nexus task test (need interface for target task executor first)
}
