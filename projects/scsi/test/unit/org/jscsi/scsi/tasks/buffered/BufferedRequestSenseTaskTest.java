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
      _logger.debug("********** REQUEST SENSE TEST **********");
      CDB cdb = new RequestSense(false, ALLOCATION_LENGTH);
      this.submitMemoryTask(cdb, cmdRef);
      verifyInputBuffer();
      cmdRef++;
   }
   
   @Test
   public void testRequestSenseinFile()
   {
      _logger.debug("********** REQUEST SENSE TEST **********");
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
      
      //int remainingLength = 
      //assertEquals()
      
      /*
      for (int i=0; i<ALLOCATION_LENGTH; i++)
      {
         byte d = data.get();
         byte w = writtenBuffer.get();
         
         assertEquals(d, w);
      }
      */
   }
     
}
