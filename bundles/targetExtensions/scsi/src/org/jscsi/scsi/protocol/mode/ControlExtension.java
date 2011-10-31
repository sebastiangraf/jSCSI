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

public class ControlExtension extends ModePage
{
   public static final byte PAGE_CODE = 0x0A;
   public static final int SUBPAGE_CODE = 0x01;
   public static final int PAGE_LENGTH = 0x1C;

   private boolean TCMOS;
   private boolean SCSIP;
   private boolean IALUAE;
   private int initialPriority;

   public ControlExtension()
   {
      super(PAGE_CODE, SUBPAGE_CODE, PAGE_LENGTH);
   }

   @Override
   protected void decodeModeParameters(int dataLength, DataInputStream inputStream)
   throws BufferUnderflowException, IllegalArgumentException
   {
      try
      {
         int b5 = inputStream.readUnsignedByte();
         int b6 = inputStream.readUnsignedByte();

         // byte 5
         this.TCMOS = ((b5 >>> 2) & 1) == 1;
         this.SCSIP = ((b5 >>> 1) & 1) == 1;
         this.IALUAE = (b5 & 1) == 1;

         // byte 6
         this.initialPriority = (b6 & 0xF);

         inputStream.skip(dataLength); // The remaining bytes are reserved
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
         // byte #5
         int b = 0;
         if (this.TCMOS)
         {
            b |= 4;
         }
         if (this.SCSIP)
         {
            b |= 2;
         }
         if (this.IALUAE)
         {
            b |= 1;
         }
         output.writeByte(b);

         // byte #6
         output.writeByte(this.initialPriority);

         // byte #7 - 32
         for (int i = 0; i < 26; i++)
         {
            output.writeByte(0);
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode mode page.");
      }
   }

   public boolean isTCMOS()
   {
      return this.TCMOS;
   }

   public void setTCMOS(boolean tcmos)
   {
      this.TCMOS = tcmos;
   }

   public boolean isSCSIP()
   {
      return this.SCSIP;
   }

   public void setSCSIP(boolean scsip)
   {
      this.SCSIP = scsip;
   }

   public boolean isIALUAE()
   {
      return this.IALUAE;
   }

   public void setIALUAE(boolean ialuae)
   {
      this.IALUAE = ialuae;
   }

   public int getInitialPriority()
   {
      return this.initialPriority;
   }

   public void setInitialPriority(int initialPriority)
   {
      this.initialPriority = initialPriority;
   }
}
