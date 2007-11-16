
package org.jscsi.scsi.tasks.buffered;

import java.nio.ByteBuffer;

import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;

// TODO: Describe class or interface
public class BufferedReadCapacity10Task extends BufferedTask
{

   public BufferedReadCapacity10Task()
   {
      super();
   }

   @Override
   protected void execute(ByteBuffer file, int blockLength) throws InterruptedException,
         SenseException
   {
      // NOTE: We ignore the PMI bit because file has no substantial transfer delay point

      // Report capacity up to maximum READ CAPACITY (10) value.
      byte[] capacity =
            ByteBuffer.allocate(8).putLong(
                  this.getFileCapacity() < 0xFFFFFFFFL ? this.getFileCapacity() : 0xFFFFFFFFL).array();

      // Create parameter data
      ByteBuffer data = ByteBuffer.allocate(8).put(capacity, 4, 4).putInt(blockLength);

      data.rewind();

      this.writeData(data);
      this.writeResponse(Status.GOOD, null);
   }

}
