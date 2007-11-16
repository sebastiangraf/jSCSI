package org.jscsi.scsi.tasks.buffered;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.ReadCapacity10;
import org.jscsi.scsi.protocol.cdb.ReadCapacity16;
import org.junit.Test;

public class BufferedReadCapacityTaskTest extends BufferTestTask
{
   private static Logger _logger = Logger.getLogger(BufferedReadCapacityTaskTest.class);
   
   private static int cmdRef = 0;

   ///////////////////////////////////////////////////////////////////////////// 
   
   @Test
   public void testReadCapacity10inMemory()
   {
      _logger.debug("********** READ CAPACITY 10 MEMORY **********");
      CDB cdb = new ReadCapacity10(false, 0);
      this.submitMemoryTask(cdb, cmdRef);
      verifyInputBufferCapacity10();
      cmdRef++;
   }
   
   @Test
   public void testReadCapacity10inFile()
   {
      _logger.debug("********** READ CAPACITY 10 FILE **********");
      CDB cdb = new ReadCapacity10(false, 0);
      this.submitFileTask(cdb, cmdRef);
      verifyInputBufferCapacity10();
      cmdRef++;
   }
   
   @Test
   public void testReadCapacity16inMemory()
   {
      _logger.debug("********** READ CAPACITY 16 MEMORY **********");
      CDB cdb = new ReadCapacity16(32, false, 0);
      this.submitMemoryTask(cdb, cmdRef);
      verifyInputBufferCapacity16();
      cmdRef++;
   }
   
   @Test
   public void testReadCapacity16inFile()
   {
      _logger.debug("********** READ CAPACITY 16 FILE **********");
      CDB cdb = new ReadCapacity16(32, false, 0);
      this.submitFileTask(cdb, cmdRef);
      verifyInputBufferCapacity16();
      cmdRef++;
   }
   
   public void verifyInputBufferCapacity10()
   {
      final int expectedNumberOfBlocks = STORE_CAPACITY / STORE_BLOCK_SIZE;
      
      ByteBuffer inputBuffer = this.getWriteData(cmdRef);
      
      assertNotNull(inputBuffer);
      
      inputBuffer.rewind();
      
      final int returnedNumberOfBlocks = inputBuffer.getInt();
      
      final int returnedBlockLength = inputBuffer.getInt();
      
      assertEquals(expectedNumberOfBlocks, returnedNumberOfBlocks);
      
      assertEquals(returnedBlockLength, STORE_BLOCK_SIZE);
   }
   
   /**
    * Whitebox test of ReadCapacity16
    */
   public void verifyInputBufferCapacity16()
   {
      final long expectedNumberOfBlocks = STORE_CAPACITY / STORE_BLOCK_SIZE;
      
      ByteBuffer inputBuffer = this.getWriteData(cmdRef);
      
      assertNotNull(inputBuffer);
      
      inputBuffer.rewind();
      
      final long returnedNumberOfBlocks = inputBuffer.getLong();
      
      final int returnedBlockLength = inputBuffer.getInt();
      
      assertEquals(expectedNumberOfBlocks, returnedNumberOfBlocks);
      
      assertEquals(returnedBlockLength, STORE_BLOCK_SIZE);
   }
}
