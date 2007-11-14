package org.jscsi.scsi.tasks.buffered;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.RequestSense;
import org.junit.Test;

public class BufferedRequestSenseTaskTest extends BufferTestTask
{
   private static Logger _logger = Logger.getLogger(BufferedReadCapacityTaskTest.class);
   
   private static int cmdRef = 0;

   ///////////////////////////////////////////////////////////////////////////// 
   
   @Test
   public void testRequestSense()
   {
      _logger.debug("********** REQUEST SENSE TEST **********");
      CDB cdb = new RequestSense(false, 252);
      this.submitCDB(cdb, cmdRef);
      // TODO: Verify Sense Return Value
      cmdRef++;
   }
     
}
