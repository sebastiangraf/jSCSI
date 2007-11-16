//
// Cleversafe open-source code header - Version 1.1 - December 1, 2006
//
// Cleversafe Dispersed Storage(TM) is software for secure, private and
// reliable storage of the world's data using information dispersal.
//
// Copyright (C) 2005-2007 Cleversafe, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
// USA.
//
// Contact Information: Cleversafe, 10 W. 35th Street, 16th Floor #84,
// Chicago IL 60616
// email licensing@cleversafe.org
//
// END-OF-HEADER
//-----------------------
// @author: jquigley
//
// Date: Nov 16, 2007
//---------------------

package org.jscsi.scsi.tasks.lu;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.protocol.cdb.RequestSense;
import org.jscsi.scsi.protocol.sense.DescriptorSenseData;
import org.jscsi.scsi.protocol.sense.FixedSenseData;
import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.tasks.AbstractTask;

public class RequestSenseTask extends AbstractTask
{
   private static Logger _logger = Logger.getLogger(RequestSenseTask.class);

   public RequestSenseTask()
   {
      super();
   }

   @Override
   protected void execute(ByteBuffer file, int blockLength) throws InterruptedException,
   SenseException
   {
      RequestSense cdb = (RequestSense) getCommand().getCommandDescriptorBlock();

      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bs);

      try
      {
         if (cdb.isDESC())
         {
            out.write((new DescriptorSenseData()).encode());
         }
         else
         {
            out.write((new FixedSenseData(true, KCQ.NO_ERROR, null, null, null)).encode());
         }

         out.writeLong(this.getFileCapacity());
         out.writeLong((long) blockLength);
         out.writeByte(0); // RTO_EN and PROT_EN set to false; do not support protection info
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


