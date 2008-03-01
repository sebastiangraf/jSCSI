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

package org.jscsi.scsi.protocol.mode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;

public class InformationalExceptionsControl extends ModePage
{
   public static final byte PAGE_CODE = 0x1C;
   public static final int PAGE_LENGTH = 0x0A;

   private boolean PERF;
   private boolean EBF;
   private boolean EWASC;
   private boolean DEXCPT;
   private boolean TEST;
   private boolean LOGERR;
   private int MRIE;
   private long intervalTimer;
   private long reportCount;

   public InformationalExceptionsControl()
   {
      super(PAGE_CODE, PAGE_LENGTH);
   }

   @Override
   protected void decodeModeParameters(int dataLength, DataInputStream inputStream)
   throws BufferUnderflowException, IllegalArgumentException
   {
      try
      {
         // byte 2
         int b = inputStream.readUnsignedByte();
         this.PERF = ((b >>> 7) & 1) == 1;
         this.EBF = ((b >>> 5) & 1) == 1;
         this.EWASC = ((b >>> 4) & 1) == 1;
         this.DEXCPT = ((b >>> 3) & 1) == 1;
         this.TEST = ((b >>> 2) & 1) == 1;
         this.LOGERR = (b & 1) == 1;

         // byte 3
         this.MRIE = inputStream.readUnsignedByte();

         //bytes 4 - 7
         int mss = inputStream.readUnsignedShort();
         int lss = inputStream.readUnsignedShort();
         this.intervalTimer = ((mss << 16) | lss) & 0xFFFFFFFFL;

         // bytes 8 - 11
         mss = inputStream.readUnsignedShort();
         lss = inputStream.readUnsignedShort();
         this.reportCount = ((mss << 16) | lss) & 0xFFFFFFFFL;
      }
      catch (IOException e)
      {
         throw new IllegalArgumentException("Error reading input data.");
      }
   }

   @Override
   protected void encodeModeParameters(DataOutputStream output)
   {
      try
      {
         // byte 2
         int b = 0;
         if (this.PERF)
         {
            b |= 0x80;
         }
         if (this.EBF)
         {
            b |= 0x20;
         }
         if (this.EWASC)
         {
            b |= 0x10;
         }
         if (this.DEXCPT)
         {
            b |= 0x08;
         }
         if (this.TEST)
         {
            b |= 0x04;
         }
         if (this.LOGERR)
         {
            b |= 0x01;
         }
         output.writeByte(b);

         // byte 3
         output.writeByte(this.MRIE);

         // bytes 4 - 7
         output.writeInt((int) this.intervalTimer);

         // bytes 8 - 11
         output.writeInt((int) this.reportCount);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   public boolean isPERF()
   {
      return this.PERF;
   }

   public void setPERF(boolean perf)
   {
      this.PERF = perf;
   }

   public boolean isEBF()
   {
      return this.EBF;
   }

   public void setEBF(boolean ebf)
   {
      this.EBF = ebf;
   }

   public boolean isEWASC()
   {
      return this.EWASC;
   }

   public void setEWASC(boolean ewasc)
   {
      this.EWASC = ewasc;
   }

   public boolean isDEXCPT()
   {
      return this.DEXCPT;
   }

   public void setDEXCPT(boolean dexcpt)
   {
      this.DEXCPT = dexcpt;
   }

   public boolean isTEST()
   {
      return this.TEST;
   }

   public void setTEST(boolean test)
   {
      this.TEST = test;
   }

   public boolean isLOGERR()
   {
      return this.LOGERR;
   }

   public void setLOGERR(boolean logerr)
   {
      this.LOGERR = logerr;
   }

   public int getMRIE()
   {
      return this.MRIE;
   }

   public void setMRIE(int mrie)
   {
      this.MRIE = mrie;
   }

   public long getIntervalTimer()
   {
      return this.intervalTimer;
   }

   public void setIntervalTimer(long intervalTimer)
   {
      this.intervalTimer = intervalTimer;
   }

   public long getReportCount()
   {
      return this.reportCount;
   }

   public void setReportCount(long reportCount)
   {
      this.reportCount = reportCount;
   }
}
