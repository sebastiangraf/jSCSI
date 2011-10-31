/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
//Cleversafe open-source code header - Version 1.1 - December 1, 2006
//
//Cleversafe Dispersed Storage(TM) is software for secure, private and
//reliable storage of the world's data using information dispersal.
//
//Copyright (C) 2005-2007 Cleversafe, Inc.
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
//USA.
//
//Contact Information: 
// Cleversafe, 10 W. 35th Street, 16th Floor #84,
// Chicago IL 60616
// email: licensing@cleversafe.org
//
//END-OF-HEADER
//-----------------------
//@author: John Quigley <jquigley@cleversafe.com>
//@date: January 1, 2008
//---------------------

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

}
