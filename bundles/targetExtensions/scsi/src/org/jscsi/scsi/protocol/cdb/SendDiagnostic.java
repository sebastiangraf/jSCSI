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

public class SendDiagnostic extends AbstractCDB
{
   public static final int OPERATION_CODE = 0x1D;

   public static final int BACKGROUND_SHORT_SELF_TEST = 0x01;
   public static final int BACKGROUND_EXTENDED_SELF_TEST = 0x02;
   public static final int ABORT_BACKGROUND_SELF_TEST = 0x04;
   public static final int FOREGROUND_SHORT_SELF_TEST = 0x05;
   public static final int FOREGROUND_EXTENDED_SELF_TEST = 0x06;

   private int selfTestCode;
   private boolean PF;
   private boolean selfTest;
   private boolean devOffL;
   private boolean unitOffL;
   private int parameterListLength;

   protected SendDiagnostic()
   {
      super(OPERATION_CODE);
   }

   public SendDiagnostic(
         int selfTestCode,
         boolean pf,
         boolean selfTest,
         boolean devOffL,
         boolean unitOffL,
         int parameterListLength,
         boolean linked,
         boolean normalACA)
   {
      super(OPERATION_CODE, linked, normalACA);

      this.selfTestCode = selfTestCode;
      this.PF = pf;
      this.selfTest = selfTest;
      this.devOffL = devOffL;
      this.unitOffL = unitOffL;
      this.parameterListLength = parameterListLength;
   }

   public SendDiagnostic(
         int selfTestCode,
         boolean pf,
         boolean selfTest,
         boolean devOffL,
         boolean unitOffL,
         int parameterListLength)
   {
      this(selfTestCode, pf, selfTest, devOffL, unitOffL, parameterListLength, false, false);
   }

   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      int tmp = in.readUnsignedByte();
      this.unitOffL = (tmp & 0x01) == 1;
      this.devOffL = ((tmp >>> 1) & 1) == 1;
      this.selfTest = ((tmp >>> 2) & 1) == 1;
      this.PF = ((tmp >>> 4) & 1) == 1;
      this.selfTestCode = (tmp >>> 5) & 7;
      in.readByte();
      this.parameterListLength = in.readUnsignedShort();
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
         out.writeByte(0);
         out.writeShort(this.parameterListLength);
         out.writeByte(super.getControl());

         return cdb.toByteArray();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   protected int encodeByte1()
   {
      int b = this.selfTestCode;
      b = b << 5;
      if (this.PF)
      {
         b |= 0x10;
      }
      if (this.selfTest)
      {
         b |= 0x04;
      }
      if (this.devOffL)
      {
         b |= 0x02;
      }
      if (this.unitOffL)
      {
         b |= 0x01;
      }
      return b;
   }

   public int size()
   {
      return 6;
   }

   public int getSelfTestCode()
   {
      return this.selfTestCode;
   }

   public void setSelfTestCode(int selfTestCode)
   {
      this.selfTestCode = selfTestCode;
   }

   public boolean isPF()
   {
      return this.PF;
   }

   public void setPF(boolean pf)
   {
      this.PF = pf;
   }

   public boolean isSelfTest()
   {
      return this.selfTest;
   }

   public void setSelfTest(boolean selfTest)
   {
      this.selfTest = selfTest;
   }

   public boolean isDevOffL()
   {
      return this.devOffL;
   }

   public void setDevOffL(boolean devOffL)
   {
      this.devOffL = devOffL;
   }

   public boolean isUnitOffL()
   {
      return this.unitOffL;
   }

   public void setUnitOffL(boolean unitOffL)
   {
      this.unitOffL = unitOffL;
   }

   public int getParameterListLength()
   {
      return this.parameterListLength;
   }

   public void setParameterListLength(int parameterListLength)
   {
      this.parameterListLength = parameterListLength;
   }
}
