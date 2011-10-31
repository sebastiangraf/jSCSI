/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

public class Write10 extends AbstractTransferCDB
{
   public static final int OPERATION_CODE = 0x2A;

   private boolean DPO;
   private boolean FUA;
   private boolean FUA_NV;

   private int groupNumber;

   public Write10()
   {
      this(OPERATION_CODE);
   }

   protected Write10(int operationCode)
   {
      super(operationCode);
   }

   public Write10(
         int groupNumber,
         boolean dpo,
         boolean fua,
         boolean fua_nv,
         boolean linked,
         boolean normalACA,
         long logicalBlockAddress,
         long transferLength)
   {
      this(OPERATION_CODE, groupNumber, dpo, fua, fua_nv, linked, normalACA, logicalBlockAddress,
            transferLength);
   }

   protected Write10(
         int operationCode,
         int groupNumber,
         boolean dpo,
         boolean fua,
         boolean fua_nv,
         boolean linked,
         boolean normalACA,
         long logicalBlockAddress,
         long transferLength)
   {
      super(operationCode, linked, normalACA, logicalBlockAddress, transferLength);
      this.groupNumber = groupNumber;
      this.DPO = dpo;
      this.FUA = fua;
      this.FUA_NV = fua_nv;
   }

   public Write10(long logicalBlockAddress, long transferLength)
   {
      this(0, false, false, false, false, false, logicalBlockAddress, transferLength);
   }

   protected void decodeByte1(int unsignedByte) throws IllegalArgumentException
   {
      if (((unsignedByte >>> 5) & 0x07) != 0)
      {
         throw new IllegalArgumentException("Write protection information is not supported");
      }

      this.DPO = ((unsignedByte >>> 4) & 0x01) == 1;
      this.FUA = ((unsignedByte >>> 3) & 0x01) == 1;
      this.FUA_NV = ((unsignedByte >>> 1) & 0x01) == 1;
   }

   protected int encodeByte1()
   {
      int b = 0;
      if (DPO)
      {
         b |= 0x02;
      }
      if (FUA)
      {
         b |= 0x08;
      }
      if (FUA_NV)
      {
         b |= 0x10;
      }
      return b;
   }

   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      this.decodeByte1(in.readUnsignedByte());

      long mss = in.readUnsignedShort();
      long lss = in.readUnsignedShort();
      setLogicalBlockAddress((mss << 16) | lss);

      this.groupNumber = in.readUnsignedByte() & 0x1F;
      setTransferLength(in.readUnsignedShort());
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

         out.writeByte(this.encodeByte1());

         out.writeInt((int) getLogicalBlockAddress());
         out.writeByte(this.groupNumber & 0x1F);
         out.writeShort((int) getTransferLength());
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
      return 10;
   }

   public boolean isDPO()
   {
      return this.DPO;
   }

   public void setDPO(boolean dpo)
   {
      this.DPO = dpo;
   }

   public boolean isFUA()
   {
      return this.FUA;
   }

   public void setFUA(boolean fua)
   {
      this.FUA = fua;
   }

   public boolean isFUA_NV()
   {
      return this.FUA_NV;
   }

   public void setFUA_NV(boolean fua_nv)
   {
      this.FUA_NV = fua_nv;
   }

   public int getGroupNumber()
   {
      return this.groupNumber;
   }

   public void setGroupNumber(int groupNumber)
   {
      this.groupNumber = groupNumber;
   }

   @Override
   public String toString()
   {
      return "<Write10>";
   }
}
