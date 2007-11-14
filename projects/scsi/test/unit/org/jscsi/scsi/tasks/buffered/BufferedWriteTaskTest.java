package org.jscsi.scsi.tasks.buffered;

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

   /////////////////////////////////////////////////////////////////////////////
   
   @Test
   public void testWrite6()
   {
      _logger.debug("********** WRITE6 **********");
      CDB cdb = new Write6(false, true, 0, WRITE_BLOCKS);
      this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitCDB(cdb, cmdRef);
      // TODO: Verify Write Return Value
      this.purgeReadData(cmdRef);
      cmdRef++;
   }
   
   @Test
   public void testWrite10()
   {
      _logger.debug("********** WRITE10 **********");
      CDB cdb = new Write10(0, false, false, false, false, false, 0, WRITE_BLOCKS);
      this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitCDB(cdb, cmdRef);
      // TODO: Verify Write Return Value
      this.purgeReadData(cmdRef);
      cmdRef++;
   }
   
   @Test
   public void testWrite12()
   {
      _logger.debug("********** WRITE12 **********");
      CDB cdb = new Write12(0, false, false, false, false, false, 0, WRITE_BLOCKS);
      this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitCDB(cdb, cmdRef);
      // TODO: Verify Write Return Value
      this.purgeReadData(cmdRef);
      cmdRef++;
   }
   
   @Test
   public void testWrite16()
   {
      _logger.debug("********** WRITE16 **********");
      CDB cdb = new Write12(0, false, false, false, false, false, 0, WRITE_BLOCKS);
      this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitCDB(cdb, cmdRef);
      // TODO: Verify Write Return Value
      this.purgeReadData(cmdRef);
      cmdRef++;
   }
}
