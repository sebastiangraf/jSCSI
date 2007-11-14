package org.jscsi.scsi.tasks.buffered;

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
   public void testReadCapacity10()
   {
      _logger.debug("********** READ CAPACITY 10 **********");
      CDB cdb = new ReadCapacity10(false, 0);
      this.submitCDB(cdb, cmdRef);
      // TODO: Verify Read Capacity Return Value
      cmdRef++;
   }
   
   @Test
   public void testReadCapacity16()
   {
      _logger.debug("********** READ CAPACITY 16 **********");
      CDB cdb = new ReadCapacity16(8, false, 0);
      this.submitCDB(cdb, cmdRef);
      // TODO: Verify Read Capacity Return Value
      cmdRef++;
   }
}
