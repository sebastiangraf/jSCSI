
package org.jscsi.scsi.tasks.buffered;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.tasks.Status;
import org.jscsi.scsi.transport.TargetTransportPort;

// TODO: Describe class or interface
public class BufferedReadCapacity16Task extends BufferedTask
{
   
   public BufferedReadCapacity16Task()
   {
      super();
   }

   @Override
   protected void execute(
         ByteBuffer file,
         int blockLength,
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry) throws InterruptedException, SenseException
   {
      // NOTE: We ignore the PMI bit because file has no substantial transfer delay point
      
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bs);
      
      try
      {
         out.writeLong(this.getFileCapacity());
         out.writeInt(blockLength);
         out.writeByte(0);    // RTO_EN and PROT_EN set to false; do not support protection info
         // the remaining bytes are reserved
      }
      catch (IOException e1)
      {
         throw new RuntimeException("unable to encode READ CAPACITY (16) parameter data");
      } 
      
      this.writeData(bs.toByteArray());
      this.writeResponse(Status.GOOD, null);
   }

}


