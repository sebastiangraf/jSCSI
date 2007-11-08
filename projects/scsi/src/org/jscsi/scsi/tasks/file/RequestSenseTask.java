
package org.jscsi.scsi.tasks.file;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.RequestSense;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.DescriptorSenseData;
import org.jscsi.scsi.protocol.sense.FixedSenseData;
import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.tasks.AbstractTask;
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
      
      ByteBuffer data = ByteBuffer.allocate((int)cdb.getAllocationLength());
      
      try
      {
         if ( cdb.isDESC() )
         {
            data.put( (new DescriptorSenseData()).encode() );
         }
         else
         {
            data.put((new FixedSenseData(true, KCQ.NO_ERROR, null, null, null)).encode());
         }
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


