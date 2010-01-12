//Cleversafe open-source code header - Version 1.1 - December 1, 2006
//
//Cleversafe Dispersed Storage(TM) is software for secure, private and
//reliable storage of the world's data using information dispersal.
//
//Copyright (C) 2005-2007 Cleversafe, Inc.
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
//USA.
//
//Contact Information: 
// Cleversafe, 10 W. 35th Street, 16th Floor #84,
// Chicago IL 60616
// email: licensing@cleversafe.org
//
//END-OF-HEADER
//-----------------------
//@author: John Quigley <jquigley@cleversafe.com>
//@date: January 1, 2008
//---------------------

package org.jscsi.scsi.tasks.lu;

import java.io.IOException;

import org.apache.log4j.Logger;
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

//TODO: Describe class or interface
public class InquiryTask extends LUTask
{
   private static Logger _logger = Logger.getLogger(InquiryTask.class);

   public InquiryTask()
   {
      super("InquiryTask");
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
               _logger.debug("cound not encode vpd page in task: " + this);
               throw new RuntimeException("Could not encode VPD page", e);
            }
         }
         else
         {
            // Invalid page code
            _logger.debug("invalid page code in task: " + this);
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
            _logger.debug("invalid page code in task: " + this);
            throw new InvalidFieldInCDBException(true, 2);
         }
      }

      this.writeResponse(Status.GOOD, null);
   }

   public String toString()
   {
      return "<InquiryTask>";
   }
}
