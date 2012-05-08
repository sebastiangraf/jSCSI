/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
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

public class ReadWriteErrorRecovery extends ModePage
{
   public static final byte PAGE_CODE = 0x01;
   public static final int PAGE_LENGTH = 0x0A;

   private boolean AWRE;
   private boolean ARRE;
   private boolean TB;
   private boolean RC;
   private boolean EER;
   private boolean PER;
   private boolean DTE;
   private boolean DCR;
   private int readRetryCount;
   private int writeRetryCount;
   private int recoveryTimeLimit;

   public ReadWriteErrorRecovery()
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
         this.AWRE = ((b >>> 7) & 1) == 1;
         this.ARRE = ((b >>> 6) & 1) == 1;
         this.TB = ((b >>> 5) & 1) == 1;
         this.RC = ((b >>> 4) & 1) == 1;
         this.EER = ((b >>> 3) & 1) == 1;
         this.PER = ((b >>> 2) & 1) == 1;
         this.DTE = ((b >>> 1) & 1) == 1;
         this.DCR = (b & 1) == 1;

         // byte 3
         this.readRetryCount = inputStream.readUnsignedByte();

         // byte 4
         inputStream.readByte();

         // byte 5
         inputStream.readByte();

         // byte 6
         inputStream.readByte();

         // byte 7
         inputStream.readByte();

         // byte 8
         this.writeRetryCount = inputStream.readUnsignedByte();

         // byte 9
         inputStream.readByte();

         // bytes 10 - 11
         this.recoveryTimeLimit = inputStream.readUnsignedShort();
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
         if (this.AWRE)
         {
            b |= 0x80;
         }
         if (this.ARRE)
         {
            b |= 0x40;
         }
         if (this.TB)
         {
            b |= 0x20;
         }
         if (this.RC)
         {
            b |= 0x10;
         }
         if (this.EER)
         {
            b |= 0x08;
         }
         if (this.PER)
         {
            b |= 0x04;
         }
         if (this.DTE)
         {
            b |= 0x02;
         }
         if (this.DCR)
         {
            b |= 0x01;
         }
         output.writeByte(b);

         // byte 3
         output.writeByte(this.readRetryCount);

         // byte 4
         output.writeByte(0);

         // byte 5
         output.writeByte(0);

         // byte 6
         output.writeByte(0);

         // byte 7
         output.writeByte(0);

         // byte 8
         output.write(this.writeRetryCount);

         // byte 9
         output.writeByte(0);

         // bytes 10 - 11
         output.writeShort(this.recoveryTimeLimit);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   public boolean isAWRE()
   {
      return this.AWRE;
   }

   public void setAWRE(boolean awre)
   {
      this.AWRE = awre;
   }

   public boolean isARRE()
   {
      return this.ARRE;
   }

   public void setARRE(boolean arre)
   {
      this.ARRE = arre;
   }

   public boolean isTB()
   {
      return this.TB;
   }

   public void setTB(boolean tb)
   {
      this.TB = tb;
   }

   public boolean isRC()
   {
      return this.RC;
   }

   public void setRC(boolean rc)
   {
      this.RC = rc;
   }

   public boolean isEER()
   {
      return this.EER;
   }

   public void setEER(boolean eer)
   {
      this.EER = eer;
   }

   public boolean isPER()
   {
      return this.PER;
   }

   public void setPER(boolean per)
   {
      this.PER = per;
   }

   public boolean isDTE()
   {
      return this.DTE;
   }

   public void setDTE(boolean dte)
   {
      this.DTE = dte;
   }

   public boolean isDCR()
   {
      return this.DCR;
   }

   public void setDCR(boolean dcr)
   {
      this.DCR = dcr;
   }

   public int getReadRetryCount()
   {
      return this.readRetryCount;
   }

   public void setReadRetryCount(int readRetryCount)
   {
      this.readRetryCount = readRetryCount;
   }

   public int getWriteRetryCount()
   {
      return this.writeRetryCount;
   }

   public void setWriteRetryCount(int writeRetryCount)
   {
      this.writeRetryCount = writeRetryCount;
   }

   public int getRecoveryTimeLimit()
   {
      return this.recoveryTimeLimit;
   }

   public void setRecoveryTimeLimit(int recoveryTimeLimit)
   {
      this.recoveryTimeLimit = recoveryTimeLimit;
   }
}
