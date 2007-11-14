package org.jscsi.scsi.tasks.buffered;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.Read10;
import org.jscsi.scsi.protocol.cdb.Read12;
import org.jscsi.scsi.protocol.cdb.Read6;
import org.jscsi.scsi.protocol.cdb.Write12;
import org.junit.Test;

public class BufferedReadTaskTest extends BufferTestTask
{
   private static Logger _logger = Logger.getLogger(BufferedReadTaskTest.class);
   
   private static final int READ_BLOCKS = 10;
   
   private static int cmdRef = 0;

   /////////////////////////////////////////////////////////////////////////////
   
   @Test
   public void simpleRead6()
   {
      _logger.debug("********** SIMPLE READ6 **********");
      CDB cdb = new Read6(false, true, 0, READ_BLOCKS);
      this.createWriteData(READ_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitRead(cdb, cmdRef);
      this.purgeWriteData(cmdRef);
      cmdRef++;
   }
   
   @Test
   public void simpleRead10()
   {
      _logger.debug("********** SIMPLE READ10 **********");
      CDB cdb = new Read10(0, false, false, false, false, false, 0, READ_BLOCKS);
      this.createWriteData(READ_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitRead(cdb, cmdRef);
      this.purgeWriteData(cmdRef);
      cmdRef++;
   }
   
   @Test
   public void simpleRead12()
   {
      _logger.debug("********** SIMPLE READ12 **********");
      CDB cdb = new Read12(0, false, false, false, false, false, 0, READ_BLOCKS);
      this.createWriteData(READ_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitRead(cdb, cmdRef);
      this.purgeWriteData(cmdRef);
      cmdRef++;
   }
   
   @Test
   public void simpleRead16()
   {
      _logger.debug("********** SIMPLE READ16 **********");
      CDB cdb = new Read12(0, false, false, false, false, false, 0, READ_BLOCKS);
      this.createWriteData(READ_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.submitRead(cdb, cmdRef);
      this.purgeWriteData(cmdRef);
      cmdRef++;
   }
   
}
