package org.jscsi.scsi.tasks.buffered;

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

   /////////////////////////////////////////////////////////////////////////////
   
   @Test
   public void testRead6()
   {
      _logger.debug("********** READ6 **********");
      CDB cdb = new Read6(false, true, 0, READ_BLOCKS);
      this.createWriteData(READ_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitCDB(cdb, cmdRef);
      // TODO: Verify Read Return Value
      this.purgeWriteData(cmdRef);
      cmdRef++;
   }
   
   @Test
   public void testRead10()
   {
      _logger.debug("********** READ10 **********");
      CDB cdb = new Read10(0, false, false, false, false, false, 0, READ_BLOCKS);
      this.createWriteData(READ_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitCDB(cdb, cmdRef);
      // TODO: Verify Read Return Value
      this.purgeWriteData(cmdRef);
      cmdRef++;
   }
   
   @Test
   public void testRead12()
   {
      _logger.debug("********** READ12 **********");
      CDB cdb = new Read12(0, false, false, false, false, false, 0, READ_BLOCKS);
      this.createWriteData(READ_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitCDB(cdb, cmdRef);
      // TODO: Verify Read Return Value
      this.purgeWriteData(cmdRef);
      cmdRef++;
   }
   
   @Test
   public void testRead16()
   {
      _logger.debug("********** READ16 **********");
      CDB cdb = new Read16(0, false, false, false, false, false, 0, READ_BLOCKS);
      this.createWriteData(READ_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitCDB(cdb, cmdRef);
      // TODO: Verify Read Return Value
      this.purgeWriteData(cmdRef);
      cmdRef++;
   }
   
}
