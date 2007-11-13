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
   
   private int cmdRef = 0;

   /////////////////////////////////////////////////////////////////////////////
   
   @Test
   public void simpleWrite6()
   {
      _logger.debug("********** SIMPLE WRITE6 **********");
      CDB cdb = new Write6(false, true, 0, WRITE_BLOCKS);
      this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitWrite(cdb, cmdRef);
      this.purgeReadData(cmdRef);
      this.cmdRef++;
   }
   
   @Test
   public void simpleWrite10()
   {
      _logger.debug("********** SIMPLE WRITE10 **********");
      CDB cdb = new Write10(0, false, false, false, false, false, 0, WRITE_BLOCKS);
      this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitWrite(cdb, cmdRef);
      this.purgeReadData(cmdRef);
      this.cmdRef++;
   }
   
   @Test
   public void simpleWrite12()
   {
      _logger.debug("********** SIMPLE WRITE12 **********");
      CDB cdb = new Write12(0, false, false, false, false, false, 0, WRITE_BLOCKS);
      this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitWrite(cdb, cmdRef);
      this.purgeReadData(cmdRef);
      this.cmdRef++;
   }
   
   @Test
   public void simpleWrite16()
   {
      _logger.debug("********** SIMPLE WRITE16 **********");
      CDB cdb = new Write12(0, false, false, false, false, false, 0, WRITE_BLOCKS);
      this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitWrite(cdb, cmdRef);
      this.purgeReadData(cmdRef);
      this.cmdRef++;
   }
}
