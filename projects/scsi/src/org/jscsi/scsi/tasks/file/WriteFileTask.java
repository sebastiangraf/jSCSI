package org.jscsi.scsi.tasks.file;

import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.TransferCDB;
import org.jscsi.scsi.protocol.cdb.Write6;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.LogicalBlockAddressOutOfRangeException;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.protocol.sense.exceptions.SynchronousDataTransferErrorException;
import org.jscsi.scsi.tasks.Status;
import org.jscsi.scsi.transport.TargetTransportPort;

public class WriteFileTask extends FileTask
{

   public WriteFileTask()
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
      long capacity = this.getFileCapacity();
      
      TransferCDB cdb = (TransferCDB)command.getCommandDescriptorBlock();
      long lba = cdb.getLogicalBlockAddress();
      long transferLength = cdb.getTransferLength();
      
      // check if transfer would exceed the device size
      if ( lba < 0 || transferLength < 0 || lba > capacity || (lba + transferLength) > capacity)
      {
         switch (cdb.getOperationCode())
         {
            case Write6.OPERATION_CODE:
               throw new LogicalBlockAddressOutOfRangeException(true, true, (byte)4, 1);
            default:
               throw new LogicalBlockAddressOutOfRangeException(true, true, 2);
         }
      }
      
      // duplicate file byte buffer to avoid interference with other tasks
      file = file.duplicate();
      
      // set file position
      // deviceSize will always be less than Integer.MAX_VALUE so truncating will be safe
      file.position( (int)(lba * blockLength) );
      
      // attempt to read data from transport port
      if ( ! this.readData(file) )
      {
         throw new SynchronousDataTransferErrorException();
      }
      
      // write operation complete
      this.writeResponse(Status.GOOD, null);
      
   }


}
