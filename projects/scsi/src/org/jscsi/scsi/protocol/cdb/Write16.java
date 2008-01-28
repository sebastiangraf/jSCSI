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

package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class Write16 extends Write10
{
   public static final int OPERATION_CODE = 0x8A;

   protected Write16()
   {
      super(OPERATION_CODE);
   }

   public Write16(
         int groupNumber,
         boolean dpo,
         boolean fua,
         boolean fua_nv,
         boolean linked,
         boolean normalACA,
         long logicalBlockAddress,
         long transferLength)
   {
      super(OPERATION_CODE, groupNumber, dpo, fua, fua_nv, linked, normalACA, logicalBlockAddress,
            transferLength);

      // Don't check out of bounds, LONG_MAX is less than ULONG_MAX
   }

   public Write16(long logicalBlockAddress, long transferLength)
   {
      this(0, false, false, false, false, false, logicalBlockAddress, transferLength);
   }

   @Override
   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      super.decodeByte1(in.readUnsignedByte());

      // CAUTION: Signed longs represent unsigned longs
      setLogicalBlockAddress(in.readLong());
      long mss = in.readUnsignedShort();
      long lss = in.readUnsignedShort();
      setTransferLength((mss << 16) | lss);

      setGroupNumber(in.readUnsignedByte() & 0x1F);
      super.setControl(in.readUnsignedByte());

      if (operationCode != OPERATION_CODE)
      {
         throw new IOException("Invalid operation code: " + Integer.toHexString(operationCode));
      }
   }

   @Override
   public byte[] encode()
   {
      ByteArrayOutputStream cdb = new ByteArrayOutputStream(this.size());
      DataOutputStream out = new DataOutputStream(cdb);

      try
      {
         out.writeByte(OPERATION_CODE);

         out.writeByte(super.encodeByte1());

         // CAUTION: Signed longs represent unsigned longs
         out.writeLong(getLogicalBlockAddress());
         out.writeInt((int) getTransferLength());

         out.writeByte(getGroupNumber() & 0x1F);
         out.writeByte(super.getControl());

         return cdb.toByteArray();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   public BigInteger getFullLogicalBlockAddress()
   {
      if (getLogicalBlockAddress() > 0)
      {
         return BigInteger.valueOf(getLogicalBlockAddress());
      }
      else
      {
         return BigInteger.valueOf(getLogicalBlockAddress()).abs().add(
               BigInteger.valueOf(1).shiftLeft(63));
      }
   }

   public BigInteger getFullTransferLength()
   {
      if (getTransferLength() > 0)
      {
         return BigInteger.valueOf(getTransferLength());
      }
      else
      {
         return BigInteger.valueOf(getTransferLength()).abs().add(
               BigInteger.valueOf(1).shiftLeft(63));
      }
   }

   public int size()
   {
      return 16;
   }

   @Override
   public String toString()
   {
      return "<Write16>";
   }
}
