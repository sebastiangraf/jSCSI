
package org.jscsi.scsi.tasks.file;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.RequestSense;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.DescriptorSenseData;
import org.jscsi.scsi.protocol.sense.FixedSenseData;
import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.tasks.Status;
import org.jscsi.scsi.transport.TargetTransportPort;

// TODO: Describe class or interface
public class RequestSenseTask extends FileTask
{

   public RequestSenseTask()
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
      RequestSense cdb = (RequestSense)command.getCommandDescriptorBlock();
      
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bs);
      
      try
      {
         if ( cdb.isDESC() )
         {
            this.writeData((new DescriptorSenseData()).encode());
         }
         else
         {
            this.writeData((new FixedSenseData(true, KCQ.NO_ERROR, null, null, null)).encode());
         }
         
         out.writeLong(this.getFileCapacity());
         out.writeLong((long)blockLength);
         out.writeByte(0);    // RTO_EN and PROT_EN set to false; do not support protection info
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


