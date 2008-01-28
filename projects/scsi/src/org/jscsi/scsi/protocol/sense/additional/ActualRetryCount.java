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

package org.jscsi.scsi.protocol.sense.additional;

import java.io.IOException;
import java.nio.ByteBuffer;

//TODO: Describe class or interface
public class ActualRetryCount implements SenseKeySpecificField
{
   private int actualRetryCount; // USHORT_MAX

   public ActualRetryCount()
   {
      this.actualRetryCount = -1;
   }

   public ActualRetryCount(int actualRetryCount)
   {
      this.actualRetryCount = actualRetryCount;
   }

   public int getActualRetryCount()
   {
      return actualRetryCount;
   }

   public void decode(byte[] header, ByteBuffer buffer) throws IOException
   {
      decode(buffer);
   }

   public byte[] encode()
   {
      byte[] encodedData = new byte[3];

      encodedData[0] = (byte) 0x80;
      encodedData[1] = (byte) ((this.actualRetryCount >>> 8) & 0xFF);
      encodedData[2] = (byte) (this.actualRetryCount & 0xFF);

      return encodedData;
   }

   @SuppressWarnings("unchecked")
   public ActualRetryCount decode(ByteBuffer buffer) throws IOException
   {
      byte[] encodedData = new byte[3];
      buffer.get(encodedData);

      this.actualRetryCount = (encodedData[2] & 0xFF); // 8 LSBs
      this.actualRetryCount |= ((encodedData[1] & 0xFF) << 8);

      return this;
   }

}
