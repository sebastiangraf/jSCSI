package org.jscsi.scsi.tasks.buffered;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.Write6;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskAttribute;
import org.jscsi.scsi.transport.Nexus;
import org.junit.Assert;
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
      CDB cdb = new Write6(false, true, 0, WRITE_BLOCKS);
      Command cmd = new Command(new Nexus("initiator", "target", 0, 0), cdb, TaskAttribute.ORDERED, this.cmdRef, 0);
      this.createReadData(WRITE_BLOCKS * STORE_BLOCK_SIZE, cmdRef);
      this.cmdRef++;

      try
      {
         Task task = this.getMemoryTask(this, cmd);
         task.run();
         
         task = this.getFileTask(this, cmd);
         task.run();
      }
      catch (IllegalRequestException e)
      {
         Assert.fail("illegal request");
      }
      this.purgeReadData(cmdRef);
   }
}
