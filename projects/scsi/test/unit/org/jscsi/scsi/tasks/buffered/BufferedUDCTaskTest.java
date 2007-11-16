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
      for (int itr = 0; itr<ITERATIONS; itr++)
      {
         // Choose random block length
         int numBlocks = Math.abs(rnd.nextInt() % (MAX_BLOCKS - MIN_BLOCKS)) + MIN_BLOCKS;
         
         // Generate random data
         ByteBuffer data = generateData(numBlocks*STORE_BLOCK_SIZE);
         
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
      
      for (int i=0; i<bytesWritten; i++)
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
