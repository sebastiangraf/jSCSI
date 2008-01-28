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

package org.jscsi.scsi.protocol.sense;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException.ResponseCode;

//TODO: Describe class or interface
public class DescriptorSenseData extends SenseData
{
   /**
    * Constructs a descriptor format sense data with 
    */
   public DescriptorSenseData()
   {
      super(ResponseCode.valueOf(true, true), KCQ.NO_ERROR, null, null, null);
   }

   @Override
   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      throw new RuntimeException("not implemented");
   }

   @Override
   public void decodeSenseKeySpecific(SenseKeySpecificField field) throws IOException
   {
      throw new RuntimeException("not implemented");
   }

   @Override
   public byte[] encode()
   {
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bs);

      try
      {
         out.writeByte(this.getResponseCode() & 0x7F); // RESPONSE CODE (wipe Reserved bit)
         out.writeByte(this.getSenseKey().value());
         out.writeByte(this.getSenseCode());
         out.writeByte(this.getSenseCodeQualifier());
         out.writeInt(0); // 3-byte reserved field, ADDITIONAL SENSE LENGTH = 0 
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode descriptor format sense data");
      }

      return bs.toByteArray();
   }

}
