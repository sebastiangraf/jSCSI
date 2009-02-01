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

   public String toString()
   {
      return "<ReportLuns>";
   }
}
