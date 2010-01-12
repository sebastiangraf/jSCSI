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

package org.jscsi.scsi.protocol.sense.exceptions;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.additional.ActualRetryCount;
import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;

//TODO: Describe class or interface
public class WriteErrorException extends MediumErrorException
{
   private static final long serialVersionUID = 8618668064348731367L;

   private long LBA;
   private byte[] logicalBlockAddress;
   private ActualRetryCount actualRetryCount;

   public WriteErrorException(boolean current, long logicalBlockAddress, int actualRetryCount)
   {
      super(KCQ.WRITE_ERROR, current);
      assert actualRetryCount < 0xFFFF : "actual retry count field out of range";

      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bs);

      this.LBA = logicalBlockAddress;

      try
      {
         out.writeLong(0);
         out.writeLong(logicalBlockAddress);
         out.close();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to create serialize exception parameter", e);
      }

      this.logicalBlockAddress = bs.toByteArray();
      assert this.logicalBlockAddress.length == 8 : "Invalid length for error information field";

      this.actualRetryCount = new ActualRetryCount(actualRetryCount);
   }

   @Override
   protected int getActualRetryCount()
   {
      return this.actualRetryCount.getActualRetryCount();
   }

   @Override
   protected long getLogicalBlockAddress()
   {
      return this.LBA;
   }

   @Override
   protected byte[] getCommandSpecificInformation()
   {
      return null;
   }

   @Override
   protected byte[] getInformation()
   {
      return this.logicalBlockAddress;
   }

   @Override
   protected SenseKeySpecificField getSenseKeySpecific()
   {
      return this.actualRetryCount;
   }

}
