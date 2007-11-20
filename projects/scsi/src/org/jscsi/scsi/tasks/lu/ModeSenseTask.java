
package org.jscsi.scsi.tasks.lu;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.ModeSense10;
import org.jscsi.scsi.protocol.cdb.ModeSense6;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePage;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.InvalidFieldInCDBException;
import org.jscsi.scsi.protocol.sense.exceptions.SavingParametersNotSupportedException;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.protocol.sense.exceptions.SynchronousDataTransferErrorException;
import org.jscsi.scsi.transport.TargetTransportPort;

//TODO: Describe class or interface
public class ModeSenseTask extends LUTask
{
   private static Logger _logger = Logger.getLogger(ModeSenseTask.class);

   public ModeSenseTask()
   {
      super();
   }
   
   public ModeSenseTask(
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      super("ModeSenseTask", targetPort, command, modePageRegistry, inquiryDataRegistry);
   }

   @Override
   protected void execute() throws InterruptedException, SenseException
   {
      // The LLBAA field in MODE SENSE (10) is ignored because block descriptors aren't supported
      ModeSense6 cdb = (ModeSense6) getCommand().getCommandDescriptorBlock();

      // The DBD field in MODE SENSE (6,10) are ignored because block descriptor's are supported

      // check the PC field for unsupported page control methods
      switch (cdb.getPC())
      {
         case 0x01 :
            // changeable values are not supported
            throw new InvalidFieldInCDBException(true, (byte) 1, (byte) 2);
         case 0x11 :
            // savable values are not supported
            throw new SavingParametersNotSupportedException(true, (byte) 1, (byte) 2);
      }

      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bs);

      try
      {
         int devspec = 0; // DEVICE-SPECIFIC PARAMETER field
         devspec |= getModePageRegistry().isWP() ? 0x80 : 0x00;
         devspec |= getModePageRegistry().isDPOFUA() ? 0x10 : 0x00;

         if (cdb.getOperationCode() == ModeSense6.OPERATION_CODE)
         {
            _logger.trace("Assembling return data for MODE SENSE (6)");
            out.writeByte(0); // MODE DATA LENGTH placeholder, will be replaced later
            out.writeByte(0x00); // MEDIUM TYPE set to 0x00 according to SBC-2
            out.writeByte(devspec); // DEVICE-SPECIFIC PARAMETER
            out.writeByte(0); // BLOCK DESCRIPTOR LENGTH is 0; block descriptors are unsupported
         }
         else if (cdb.getOperationCode() == ModeSense10.OPERATION_CODE)
         {
            _logger.trace("Assembling return data for MODE SENSE (10)");
            out.writeShort(0); // MODE DATA LENGTH placeholder, will be replaced later
            out.writeByte(0x00); // MEDIUM TYPE set to 0x00 according to SBC-2
            out.writeByte(devspec); // DEVICE-SPECIFIC PARAMETER
            out.writeShort(0); // Reserved, LONGLBA=0, Reserved
            out.writeShort(0); // BLOCK DESCRIPTOR LENGTH is 0; block descriptors are unsupported
         }
         else
         {
            throw new RuntimeException("Invalid operation code for MODE SENSE task: "
                  + cdb.getOperationCode());
         }

         /*
          * The following rules apply when returning mode pages.
          * See SPC-3 6.9.1 for more information.
          * 
          * Note that we don't specially handle vendor specific fields since we
          * don't support them. Instead, we just return INVALID FIELD IN CDB.
          * All mode page objects will encode themselves in the proper way.
          * 
          * Page Code      Subpage Code   Descriptor
          * -------------  -------------  ---------------------------------------
          * 0x01 - 0x1F    0x00 - 0xDF    Return single page from registry
          * 0x01 - 0x1F    0xFF           Return all pages with this page code
          * 0x3F           0x00           Return all pages with subpage code 0x00
          * 0x3F           0xFF           Return all pages
          * 
          * Pages must be returned in ascending order.
          */

         if (cdb.getPageCode() == 0x3F)
         {
            _logger.trace("Initiator requested all mode pages");
            for (ModePage page : getModePageRegistry().get(cdb.getSubPageCode() == 0xFF))
            {
               _logger.trace(String.format(
                     "Encoding mode page: PAGE_CODE=%X, SUB_PAGE_CODE=%X (at buffer position %d)",
                     page.getPageCode(), page.getSubPageCode(), out.size()));
               out.write(page.encode());
               _logger.trace("Encoded mode page up to buffer position: " + out.size());
            }
         }
         else if (!getModePageRegistry().contains((byte) cdb.getPageCode()))
         {
            throw new InvalidFieldInCDBException(true, (byte) 5, 2);
         }
         else
         {
            if (cdb.getSubPageCode() == 0xFF)
            {
               _logger.trace("Initiator requested all pages with PAGE_CODE=" + cdb.getPageCode());
               for (ModePage page : getModePageRegistry().get((byte) cdb.getPageCode()))
               {
                  _logger.trace("Encoding mode page: PAGE_CODE=" + page.getPageCode()
                        + ", SUB_PAGE_CODE=" + page.getSubPageCode() + " to buffer position: "
                        + out.size());
                  out.write(page.encode());
                  _logger.trace("Encoded mode page up to buffer position: " + out.size());
               }
            }
            else
            {
               if (!getModePageRegistry().contains((byte) cdb.getPageCode(), cdb.getSubPageCode()))
               {
                  _logger.trace("Requested mode page does not exist: " + cdb.getPageCode()
                        + ", SUB_PAGE_CODE=" + cdb.getSubPageCode());
                  throw new InvalidFieldInCDBException(true, 3);
               }
               else
               {
                  _logger.trace("Initiator requested mode page: " + cdb.getPageCode()
                        + ", SUB_PAGE_CODE=" + cdb.getSubPageCode());
                  out.write(getModePageRegistry().get((byte) cdb.getPageCode(),
                        cdb.getSubPageCode()).encode());
               }
            }
         }

         ByteBuffer data = ByteBuffer.wrap(bs.toByteArray());

         if (cdb.getOperationCode() == ModeSense6.OPERATION_CODE)
         {
            // MODE DATA LENGTH = data size - len(MODE DATA LENGTH)
            // this should generally be okay because data.limit() will
            // never be larger than BYTE_MAX
            data.put((byte) (data.limit() - 1));
         }
         else
         // ModeSense10.OPERATION_CODE
         {
            // MODE DATA LENGTH = data size - len(MODE DATA LENGTH)
            // this should generally be okay because data.limit() will
            // never be larger than SHORT_MAX
            data.putShort((short) (data.limit() - 2));
         }

         data.position(0);

         if (!this.writeData(data))
         {
            throw new SynchronousDataTransferErrorException();
         }

         this.writeResponse(Status.GOOD, null);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to write mode sense data to byte array");
      }
   }
}
