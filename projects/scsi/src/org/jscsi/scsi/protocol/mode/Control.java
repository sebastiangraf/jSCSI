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

public class Control extends ModePage
{
   public static final byte PAGE_CODE = 0x0A;
   public static final int PAGE_LENGTH = 0x0A;

   private int TST = 0x00; // MAX VALUE 0x07 (3-bit)
   private boolean TMF_ONLY = false;
   private boolean D_SENSE = true;
   private boolean GLTSD;
   private boolean RLEC;
   private int queueAlgorithmModifier; // MAX VALUE 0x0F (4-bit)
   private int QERR;
   private boolean RAC;
   private int UA_INTLCK_CTRL;
   private boolean SWP;
   private boolean ATO;
   private boolean TAS;
   private int autoloadMode;
   private int busyTimeoutPeriod; // MAX USHORT_MAX
   private int extendedSelfTestCompletionTime; // MAX USHORT_MAX

   public Control()
   {
      super(PAGE_CODE, PAGE_LENGTH);
   }

   @Override
   protected void decodeModeParameters(int dataLength, DataInputStream inputStream)
   throws BufferUnderflowException, IllegalArgumentException
   {
      try
      {
         int b1 = inputStream.readUnsignedByte();
         int b2 = inputStream.readUnsignedByte();
         int b3 = inputStream.readUnsignedByte();
         int b4 = inputStream.readUnsignedByte();

         // byte 1
         this.TST = (b1 >>> 5) & 0x07;
         this.TMF_ONLY = ((b1 >>> 4) & 0x01) == 1;
         this.D_SENSE = ((b1 >>> 2) & 0x01) == 1;
         this.GLTSD = ((b1 >>> 1) & 0x01) == 1;
         this.RLEC = (b1 & 0x01) == 1;

         // byte 2
         this.queueAlgorithmModifier = (b2 >>> 4) & 0x0F;
         this.QERR = (b2 >>> 1) & 0x3;

         // byte 3
         this.RAC = ((b3 >>> 6) & 0x01) == 1;
         this.UA_INTLCK_CTRL = (b3 >>> 4) & 0x03;
         this.SWP = ((b3 >>> 3) & 0x01) == 1;

         // byte 4
         this.ATO = ((b4 >>> 7) & 0x01) == 1;
         this.TAS = ((b4 >>> 6) & 0x01) == 1;
         this.autoloadMode = b4 & 0x07;

         inputStream.readShort();

         this.busyTimeoutPeriod = inputStream.readUnsignedShort();
         this.extendedSelfTestCompletionTime = inputStream.readUnsignedShort();
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
         // byte #3
         int b = 0;
         b = this.TST << 5;
         if (this.TMF_ONLY)
         {
            b |= 0x10;
         }
         if (this.D_SENSE)
         {
            b |= 4;
         }
         if (this.GLTSD)
         {
            b |= 2;
         }
         if (this.RLEC)
         {
            b |= 1;
         }
         output.writeByte(b);

         // byte #4
         b = 0;
         b = this.queueAlgorithmModifier << 4;
         b |= ((this.QERR << 1) & 0x06);
         output.writeByte(b);

         // byte #5
         b = 0;
         if (this.RAC)
         {
            b |= 0x40;
         }
         b |= ((this.UA_INTLCK_CTRL << 4) & 0x30);
         if (this.SWP)
         {
            b |= 0x08;
         }
         output.writeByte(b);

         // byte #6
         b = 0;
         if (this.ATO)
         {
            b |= 0x80;
         }
         if (this.TAS)
         {
            b |= 0x40;
         }
         b |= this.autoloadMode & 0x07;
         output.writeByte(b);

         // byte #7
         output.writeByte(0);

         //byte #8
         output.writeByte(0);

         // byte #9 - 10
         output.writeShort(this.busyTimeoutPeriod);

         // byte #11 - 12
         output.writeShort(this.extendedSelfTestCompletionTime);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   public int getTST()
   {
      return this.TST;
   }

   public void setTST(int tst)
   {
      this.TST = tst;
   }

   public boolean isTMF_ONLY()
   {
      return this.TMF_ONLY;
   }

   public void setTMF_ONLY(boolean tmf_only)
   {
      this.TMF_ONLY = tmf_only;
   }

   public boolean isD_SENSE()
   {
      return this.D_SENSE;
   }

   public void setD_SENSE(boolean d_sense)
   {
      this.D_SENSE = d_sense;
   }

   public boolean isGLTSD()
   {
      return this.GLTSD;
   }

   public void setGLTSD(boolean gltsd)
   {
      this.GLTSD = gltsd;
   }

   public boolean isRLEC()
   {
      return this.RLEC;
   }

   public void setRLEC(boolean rlec)
   {
      this.RLEC = rlec;
   }

   public int getQueueAlgorithmModifier()
   {
      return this.queueAlgorithmModifier;
   }

   public void setQueueAlgorithmModifier(int queue_algorihtm_modifier)
   {
      this.queueAlgorithmModifier = queue_algorihtm_modifier;
   }

   public int getQERR()
   {
      return this.QERR;
   }

   public void setQERR(int qerr)
   {
      this.QERR = qerr;
   }

   public boolean isRAC()
   {
      return this.RAC;
   }

   public void setRAC(boolean rac)
   {
      this.RAC = rac;
   }

   public int getUA_INTLCK_CTRL()
   {
      return this.UA_INTLCK_CTRL;
   }

   public void setUA_INTLCK_CTRL(int ua_intlck_ctrl)
   {
      this.UA_INTLCK_CTRL = ua_intlck_ctrl;
   }

   public boolean isSWP()
   {
      return this.SWP;
   }

   public void setSWP(boolean swp)
   {
      this.SWP = swp;
   }

   public boolean isATO()
   {
      return this.ATO;
   }

   public void setATO(boolean ato)
   {
      this.ATO = ato;
   }

   public boolean isTAS()
   {
      return this.TAS;
   }

   public void setTAS(boolean tas)
   {
      this.TAS = tas;
   }

   public int getAutoloadMode()
   {
      return this.autoloadMode;
   }

   public void setAutoloadMode(int autoload_mode)
   {
      this.autoloadMode = autoload_mode;
   }

   public int getBusyTimeoutPeriod()
   {
      return this.busyTimeoutPeriod;
   }

   public void setBusyTimeoutPeriod(int busy_timeout_period)
   {
      this.busyTimeoutPeriod = busy_timeout_period;
   }

   public int getExtendedSelfTestCompletionTime()
   {
      return this.extendedSelfTestCompletionTime;
   }

   public void setExtendedSelfTestCompletionTime(int extended_self_test_completion_time)
   {
      this.extendedSelfTestCompletionTime = extended_self_test_completion_time;
   }
}
