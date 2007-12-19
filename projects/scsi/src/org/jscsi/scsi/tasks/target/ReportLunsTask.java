
package org.jscsi.scsi.tasks.target;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.ReportLuns;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.transport.TargetTransportPort;

public class ReportLunsTask extends TargetTask
{
   private static Logger _logger = Logger.getLogger(ReportLunsTask.class);
   
   public ReportLunsTask()
   {
      super("ReportLunsTask");
   }
   
   public ReportLunsTask(
         Set<Long> logicalUnits,
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      super("ReportLuns", logicalUnits, targetPort, command, modePageRegistry, inquiryDataRegistry);
   }

   @Override
   protected void execute() throws InterruptedException, SenseException
   {
      _logger.debug("executing ReportLuns task");

      ReportLuns cdb = (ReportLuns) getCommand().getCommandDescriptorBlock();
      ByteBuffer data = ByteBuffer.allocate((int) cdb.getAllocationLength());

      try
      {
         if (cdb.getSelectReport() == 0x01)
         {
            // SELECT REPORT 0x01
            // Report only well known logical units. Because this implementation does not
            // support well known logical units, zero LUs are returned.
            data.putLong(0); // 4-byte LUN LIST LENGTH = 0, 4-byte reserved field
            _logger.warn("unsupported request to report well known logical units");
         }
         else
         {
            _logger.debug("request to report logical units");
            // SELECT REPORT 0x00 or 0x02
            data.putInt(this.getLogicalUnits().size() * 8); // LUN LIST LENGTH (each entry is 8 bytes)
            data.putInt(0); // 4-byte reserved field
            for (long lun : this.getLogicalUnits())
            {
               data.putLong(lun);
            }
         }

      }
      catch (BufferOverflowException e)
      {
         _logger.warn("BufferOverflowException when writing out to transportport");
         /*
          * The client's allocation length was not enough for return information. SBC-2 specifies
          * that no indication of this event shall be returned to the client.
          * 
          * Note that because and exception is thrown the final write will not actually proceed.
          * This slightly violates SBC-2. However, proper initiator behavior will yield and adequate
          * allocation length and improper initiators will receive a capacity or block length of
          * zero. This is actually probably the best approach.
          */
      }

      _logger.debug("calling writeData from ReportLunsTask");
      this.writeData(data);
      this.writeResponse(Status.GOOD, null);

      _logger.debug("completed ReportLuns task");
      
      // TODO Auto-generated method stub

   }
}
