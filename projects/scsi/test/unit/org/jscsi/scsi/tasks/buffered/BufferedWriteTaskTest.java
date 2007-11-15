package org.jscsi.scsi.tasks.buffered;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.ByteBuffer;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.Write10;
import org.jscsi.scsi.protocol.cdb.Write12;
import org.jscsi.scsi.protocol.cdb.Write6;
import org.junit.Test;

public class BufferedWriteTaskTest extends BufferTestTask
{
   private static Logger _logger = Logger.getLogger(BufferedWriteTaskTest.class);
   
   private static final int WRITE_BLOCKS = 10;
   
   private static int cmdRef = 0;
   
   private Random rnd = new Random();

   /////////////////////////////////////////////////////////////////////////////
   
   @Test
   public void testWrite6inMemory()
   {
      _logger.debug("********** WRITE6 MEMORY **********");
      int lba = 0;//generateRandomLBA();
      CDB cdb = new Write6(false, true, lba, WRITE_BLOCKS);
      ByteBuffer data = this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitMemoryTask(cdb, cmdRef);
      verifyDeviceBuffer(data, this.getMemoryBuffer(), lba);
      this.purgeReadData(cmdRef);
      this.purgeDeviceData();
      cmdRef++;
   }
   
   @Test
   public void testWrite6inFile()
   {
      _logger.debug("********** WRITE6 FILE **********");
      CDB cdb = new Write6(false, true, 0, WRITE_BLOCKS);
      this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitFileTask(cdb, cmdRef);
      // TODO: Verify Write Return Value
      this.purgeReadData(cmdRef);
      cmdRef++;
   }
   
   @Test
   public void testWrite10inMemory()
   {
      _logger.debug("********** WRITE10 MEMORY **********");
      CDB cdb = new Write10(0, false, false, false, false, false, 0, WRITE_BLOCKS);
      this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitMemoryTask(cdb, cmdRef);
      // TODO: Verify Write Return Value
      this.purgeReadData(cmdRef);
      cmdRef++;
   }
   
   @Test
   public void testWrite10inFile()
   {
      _logger.debug("********** WRITE10 FILE **********");
      CDB cdb = new Write10(0, false, false, false, false, false, 0, WRITE_BLOCKS);
      this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitFileTask(cdb, cmdRef);
      // TODO: Verify Write Return Value
      this.purgeReadData(cmdRef);
      cmdRef++;
   }
   
   @Test
   public void testWrite12InMemory()
   {
      _logger.debug("********** WRITE12 MEMORY **********");
      CDB cdb = new Write12(0, false, false, false, false, false, 0, WRITE_BLOCKS);
      this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitMemoryTask(cdb, cmdRef);
      // TODO: Verify Write Return Value
      this.purgeReadData(cmdRef);
      cmdRef++;
   }
   
   @Test
   public void testWrite12InFile()
   {
      _logger.debug("********** WRITE12 FILE **********");
      CDB cdb = new Write12(0, false, false, false, false, false, 0, WRITE_BLOCKS);
      this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitFileTask(cdb, cmdRef);
      // TODO: Verify Write Return Value
      this.purgeReadData(cmdRef);
      cmdRef++;
   }
   
   @Test
   public void testWrite16inMemory()
   {
      _logger.debug("********** WRITE16 MEMORY **********");
      CDB cdb = new Write12(0, false, false, false, false, false, 0, WRITE_BLOCKS);
      this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitMemoryTask(cdb, cmdRef);
      // TODO: Verify Write Return Value
      this.purgeReadData(cmdRef);
      cmdRef++;
   }
   
   @Test
   public void testWrite16inFile()
   {
      _logger.debug("********** WRITE16 FILE **********");
      CDB cdb = new Write12(0, false, false, false, false, false, 0, WRITE_BLOCKS);
      this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitFileTask(cdb, cmdRef);
      // TODO: Verify Write Return Value
      this.purgeReadData(cmdRef);
      cmdRef++;
   }
   

   /**
    * Verify that the data was properly placed into the device's buffer
    * 
    * @param data The data written to the device
    */
   private void verifyDeviceBuffer(ByteBuffer writtenData, ByteBuffer deviceData, int lba)
   {
      final int bytesWritten = WRITE_BLOCKS * STORE_BLOCK_SIZE;
      
      assertNotNull(deviceData);
      assertNotNull(writtenData);
      
      deviceData.position(lba * STORE_BLOCK_SIZE);
      writtenData.rewind();
      
      for (int i=0; i<bytesWritten; i++)
      {
         byte d = deviceData.get();
         byte w = writtenData.get();
         
         assertEquals(w, d);
      }
   }
   
   private int generateRandomLBA()
   {
      return Math.abs(rnd.nextInt()) % ((STORE_CAPACITY / STORE_BLOCK_SIZE) - WRITE_BLOCKS);
   }
}
