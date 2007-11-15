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
import org.junit.Test;

public class BufferedReadTaskTest extends BufferTestTask
{
   private static Logger _logger = Logger.getLogger(BufferedReadTaskTest.class);
   
   private static final int READ_BLOCKS = 10;
   
   private static int cmdRef = 0;
   
   private Random rnd = new Random();

   /////////////////////////////////////////////////////////////////////////////
   
   @Test
   public void testRead6inMemory()
   {
      _logger.debug("********** READ6 MEMORY **********");
      int lba = generateRandomLBA();
      CDB cdb = new Read6(false, true, lba, READ_BLOCKS);
      ByteBuffer data = this.writeDeviceData(lba*STORE_BLOCK_SIZE, READ_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitMemoryTask(cdb, cmdRef);
      verifyInputBuffer(data);
      this.purgeDeviceData();
      cmdRef++;
   }
   
   @Test
   public void testRead6inFile()
   {
      _logger.debug("********** READ6 FILE **********");
      int lba = generateRandomLBA();
      CDB cdb = new Read6(false, true, lba, READ_BLOCKS);
      ByteBuffer data = this.writeDeviceData(lba*STORE_BLOCK_SIZE, READ_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitFileTask(cdb, cmdRef);
      verifyInputBuffer(data);
      this.purgeDeviceData();
      cmdRef++;
   }
   
   @Test
   public void testRead10inMemory()
   {
      _logger.debug("********** READ10 MEMORY **********");
      int lba = generateRandomLBA();
      CDB cdb = new Read10(0, false, false, false, false, false, lba, READ_BLOCKS);
      ByteBuffer data = this.writeDeviceData(lba*STORE_BLOCK_SIZE, READ_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitMemoryTask(cdb, cmdRef);
      verifyInputBuffer(data);
      this.purgeDeviceData();
      cmdRef++;
   }
   
   @Test
   public void testRead10inFile()
   {
      _logger.debug("********** READ10 FILE **********");
      int lba = generateRandomLBA();
      CDB cdb = new Read10(0, false, false, false, false, false, lba, READ_BLOCKS);
      ByteBuffer data = this.writeDeviceData(lba*STORE_BLOCK_SIZE, READ_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitFileTask(cdb, cmdRef);
      verifyInputBuffer(data);
      this.purgeDeviceData();
      cmdRef++;
   }
   
   @Test
   public void testRead12inMemory()
   {
      _logger.debug("********** READ12 MEMORY **********");
      int lba = generateRandomLBA();
      CDB cdb = new Read12(0, false, false, false, false, false, lba, READ_BLOCKS);
      ByteBuffer data = this.writeDeviceData(lba*STORE_BLOCK_SIZE, READ_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitMemoryTask(cdb, cmdRef);
      verifyInputBuffer(data);
      this.purgeDeviceData();
      cmdRef++;
   }
   
   @Test
   public void testRead12inFile()
   {
      _logger.debug("********** READ12 FILE **********");
      int lba = generateRandomLBA();
      CDB cdb = new Read12(0, false, false, false, false, false, lba, READ_BLOCKS);
      ByteBuffer data = this.writeDeviceData(lba*STORE_BLOCK_SIZE, READ_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitFileTask(cdb, cmdRef);
      verifyInputBuffer(data);
      this.purgeDeviceData();
      cmdRef++;
   }
   
   @Test
   public void testRead16inMemory()
   {
      _logger.debug("********** READ16 MEMORY **********");
      int lba = generateRandomLBA();
      CDB cdb = new Read16(0, false, false, false, false, false, lba, READ_BLOCKS);
      ByteBuffer data = this.writeDeviceData(lba*STORE_BLOCK_SIZE, READ_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitMemoryTask(cdb, cmdRef);
      verifyInputBuffer(data);
      this.purgeDeviceData();
      cmdRef++;
   }
   
   @Test
   public void testRead16inFile()
   {
      _logger.debug("********** READ16 FILE **********");
      int lba = generateRandomLBA();
      CDB cdb = new Read16(0, false, false, false, false, false, lba, READ_BLOCKS);
      ByteBuffer data = this.writeDeviceData(lba*STORE_BLOCK_SIZE, READ_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitFileTask(cdb, cmdRef);
      verifyInputBuffer(data);
      this.purgeDeviceData();
      cmdRef++;
   }
   
   /**
    * Verify that the data that was placed into the device's buffer matches
    * what was set into the InputBuffer after calling Read
    * 
    * @param data The data written to the device
    */
   private void verifyInputBuffer(ByteBuffer data)
   {
      final int bytesWritten = READ_BLOCKS * STORE_BLOCK_SIZE;
      
      ByteBuffer writtenBuffer = this.getWriteData(cmdRef);
      
      assertNotNull(data);
      assertNotNull(writtenBuffer);
      
      data.rewind();
      writtenBuffer.rewind();
      
      for (int i=0; i<bytesWritten; i++)
      {
         byte d = data.get();
         byte w = writtenBuffer.get();
         
         assertEquals(d, w);
      }
   }
   
   private int generateRandomLBA()
   {
      return Math.abs(rnd.nextInt()) % ((STORE_CAPACITY / STORE_BLOCK_SIZE) - READ_BLOCKS);
   }
   
}
