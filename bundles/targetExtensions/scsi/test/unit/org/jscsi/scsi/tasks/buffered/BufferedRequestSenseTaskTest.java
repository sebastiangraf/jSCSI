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

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.RequestSense;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException.ResponseCode;
import org.junit.Test;

public class BufferedRequestSenseTaskTest extends BufferTestTask
{
   private static Logger _logger = Logger.getLogger(BufferedReadCapacityTaskTest.class);

   private static int cmdRef = 0;

   private static int ALLOCATION_LENGTH = 252;

   ///////////////////////////////////////////////////////////////////////////// 

   @Test
   public void testRequestSenseinMemory()
   {
      _logger.debug("********** REQUEST SENSE TEST - MEMORY **********");
      CDB cdb = new RequestSense(false, ALLOCATION_LENGTH);
      this.submitMemoryTask(cdb, cmdRef);
      verifyInputBuffer();
      cmdRef++;
   }

   @Test
   public void testRequestSenseinFile()
   {
      _logger.debug("********** REQUEST SENSE TEST - FILE **********");
      CDB cdb = new RequestSense(false, ALLOCATION_LENGTH);
      this.submitFileTask(cdb, cmdRef);
      verifyInputBuffer();
      cmdRef++;
   }

   /**
    * Verify that the Sense Data that was placed into the device's buffer
    * 
    * @param data The data written to the device
    */
   private void verifyInputBuffer()
   {
      ByteBuffer writtenBuffer = this.getWriteData(cmdRef);

      assertNotNull(writtenBuffer);

      writtenBuffer.rewind();

      byte responseCode = writtenBuffer.get();

      // Check that the valid bit is set
      assertEquals(1, (responseCode >> 7) & 1);

      // Check that the response code is for fixed sense data
      int responseCodeValue = (int) ResponseCode.CURRENT_FIXED.code();
      assertEquals(responseCodeValue, (responseCode & 0x7F));

      // Check the remaining length field
      byte[] nextSix = new byte[7];
      writtenBuffer.get(nextSix);
      int remainingLength = (int) nextSix[6];
      assertEquals(writtenBuffer.limit() - 8, remainingLength);

   }

}
