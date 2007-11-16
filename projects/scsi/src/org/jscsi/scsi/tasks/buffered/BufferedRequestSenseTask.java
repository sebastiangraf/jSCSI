
package org.jscsi.scsi.tasks.buffered;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.protocol.cdb.RequestSense;
import org.jscsi.scsi.protocol.sense.DescriptorSenseData;
import org.jscsi.scsi.protocol.sense.FixedSenseData;
import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;

// TODO: Describe class or interface
public class BufferedRequestSenseTask extends BufferedTask
{

   public BufferedRequestSenseTask()
   {
      super();
   }

   @Override
   protected void execute(ByteBuffer file, int blockLength) throws InterruptedException,
         SenseException
   {
      RequestSense cdb = (RequestSense) getCommand().getCommandDescriptorBlock();

      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bs);

      try
      {
         if (cdb.isDESC())
         {
            out.write((new DescriptorSenseData()).encode());
         }
         else
         {
            out.write((new FixedSenseData(true, KCQ.NO_ERROR, null, null, null)).encode());
         }

         out.writeLong(this.getFileCapacity());
         out.writeLong((long) blockLength);
         out.writeByte(0); // RTO_EN and PROT_EN set to false; do not support protection info
         // the remaining bytes are reserved
      }
      catch (IOException e1)
      {
         throw new RuntimeException("unable to encode READ CAPACITY (10) parameter data");
      }

      this.writeData(bs.toByteArray());
      this.writeResponse(Status.GOOD, null);
   }
}
