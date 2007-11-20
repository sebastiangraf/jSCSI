
package org.jscsi.scsi.tasks.lu;

import java.io.IOException;

import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.Inquiry;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.inquiry.vpd.VPDPage;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.InvalidFieldInCDBException;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.transport.TargetTransportPort;

// TODO: Describe class or interface
public class InquiryTask extends LUTask
{
   public InquiryTask()
   {
      super();
   }
   
   public InquiryTask(
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      super("InquiryTask", targetPort, command, modePageRegistry, inquiryDataRegistry);
   }

   @Override
   protected void execute() throws InterruptedException, SenseException
   {
      CDB cdb = getCommand().getCommandDescriptorBlock();
      assert cdb instanceof Inquiry;

      Inquiry inquiryCDB = (Inquiry) cdb;

      if (inquiryCDB.isEVPD())
      {
         VPDPage vpdPage = getInquiryDataRegistry().getVPDPage(inquiryCDB.getPageCode());
         if (vpdPage != null)
         {
            try
            {
               this.writeData(vpdPage.encode());
            }
            catch (IOException e)
            {
               throw new RuntimeException("Could not encode VPD page", e);
            }
         }
         else
         {
            // Invalid page code
            throw new InvalidFieldInCDBException(true, 2);
         }
      }
      else
      {
         if (inquiryCDB.getPageCode() == 0x00)
         {
            this.writeData(getInquiryDataRegistry().getStandardInquiryData().encode());
         }
         else
         {
            // Invalid page code
            throw new InvalidFieldInCDBException(true, 2);
         }
      }

      this.writeResponse(Status.GOOD, null);
   }
}
