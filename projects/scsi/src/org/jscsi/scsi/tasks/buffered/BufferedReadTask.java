package org.jscsi.scsi.tasks.buffered;

import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.Read6;
import org.jscsi.scsi.protocol.cdb.TransferCDB;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.LogicalBlockAddressOutOfRangeException;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.protocol.sense.exceptions.SynchronousDataTransferErrorException;
import org.jscsi.scsi.tasks.Status;
import org.jscsi.scsi.transport.TargetTransportPort;

public class BufferedReadTask extends BufferedTask
{

   public BufferedReadTask()
   {
      super();
   }

   @Override
   protected void execute(
         ByteBuffer buffer,
         int blockLength,
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry) throws InterruptedException, SenseException
   {
      long capacity = this.getFileCapacity();
      
      TransferCDB cdb = (TransferCDB)command.getCommandDescriptorBlock();
      long lba = cdb.getLogicalBlockAddress();
      long transferLength = cdb.getTransferLength();
      
      // check if transfer would exceed the device size
      if ( lba < 0 || transferLength < 0 || lba > capacity || (lba + transferLength) > capacity)
      {
         switch (cdb.getOperationCode())
         {
            case Read6.OPERATION_CODE:
               throw new LogicalBlockAddressOutOfRangeException(true, true, (byte)4, 1);
            default:
               throw new LogicalBlockAddressOutOfRangeException(true, true, 2);
         }
      }
      
      // duplicate file byte buffer to avoid interference with other tasks
      buffer = buffer.duplicate();
      
      // set file position
      // deviceSize will always be less than Integer.MAX_VALUE so truncating will be safe
      buffer.position( (int)(lba * blockLength) );
      
      // attempt to write data to transport port
      if ( ! this.writeData(buffer) )
      {
         throw new SynchronousDataTransferErrorException();
      }
      
      // read operation complete
      this.writeResponse(Status.GOOD, null);
      
   }
   
   

}
