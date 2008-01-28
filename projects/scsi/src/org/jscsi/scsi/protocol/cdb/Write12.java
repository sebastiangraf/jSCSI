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
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class Write12 extends Write10
{
   public static final int OPERATION_CODE = 0xAA;

   protected Write12()
   {
      super(OPERATION_CODE);
   }

   public Write12(
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
      if (transferLength > 4294967296L)
      {
         throw new IllegalArgumentException("Transfer length out of bounds for command type");
      }
      if (logicalBlockAddress > 4294967296L)
      {
         throw new IllegalArgumentException("Logical Block Address out of bounds for command type");
      }
   }

   public Write12(long logicalBlockAddress, long transferLength)
   {
      this(0, false, false, false, false, false, logicalBlockAddress, transferLength);
   }

   @Override
   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      super.decodeByte1(in.readUnsignedByte());

      long mss = in.readUnsignedShort();
      long lss = in.readUnsignedShort();
      setLogicalBlockAddress((mss << 16) | lss);
      mss = in.readUnsignedShort();
      lss = in.readUnsignedShort();
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

         out.writeInt((int) getLogicalBlockAddress());
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

   public int size()
   {
      return 12;
   }

   @Override
   public String toString()
   {
      return "<Write12>";
   }
}
