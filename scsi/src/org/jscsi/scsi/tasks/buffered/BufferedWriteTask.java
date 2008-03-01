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

package org.jscsi.scsi.tasks.buffered;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.protocol.cdb.TransferCDB;
import org.jscsi.scsi.protocol.cdb.Write6;
import org.jscsi.scsi.protocol.sense.exceptions.LogicalBlockAddressOutOfRangeException;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.protocol.sense.exceptions.SynchronousDataTransferErrorException;

public class BufferedWriteTask extends BufferedTask
{
   private static Logger _logger = Logger.getLogger(BufferedWriteTask.class);

   public BufferedWriteTask()
   {
      super("BufferedWriteTask");
   }

   @Override
   protected void execute(ByteBuffer buffer, int blockSize) throws InterruptedException,
         SenseException
   {
      if (_logger.isDebugEnabled())
      {
         _logger.debug("executing task: " + this);
      }
      long capacity = this.getDeviceCapacity();

      TransferCDB cdb = (TransferCDB) getCommand().getCommandDescriptorBlock();
      long lba = cdb.getLogicalBlockAddress();
      long transferLength = cdb.getTransferLength();

      // check if transfer would exceed the device size
      if (lba < 0 || transferLength < 0 || lba > capacity || (lba + transferLength) > capacity)
      {
         switch (cdb.getOperationCode())
         {
            case Write6.OPERATION_CODE :
               throw new LogicalBlockAddressOutOfRangeException(true, true, (byte) 4, 1);
            default :
               throw new LogicalBlockAddressOutOfRangeException(true, true, 2);
         }
      }

      // duplicate file byte buffer to avoid interference with other tasks
      buffer = buffer.duplicate();

      // set file position
      // deviceSize will always be less than Integer.MAX_VALUE so truncating will be safe
      buffer.position((int) (lba * blockSize));
      buffer.limit((int) (transferLength * blockSize) + (int) (lba * blockSize));

      // attempt to read data from transport port
      if (!this.readData(buffer))
      {
         throw new SynchronousDataTransferErrorException();
      }

      // write operation complete
      this.writeResponse(Status.GOOD, null);
   }
}
