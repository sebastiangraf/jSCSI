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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.Read10;
import org.jscsi.scsi.protocol.cdb.Read12;
import org.jscsi.scsi.protocol.cdb.Read16;
import org.jscsi.scsi.protocol.cdb.Read6;
import org.jscsi.scsi.protocol.cdb.Write10;
import org.jscsi.scsi.protocol.cdb.Write12;
import org.jscsi.scsi.protocol.cdb.Write16;
import org.jscsi.scsi.protocol.cdb.Write6;
import org.junit.Test;

public class BufferedUDCTaskTest extends BufferTestTask
{
   private static Logger _logger = Logger.getLogger(BufferedUDCTaskTest.class);

   private static final int MIN_BLOCKS = 5;

   private static final int MAX_BLOCKS = 15;

   private static int ITERATIONS = 10;

   private static int cmdRef = 0;

   private Random rnd = new Random();

   /////////////////////////////////////////////////////////////////////////////

   @Test
   public void testUDC()
   {
      for (int itr = 0; itr < ITERATIONS; itr++)
      {
         // Choose random block length
         int numBlocks = Math.abs(rnd.nextInt() % (MAX_BLOCKS - MIN_BLOCKS)) + MIN_BLOCKS;

         // Generate random data
         ByteBuffer data = generateData(numBlocks * STORE_BLOCK_SIZE);

         // Choose random position
         int lba = Math.abs(rnd.nextInt()) % ((STORE_CAPACITY / STORE_BLOCK_SIZE) - numBlocks);

         // Write 6
         performWrite6(lba, numBlocks, data);

         // Read 6 and Verify
         performRead6(lba, numBlocks, data);

         // Write 10
         performWrite10(lba, numBlocks, data);

         // Read 10 and Verify
         performRead10(lba, numBlocks, data);

         // Write 12
         performWrite12(lba, numBlocks, data);

         // Read 12 and Verify
         performRead12(lba, numBlocks, data);

         // Write 16
         performWrite16(lba, numBlocks, data);

         // Read 16 and Verify
         performRead16(lba, numBlocks, data);
      }
   }

   private void performWrite6(int lba, int numBlocks, ByteBuffer data)
   {
      _logger.debug("********** WRITE6 MEMORY **********");
      CDB cdb = new Write6(false, true, lba, numBlocks);
      this.addReadData(numBlocks * STORE_BLOCK_SIZE, cmdRef, data);
      this.submitMemoryTask(cdb, cmdRef);
      this.purgeReadData(cmdRef);
      cmdRef++;
   }

   private void performWrite10(int lba, int numBlocks, ByteBuffer data)
   {
      _logger.debug("********** WRITE10 MEMORY **********");
      CDB cdb = new Write10(0, false, false, false, false, false, lba, numBlocks);
      this.addReadData(numBlocks * STORE_BLOCK_SIZE, cmdRef, data);
      this.submitMemoryTask(cdb, cmdRef);
      this.purgeReadData(cmdRef);
      cmdRef++;
   }

   private void performWrite12(int lba, int numBlocks, ByteBuffer data)
   {
      _logger.debug("********** WRITE12 MEMORY **********");
      CDB cdb = new Write12(0, false, false, false, false, false, lba, numBlocks);
      this.addReadData(numBlocks * STORE_BLOCK_SIZE, cmdRef, data);
      this.submitMemoryTask(cdb, cmdRef);
      this.purgeReadData(cmdRef);
      cmdRef++;
   }

   private void performWrite16(int lba, int numBlocks, ByteBuffer data)
   {
      _logger.debug("********** WRITE16 MEMORY **********");
      CDB cdb = new Write16(0, false, false, false, false, false, lba, numBlocks);
      this.addReadData(numBlocks * STORE_BLOCK_SIZE, cmdRef, data);
      this.submitMemoryTask(cdb, cmdRef);
      this.purgeReadData(cmdRef);
      cmdRef++;
   }

   private void performRead6(int lba, int numBlocks, ByteBuffer expectedData)
   {
      _logger.debug("********** READ6 MEMORY **********");
      CDB cdb = new Read6(false, true, lba, numBlocks);
      this.submitMemoryTask(cdb, cmdRef);
      verifyInputBuffer(numBlocks, expectedData);
      this.purgeDeviceData();
      cmdRef++;
   }

   private void performRead10(int lba, int numBlocks, ByteBuffer expectedData)
   {
      _logger.debug("********** READ10 MEMORY **********");
      CDB cdb = new Read10(0, false, false, false, false, false, lba, numBlocks);
      this.submitMemoryTask(cdb, cmdRef);
      verifyInputBuffer(numBlocks, expectedData);
      this.purgeDeviceData();
      cmdRef++;
   }

   private void performRead12(int lba, int numBlocks, ByteBuffer expectedData)
   {
      _logger.debug("********** READ12 MEMORY **********");
      CDB cdb = new Read12(0, false, false, false, false, false, lba, numBlocks);
      this.submitMemoryTask(cdb, cmdRef);
      verifyInputBuffer(numBlocks, expectedData);
      this.purgeDeviceData();
      cmdRef++;
   }

   private void performRead16(int lba, int numBlocks, ByteBuffer expectedData)
   {
      _logger.debug("********** READ16 MEMORY **********");
      CDB cdb = new Read16(0, false, false, false, false, false, lba, numBlocks);
      this.submitMemoryTask(cdb, cmdRef);
      verifyInputBuffer(numBlocks, expectedData);
      this.purgeDeviceData();
      cmdRef++;
   }

   /**
    * Verify that the data that was placed into the device's buffer matches
    * what was set into the InputBuffer after calling Read
    * 
    * @param data The data written to the device
    */
   private void verifyInputBuffer(int numBlocks, ByteBuffer data)
   {
      final int bytesWritten = numBlocks * STORE_BLOCK_SIZE;

      ByteBuffer writtenBuffer = this.getWriteData(cmdRef);

      assertNotNull(data);
      assertNotNull(writtenBuffer);

      data.rewind();
      writtenBuffer.rewind();

      for (int i = 0; i < bytesWritten; i++)
      {
         byte d = data.get();
         byte w = writtenBuffer.get();

         assertEquals(d, w);
      }
   }

   private ByteBuffer generateData(int size)
   {
      byte[] data = new byte[size];
      this.rnd.nextBytes(data);

      ByteBuffer buffData = ByteBuffer.allocate(size);
      buffData.put(data);

      return buffData;
   }
}
