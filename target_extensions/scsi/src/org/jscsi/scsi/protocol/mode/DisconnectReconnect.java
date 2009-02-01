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

public class DisconnectReconnect extends ModePage
{
   public static final byte PAGE_CODE = 0x02;
   public static final int PAGE_LENGTH = 0x0E;

   private int bufferFullRatio;
   private int bufferEmptyRatio;
   private int busInactivityLimit;
   private int disconnectTimeLimit;
   private int connectTimeLimit;
   private int maximumBurstSize;
   private boolean EMDP;
   private int fairArbitration;
   private boolean DIMM;
   private int DTDC;
   private int firstBurstSize;

   public DisconnectReconnect()
   {
      super(PAGE_CODE, PAGE_LENGTH);
   }

   @Override
   protected void decodeModeParameters(int dataLength, DataInputStream inputStream)
   throws BufferUnderflowException, IllegalArgumentException
   {
      try
      {
         this.bufferFullRatio = inputStream.readUnsignedByte();
         this.bufferEmptyRatio = inputStream.readUnsignedByte();
         this.busInactivityLimit = inputStream.readUnsignedShort();
         this.disconnectTimeLimit = inputStream.readUnsignedShort();
         this.connectTimeLimit = inputStream.readUnsignedShort();
         this.maximumBurstSize = inputStream.readUnsignedShort();

         // byte 12
         int b12 = inputStream.readUnsignedByte();
         this.EMDP = ((b12 >>> 7) & 1) == 1;
         this.fairArbitration = ((b12 >>> 4) & 7);
         this.DIMM = ((b12 >>> 3) & 1) == 1;
         this.DTDC = (b12 & 7);

         // byte 13
         inputStream.readByte();

         // bytes 14 - 15
         this.firstBurstSize = inputStream.readUnsignedShort();
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

         output.writeByte(this.bufferFullRatio);
         output.writeByte(this.bufferEmptyRatio);
         output.writeByte(this.busInactivityLimit);
         output.writeByte(this.disconnectTimeLimit);
         output.writeByte(this.connectTimeLimit);
         output.writeByte(this.maximumBurstSize);

         // byte 12
         int b = 0;
         if (this.EMDP)
         {
            b |= 0x80;
         }
         b |= (this.fairArbitration << 4);
         if (this.DIMM)
         {
            b |= 8;
         }
         b |= (this.DTDC);
         output.writeByte(b);

         //byte 13
         output.writeByte(0);

         // bytes 14 - 15
         output.writeShort(this.firstBurstSize);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   public int getBufferFullRatio()
   {
      return this.bufferFullRatio;
   }

   public void setBufferFullRatio(int bufferFullRatio)
   {
      this.bufferFullRatio = bufferFullRatio;
   }

   public int getBufferEmptyRatio()
   {
      return this.bufferEmptyRatio;
   }

   public void setBufferEmptyRatio(int bufferEmptyRatio)
   {
      this.bufferEmptyRatio = bufferEmptyRatio;
   }

   public int getBusInactivityLimit()
   {
      return this.busInactivityLimit;
   }

   public void setBusInactivityLimit(int busInactivityLimit)
   {
      this.busInactivityLimit = busInactivityLimit;
   }

   public int getDisconnectTimeLimit()
   {
      return this.disconnectTimeLimit;
   }

   public void setDisconnectTimeLimit(int disconnectTimeLimit)
   {
      this.disconnectTimeLimit = disconnectTimeLimit;
   }

   public int getConnectTimeLimit()
   {
      return this.connectTimeLimit;
   }

   public void setConnectTimeLimit(int connectTimeLimit)
   {
      this.connectTimeLimit = connectTimeLimit;
   }

   public int getMaximumBurstSize()
   {
      return this.maximumBurstSize;
   }

   public void setMaximumBurstSize(int maximumBurstSize)
   {
      this.maximumBurstSize = maximumBurstSize;
   }

   public boolean isEMDP()
   {
      return this.EMDP;
   }

   public void setEMDP(boolean emdp)
   {
      this.EMDP = emdp;
   }

   public int getFairArbitration()
   {
      return this.fairArbitration;
   }

   public void setFairArbitration(int fairArbitration)
   {
      this.fairArbitration = fairArbitration;
   }

   public boolean isDIMM()
   {
      return this.DIMM;
   }

   public void setDIMM(boolean dimm)
   {
      this.DIMM = dimm;
   }

   public int getDTDC()
   {
      return this.DTDC;
   }

   public void setDTDC(int dtdc)
   {
      this.DTDC = dtdc;
   }

   public int getFirstBurstSize()
   {
      return this.firstBurstSize;
   }

   public void setFirstBurstSize(int firstBurstSize)
   {
      this.firstBurstSize = firstBurstSize;
   }
}
