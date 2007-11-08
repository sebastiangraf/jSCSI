
package org.jscsi.scsi.tasks.file;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.ReadCapacity16;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.tasks.Status;
import org.jscsi.scsi.transport.TargetTransportPort;

// TODO: Describe class or interface
public class ReadCapacity16Task extends FileTask
{
   
   public ReadCapacity16Task()
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
      ReadCapacity16 cdb = (ReadCapacity16)command.getCommandDescriptorBlock();
      
      ByteBuffer data = ByteBuffer.allocate((int)cdb.getAllocationLength());
      
      try
      {
         data.putLong(this.getFileCapacity())
             .putLong((long)blockLength)
             .put((byte)0);   // RTO_EN and PROT_EN set to false; do not support protection info
         // The remaining bytes are reserved
      }
      catch (BufferOverflowException e)
      {
         /*
          * The client's allocation length was not enough for return information. SBC-2 specifies
          * that no indication of this event shall be returned to the client.
          * 
          * Note that because and exception is thrown the final write will not actually proceed.
          * This slightly violates SBC-2. However, proper initiator behavior will yield and
          * adequate allocation length and improper initiators will receive a capacity or block
          * length of zero. This is actually probably the best approach.
          */
      }
      
      this.writeData(data);
      this.writeResponse(Status.GOOD, null);
   }

}


