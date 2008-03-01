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

public class ModeSense6 extends AbstractParameterCDB
{
   public static final int OPERATION_CODE = 0x1A;

   public static final int PC_CURRENT_VALUES = 0x00;
   public static final int PC_CHANGEABLE_VALUES = 0x01;
   public static final int PC_DEFAULT_VALUES = 0x03;
   public static final int PC_SAVED_VALUES = 0x04;

   private boolean DBD;
   private int PC;
   private int pageCode;
   private int subPageCode;

   protected ModeSense6(int operationCode)
   {
      super(operationCode);
   }

   public ModeSense6()
   {
      super(OPERATION_CODE);
   }

   public ModeSense6(
         boolean dbd,
         int pageControl,
         int pageCode,
         int subPageCode,
         boolean linked,
         boolean normalACA,
         long allocationLength)
   {
      super(OPERATION_CODE, linked, normalACA, (int) allocationLength, 0);

      if (allocationLength > 65536)
      {
         throw new IllegalArgumentException("Allocation length out of bounds for command type");
      }

      this.DBD = dbd;
      this.PC = pageControl;
      this.pageCode = pageCode;
      this.subPageCode = subPageCode;
   }

   public ModeSense6(
         boolean dbd,
         int pageControl,
         int pageCode,
         int subPageCode,
         long allocationLength)
   {
      this(dbd, pageControl, pageCode, subPageCode, false, false, allocationLength);
   }

   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));
      int tmp;

      int operationCode = in.readUnsignedByte();
      this.DBD = (in.readUnsignedByte() & 0x08) != 0;
      tmp = in.readUnsignedByte();
      this.pageCode = tmp & 0x3F;
      this.PC = tmp >>> 6;
      this.subPageCode = in.readUnsignedByte();
      setAllocationLength(in.readUnsignedByte());

      super.setControl(in.readUnsignedByte());

      if (operationCode != OPERATION_CODE)
      {
         throw new IOException("Invalid operation code: " + Integer.toHexString(operationCode));
      }
   }

   public byte[] encode()
   {
      ByteArrayOutputStream cdb = new ByteArrayOutputStream(this.size());
      DataOutputStream out = new DataOutputStream(cdb);

      try
      {
         out.writeByte(OPERATION_CODE);
         out.writeByte(this.DBD ? 0x08 : 0x00);
         out.writeByte((this.PC << 6) | this.pageCode);
         out.writeByte(this.subPageCode);
         out.writeByte((int) getAllocationLength());
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
      return 6;
   }

   public boolean isDBD()
   {
      return this.DBD;
   }

   public void setDBD(boolean dbd)
   {
      this.DBD = dbd;
   }

   public int getPC()
   {
      return this.PC;
   }

   public void setPC(int pc)
   {
      this.PC = pc;
   }

   public int getPageCode()
   {
      return this.pageCode;
   }

   public void setPageCode(int pageCode)
   {
      this.pageCode = pageCode;
   }

   public int getSubPageCode()
   {
      return this.subPageCode;
   }

   public void setSubPageCode(int subPageCode)
   {
      this.subPageCode = subPageCode;
   }

   public String toString()
   {
      return "<ModeSense6>";
   }
}
