
package org.jscsi.scsi.tasks.buffered;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.protocol.cdb.ReadCapacity10;
import org.jscsi.scsi.protocol.cdb.ReadCapacity16;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;

// TODO: Describe class or interface
public class BufferedReadCapacityTask extends BufferedTask
{

   public BufferedReadCapacityTask()
   {
      super("BufferedReadCapacityTask");
   }

   @Override
   protected void execute(ByteBuffer file, int blockLength) throws InterruptedException,
         SenseException
   {
      // NOTE: We ignore the PMI bit because file has no substantial transfer delay point
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bs);

      try
      {
         if (getCommand().getCommandDescriptorBlock() instanceof ReadCapacity10)
         {
            if (this.getDeviceCapacity() >= 0xFFFFFFFFL)
            {
               out.writeInt(-1);
            }
            else
            {
               out.writeInt((int) this.getDeviceCapacity());
            }
            out.writeInt(blockLength);
         }
         else if (getCommand().getCommandDescriptorBlock() instanceof ReadCapacity16)
         {
            out.writeLong(this.getDeviceCapacity());
            out.writeInt(blockLength);

            out.writeInt(0);
            out.writeLong(0);
            out.writeLong(0);
         }
         else
         {
            throw new RuntimeException("Invalid CDB passed in ReadCapacityGridTask");
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException("unable to encode READ CAPACITY parameter data");
      }

      this.writeData(ByteBuffer.wrap(bs.toByteArray()));
      this.writeResponse(Status.GOOD, null);
   }
}
