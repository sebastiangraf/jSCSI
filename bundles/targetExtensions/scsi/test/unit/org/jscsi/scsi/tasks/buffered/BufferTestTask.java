/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
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

package org.jscsi.scsi.tasks.buffered;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.inquiry.StaticInquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.mode.StaticModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.target.Target;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskAttribute;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

public class BufferTestTask implements TargetTransportPort
{
   private static Logger _logger = Logger.getLogger(BufferTestTask.class);

   static final int STORE_CAPACITY = 1024 * 1024 * 4; // 4MB
   static final int STORE_BLOCK_SIZE = 4096;
   static final String STORE_FILE_PATH = "test-output/file-store.dat";

   private static ModePageRegistry modeRegistry;
   private static InquiryDataRegistry inquiryRegistry;
   private static ByteBuffer memBuf;
   private static ByteBuffer fileBuf;
   private static TaskFactory memFactory;
   private static TaskFactory fileFactory;
   private static RandomAccessFile file;

   private Random rnd = new Random();
   private HashMap<Long, ByteBuffer> readDataMap = new HashMap<Long, ByteBuffer>();
   private HashMap<Long, ByteBuffer> writeDataMap = new HashMap<Long, ByteBuffer>();

   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      _logger.debug("configuring test class");

      new File("test-output").mkdir();

      // initialize the buffers
      memBuf = ByteBuffer.allocate(STORE_CAPACITY);
      memBuf.put(new byte[STORE_CAPACITY]); // Zero out contents

      // Attempt to delete file if it is there
      File deleteFile = new File(STORE_FILE_PATH);
      deleteFile.delete();

      file = new RandomAccessFile(STORE_FILE_PATH, "rw");
      fileBuf = file.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, STORE_CAPACITY);

      // initialize the registries
      modeRegistry = new StaticModePageRegistry();
      inquiryRegistry = new StaticInquiryDataRegistry();

      memFactory = new BufferedTaskFactory(memBuf, STORE_BLOCK_SIZE, modeRegistry, inquiryRegistry);
      fileFactory =
            new BufferedTaskFactory(fileBuf, STORE_BLOCK_SIZE, modeRegistry, inquiryRegistry);
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      _logger.debug("flushing file buffer to backing store");
      ((MappedByteBuffer) fileBuf).force();

      _logger.debug("closing file buffer backing store");
      file.close();
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
   // TargetTransportPort implementation

   public boolean readData(Nexus nexus, long commandReferenceNumber, ByteBuffer output)
         throws InterruptedException
   {
      _logger.debug("servicing readData request: nexus: " + nexus + ", cmdRef: "
            + commandReferenceNumber);
      ByteBuffer data = this.readDataMap.get(commandReferenceNumber);
      data.rewind();
      output.put(data);
      return true;
   }

   public boolean writeData(Nexus nexus, long commandReferenceNumber, ByteBuffer input)
         throws InterruptedException
   {
      _logger.debug("servicing writeData request");

      ByteBuffer copy = ByteBuffer.allocate(input.limit());
      copy.put(input);

      this.writeDataMap.put(commandReferenceNumber, copy);

      return true;
   }

   public void registerTarget(Target target)
   {
      _logger.debug("servicing registerTarget request");
   }

   public void removeTarget(String targetName) throws Exception
   {
      _logger.debug("servicing removeTarget request");
   }

   public void terminateDataTransfer(Nexus nexus, long commandReferenceNumber)
   {
      _logger.debug("servicing terminateDataTransfer request");
   }

   public void writeResponse(
         Nexus nexus,
         long commandReferenceNumber,
         Status status,
         ByteBuffer senseData)
   {
      _logger.debug("servicing writeResponse request: nexus: " + nexus + ", cmdRef: "
            + commandReferenceNumber);
      _logger.debug("response was status: " + status);
   }

   /////////////////////////////////////////////////////////////////////////////

   public void addReadData(int size, long cmdRef, ByteBuffer data)
   {
      this.readDataMap.put(cmdRef, data);
   }

   public ByteBuffer createReadData(int size, long cmdRef)
   {
      byte[] data = new byte[size];
      this.rnd.nextBytes(data);

      ByteBuffer buffData = ByteBuffer.allocate(size);
      buffData.put(data);

      this.readDataMap.put(cmdRef, buffData);
      return buffData;
   }

   public ByteBuffer writeDeviceData(int offset, int size, long cmdRef)
   {
      byte[] data = new byte[size];
      this.rnd.nextBytes(data);

      ByteBuffer buffData = ByteBuffer.allocate(size);
      buffData.put(data);

      // Write to memory buffer
      memBuf.position(offset);
      memBuf.put(data);

      // Write to file buffer
      fileBuf.position(offset);
      fileBuf.put(data);

      return buffData;
   }

   public void purgeReadData(long cmdRef)
   {
      this.readDataMap.remove(cmdRef);
   }

   public void purgeDeviceData()
   {
      byte[] zeros = new byte[STORE_CAPACITY];

      ByteBuffer buffData = ByteBuffer.allocate(zeros.length);
      buffData.put(zeros);

      // Write to memory buffer
      memBuf.rewind();
      memBuf.put(zeros);

      // Write to file buffer
      fileBuf.rewind();
      fileBuf.put(zeros);
   }

   public ByteBuffer getReadData(long cmdRef)
   {
      return this.readDataMap.remove(cmdRef);
   }

   public ByteBuffer getWriteData(long cmdRef)
   {
      return this.writeDataMap.remove(cmdRef);
   }

   public void submitMemoryTask(CDB cdb, int cmdRef)
   {
      Command cmd =
            new Command(new Nexus("initiator", "target", 0, 0), cdb, TaskAttribute.ORDERED, cmdRef,
                  0);

      try
      {
         _logger.debug("running memory buffer task");
         Task task = this.getMemoryTask(this, cmd);
         task.run();
      }
      catch (IllegalRequestException e)
      {
         Assert.fail("illegal request");
      }

   }

   public void submitFileTask(CDB cdb, int cmdRef)
   {
      Command cmd =
            new Command(new Nexus("initiator", "target", 0, 0), cdb, TaskAttribute.ORDERED, cmdRef,
                  0);

      try
      {
         Task task = this.getFileTask(this, cmd);

         _logger.debug("running file buffer task");
         task = this.getFileTask(this, cmd);

         task.run();
      }
      catch (IllegalRequestException e)
      {
         Assert.fail("illegal request");
      }

   }

   /////////////////////////////////////////////////////////////////////////////

   public Task getMemoryTask(TargetTransportPort port, Command command)
         throws IllegalRequestException
   {
      return memFactory.getInstance(port, command);
   }

   public Task getFileTask(TargetTransportPort port, Command command)
         throws IllegalRequestException
   {
      return fileFactory.getInstance(port, command);
   }

   /////////////////////////////////////////////////////////////////////////////

   public ByteBuffer getMemoryBuffer()
   {
      ByteBuffer copy = ByteBuffer.allocate(memBuf.limit());

      ByteBuffer rewoundCopy = memBuf.duplicate();
      rewoundCopy.rewind();

      copy.put(rewoundCopy);

      return copy;
   }

   public ByteBuffer getFileBuffer()
   {
      ByteBuffer copy = ByteBuffer.allocate(fileBuf.limit());

      ByteBuffer rewoundCopy = fileBuf.duplicate();
      rewoundCopy.rewind();

      copy.put(rewoundCopy);

      return copy;
   }

}
