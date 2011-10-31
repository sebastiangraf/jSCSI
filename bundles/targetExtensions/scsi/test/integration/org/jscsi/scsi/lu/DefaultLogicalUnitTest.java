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

package org.jscsi.scsi.lu;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.Read10;
import org.jscsi.scsi.protocol.cdb.Read12;
import org.jscsi.scsi.protocol.cdb.Read16;
import org.jscsi.scsi.protocol.cdb.Read6;
import org.jscsi.scsi.protocol.cdb.Write10;
import org.jscsi.scsi.protocol.cdb.Write12;
import org.jscsi.scsi.protocol.cdb.Write16;
import org.jscsi.scsi.protocol.cdb.Write6;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.inquiry.StaticInquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.mode.StaticModePageRegistry;
import org.jscsi.scsi.tasks.TaskAttribute;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.tasks.buffered.BufferedTaskFactory;
import org.jscsi.scsi.tasks.management.DefaultTaskManager;
import org.jscsi.scsi.tasks.management.DefaultTaskSet;
import org.jscsi.scsi.tasks.management.TaskManager;
import org.jscsi.scsi.tasks.management.TaskSet;
import org.jscsi.scsi.transport.TestTargetTransportPort;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DefaultLogicalUnitTest extends AbstractLogicalUnit
{
   private static Logger _logger = Logger.getLogger(DefaultLogicalUnitTest.class);

   private static final int NUM_BLOCKS_TRANSFER = 16;

   private static final int TASK_SET_QUEUE_DEPTH = 16;
   private static final int TASK_MGR_NUM_THREADS = 2;
   private static final int STORE_BLOCK_SIZE = 4096;
   // STORE_CAPACITY is representative of the number of blocks, thus:
   //   8192 * 4096B = 32MB
   private static final int STORE_CAPACITY = 8192;

   private static LogicalUnit lu;

   private static TaskSet taskSet;
   private static TaskManager taskManager;
   private static ModePageRegistry modeRegistry;
   private static InquiryDataRegistry inquiryRegistry;
   private static TaskFactory taskFactory;
   private static ByteBuffer store;

   private long cmdRef = 0;

   private TestTargetTransportPort transport =
         new TestTargetTransportPort(STORE_CAPACITY, STORE_BLOCK_SIZE);

   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      _logger.debug("initializing test");

      taskSet = new DefaultTaskSet(TASK_SET_QUEUE_DEPTH);
      taskManager = new DefaultTaskManager(TASK_MGR_NUM_THREADS, taskSet);
      modeRegistry = new StaticModePageRegistry();
      inquiryRegistry = new StaticInquiryDataRegistry();

      store = ByteBuffer.allocate(STORE_BLOCK_SIZE * STORE_CAPACITY);
      taskFactory = new BufferedTaskFactory(store, STORE_BLOCK_SIZE, modeRegistry, inquiryRegistry);

      lu = new DefaultLogicalUnitTest(taskSet, taskManager, taskFactory);
      _logger.debug("created logical unit: " + lu);

      lu.start();
      _logger.debug("logical unit successfully started");
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      lu.stop();
      _logger.debug("exiting test");
   }

   @Before
   public void setUp() throws Exception
   {
   }

   @After
   public void tearDown() throws Exception
   {
   }

   /////////////////////////////////////////////////////////////////////////////
   //

   @Test
   public void TestReadWriteCompare6()
   {
      CDB cdb1 = new Write6(false, true, 10, NUM_BLOCKS_TRANSFER);
      Command cmd1 =
            new Command(this.transport.createNexus(this.cmdRef), cdb1, TaskAttribute.ORDERED,
                  this.cmdRef, 0);
      this.transport.createReadData(NUM_BLOCKS_TRANSFER * STORE_BLOCK_SIZE, this.cmdRef);
      lu.enqueue(this.transport, cmd1);
      this.cmdRef++;

      CDB cdb2 = new Read6(false, true, 10, NUM_BLOCKS_TRANSFER);
      Command cmd2 =
            new Command(this.transport.createNexus(this.cmdRef), cdb2, TaskAttribute.ORDERED,
                  this.cmdRef, 0);
      lu.enqueue(this.transport, cmd2);

      try
      {
         Thread.sleep(500);
      }
      catch (InterruptedException e)
      {
      }

      byte[] readBuf = this.transport.getReadDataMap().get(cmdRef - 1).array();
      byte[] writeBuf = this.transport.getWriteDataMap().get(cmdRef).array();

      Assert.assertTrue("inconsistent read/write comparison", Arrays.equals(readBuf, writeBuf));
   }

   @Test
   public void TestReadWriteCompare10()
   {
      CDB cdb1 = new Write10(0, false, false, false, false, false, 10, NUM_BLOCKS_TRANSFER);
      Command cmd1 =
            new Command(this.transport.createNexus(this.cmdRef), cdb1, TaskAttribute.ORDERED,
                  this.cmdRef, 0);
      this.transport.createReadData(NUM_BLOCKS_TRANSFER * STORE_BLOCK_SIZE, this.cmdRef);
      lu.enqueue(this.transport, cmd1);
      this.cmdRef++;

      CDB cdb2 = new Read10(0, false, false, false, false, false, 10, NUM_BLOCKS_TRANSFER);
      Command cmd2 =
            new Command(this.transport.createNexus(this.cmdRef), cdb2, TaskAttribute.ORDERED,
                  this.cmdRef, 0);
      lu.enqueue(this.transport, cmd2);

      try
      {
         Thread.sleep(500);
      }
      catch (InterruptedException e)
      {
      }

      byte[] readBuf = this.transport.getReadDataMap().get(cmdRef - 1).array();
      byte[] writeBuf = this.transport.getWriteDataMap().get(cmdRef).array();

      Assert.assertTrue("inconsistent read/write comparison", Arrays.equals(readBuf, writeBuf));
   }

   @Test
   public void TestReadWriteCompare12()
   {
      CDB cdb1 = new Write12(0, false, false, false, false, false, 10, NUM_BLOCKS_TRANSFER);
      Command cmd1 =
            new Command(this.transport.createNexus(this.cmdRef), cdb1, TaskAttribute.ORDERED,
                  this.cmdRef, 0);
      this.transport.createReadData(NUM_BLOCKS_TRANSFER * STORE_BLOCK_SIZE, this.cmdRef);
      lu.enqueue(this.transport, cmd1);
      this.cmdRef++;

      CDB cdb2 = new Read12(0, false, false, false, false, false, 10, NUM_BLOCKS_TRANSFER);
      Command cmd2 =
            new Command(this.transport.createNexus(this.cmdRef), cdb2, TaskAttribute.ORDERED,
                  this.cmdRef, 0);
      lu.enqueue(this.transport, cmd2);

      try
      {
         Thread.sleep(500);
      }
      catch (InterruptedException e)
      {
      }

      byte[] readBuf = this.transport.getReadDataMap().get(cmdRef - 1).array();
      byte[] writeBuf = this.transport.getWriteDataMap().get(cmdRef).array();

      Assert.assertTrue("inconsistent read/write comparison", Arrays.equals(readBuf, writeBuf));
   }

   @Test
   public void TestReadWriteCompare16()
   {
      CDB cdb1 = new Write16(0, false, false, false, false, false, 10, NUM_BLOCKS_TRANSFER);
      Command cmd1 =
            new Command(this.transport.createNexus(this.cmdRef), cdb1, TaskAttribute.ORDERED,
                  this.cmdRef, 0);
      this.transport.createReadData(NUM_BLOCKS_TRANSFER * STORE_BLOCK_SIZE, this.cmdRef);
      lu.enqueue(this.transport, cmd1);
      this.cmdRef++;

      CDB cdb2 = new Read16(0, false, false, false, false, false, 10, NUM_BLOCKS_TRANSFER);
      Command cmd2 =
            new Command(this.transport.createNexus(this.cmdRef), cdb2, TaskAttribute.ORDERED,
                  this.cmdRef, 0);
      lu.enqueue(this.transport, cmd2);

      try
      {
         Thread.sleep(500);
      }
      catch (InterruptedException e)
      {
      }

      byte[] readBuf = this.transport.getReadDataMap().get(cmdRef - 1).array();
      byte[] writeBuf = this.transport.getWriteDataMap().get(cmdRef).array();

      Assert.assertTrue("inconsistent read/write comparison", Arrays.equals(readBuf, writeBuf));
   }

   /////////////////////////////////////////////////////////////////////////////
   // constructor(s)

   public DefaultLogicalUnitTest()
   {

   }

   public DefaultLogicalUnitTest(TaskSet taskSet, TaskManager taskManager, TaskFactory taskFactory)
   {
      super(taskSet, taskManager, taskFactory);
   }
}
