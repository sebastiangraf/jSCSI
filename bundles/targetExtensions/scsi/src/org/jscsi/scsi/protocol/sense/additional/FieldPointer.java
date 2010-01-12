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
public class FieldPointer implements SenseKeySpecificField
{

   private boolean CD;
   private boolean BPV;
   private byte bitPointer = -1; // MAX VALUE 0x07 (3-bit), for negative value BPV = 0
   private int fieldPointer; // USHORT_MAX

   public FieldPointer(boolean commandData, byte bitPointer, int fieldPointer)
   {
      this.CD = commandData;
      this.bitPointer = bitPointer;
      this.fieldPointer = fieldPointer;
   }

   public FieldPointer()
   {
      this.CD = false;
      this.bitPointer = -1;
      this.fieldPointer = -1;
   }

   @SuppressWarnings("unchecked")
   public FieldPointer decode(ByteBuffer buffer) throws IOException
   {
      byte[] encodedData = new byte[3];

      buffer.get(encodedData);

      this.CD = ((encodedData[0] >>> 6) & 0x01) == 1;

      this.BPV = ((encodedData[0] >>> 3) & 0x01) == 1;

      this.bitPointer = (byte) (encodedData[0] & 0x07);

      this.fieldPointer = (encodedData[2] & 0xFF); // 8 LSBs
      this.fieldPointer |= ((encodedData[1] & 0xFF) << 8);

      return this;
   }

   public void decode(byte[] header, ByteBuffer buffer) throws IOException
   {
      decode(buffer);
   }

   public byte[] encode()
   {
      byte[] encodedData = new byte[3];

      encodedData[0] = (byte) (1 << 7);
      encodedData[0] |= (byte) (this.CD ? (1 << 6) : 0);
      encodedData[0] |= (byte) (this.BPV ? (1 << 3) : 0);
      encodedData[0] |= (byte) (this.bitPointer & 0x07);

      encodedData[1] = (byte) ((this.fieldPointer >>> 8) & 0xFF);
      encodedData[2] = (byte) (this.fieldPointer & 0xFF);

      return encodedData;
   }

   public boolean isCD()
   {
      return this.CD;
   }

   public void setCD(boolean cd)
   {
      this.CD = cd;
   }

   public boolean isBPV()
   {
      return this.BPV;
   }

   public void setBPV(boolean bpv)
   {
      this.BPV = bpv;
   }

   public byte getBitPointer()
   {
      return this.bitPointer;
   }

   public void setBitPointer(byte bitPointer)
   {
      this.bitPointer = bitPointer;
   }

   public int getFieldPointer()
   {
      return this.fieldPointer;
   }

   public void setFieldPointer(int fieldPointer)
   {
      this.fieldPointer = fieldPointer;
   }

}
