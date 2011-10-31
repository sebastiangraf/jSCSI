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

package org.jscsi.scsi.protocol.mode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;

public class BackgroundControl extends ModePage
{
   public static final byte PAGE_CODE = 0x1C;
   public static final int SUBPAGE_CODE = 0x01;
   public static final int PAGE_LENGTH = 0x0C;

   private boolean S_L_FULL;
   private boolean LOWIR;
   private boolean EN_BMS;
   private boolean EN_PS;
   private int backgroundMediumScanIntervalTime;
   private int backgroundPrescanTimeLimit;
   private int minimumIdleTimeBeforeBackgroundScan;
   private int maximumTimeToSuspectBackgroundScan;

   public BackgroundControl()
   {
      super(PAGE_CODE, SUBPAGE_CODE, PAGE_LENGTH);
   }

   @Override
   protected void decodeModeParameters(int dataLength, DataInputStream inputStream)
   throws BufferUnderflowException, IllegalArgumentException
   {
      try
      {
         // byte 4
         int b = inputStream.readUnsignedByte();
         this.S_L_FULL = ((b >>> 2) & 1) == 1;
         this.LOWIR = ((b >>> 1) & 1) == 1;
         this.EN_BMS = b == 1;

         // byte 5
         this.EN_PS = inputStream.readUnsignedByte() == 1;

         // bytes 6 - 7
         this.backgroundMediumScanIntervalTime = inputStream.readUnsignedShort();

         // bytes 8 - 9
         this.backgroundPrescanTimeLimit = inputStream.readUnsignedShort();

         // bytes 10 - 11
         this.minimumIdleTimeBeforeBackgroundScan = inputStream.readUnsignedShort();

         // bytes 12 - 13
         this.maximumTimeToSuspectBackgroundScan = inputStream.readUnsignedShort();

         // bytes 14 - 15
         inputStream.readShort();
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
         // byte 4
         int b = 0;
         if (this.S_L_FULL)
         {
            b |= 4;
         }
         if (this.LOWIR)
         {
            b |= 2;
         }
         if (this.EN_BMS)
         {
            b |= 1;
         }
         output.writeByte(b);

         // byte 5
         b = 0;
         if (this.EN_PS)
         {
            b = 1;
         }
         output.writeByte(b);

         // bytes 6 - 7
         output.writeShort(this.backgroundMediumScanIntervalTime);

         // bytes 8 - 9
         output.writeShort(this.backgroundPrescanTimeLimit);

         // bytes 10 - 11
         output.writeShort(this.minimumIdleTimeBeforeBackgroundScan);

         // bytes 12 - 13
         output.writeShort(this.maximumTimeToSuspectBackgroundScan);

         // bytes 14 - 15
         output.writeShort(0);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   public boolean isS_L_FULL()
   {
      return this.S_L_FULL;
   }

   public void setS_L_FULL(boolean s_l_full)
   {
      this.S_L_FULL = s_l_full;
   }

   public boolean isLOWIR()
   {
      return this.LOWIR;
   }

   public void setLOWIR(boolean lowir)
   {
      this.LOWIR = lowir;
   }

   public boolean isEN_BMS()
   {
      return this.EN_BMS;
   }

   public void setEN_BMS(boolean en_bms)
   {
      this.EN_BMS = en_bms;
   }

   public boolean isEN_PS()
   {
      return this.EN_PS;
   }

   public void setEN_PS(boolean en_ps)
   {
      this.EN_PS = en_ps;
   }

   public int getBackgroundMediumScanIntervalTime()
   {
      return this.backgroundMediumScanIntervalTime;
   }

   public void setBackgroundMediumScanIntervalTime(int backgroundMediumScanIntervalTime)
   {
      this.backgroundMediumScanIntervalTime = backgroundMediumScanIntervalTime;
   }

   public int getBackgroundPrescanTimeLimit()
   {
      return this.backgroundPrescanTimeLimit;
   }

   public void setBackgroundPrescanTimeLimit(int backgroundPrescanTimeLimit)
   {
      this.backgroundPrescanTimeLimit = backgroundPrescanTimeLimit;
   }

   public int getMinimumIdleTimeBeforeBackgroundScan()
   {
      return this.minimumIdleTimeBeforeBackgroundScan;
   }

   public void setMinimumIdleTimeBeforeBackgroundScan(int minimumIdleTimeBeforeBackgroundScan)
   {
      this.minimumIdleTimeBeforeBackgroundScan = minimumIdleTimeBeforeBackgroundScan;
   }

   public int getMaximumTimeToSuspectBackgroundScan()
   {
      return this.maximumTimeToSuspectBackgroundScan;
   }

   public void setMaximumTimeToSuspectBackgroundScan(int maximumTimeToSuspectBackgroundScan)
   {
      this.maximumTimeToSuspectBackgroundScan = maximumTimeToSuspectBackgroundScan;
   }
}
