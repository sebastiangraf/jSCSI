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

public class PowerCondition extends ModePage
{
   public static final byte PAGE_CODE = 0x1A;
   public static final int PAGE_LENGTH = 0x0A;

   private boolean idle;
   private boolean standby;
   private int idleConditionTimer;
   private int standbyConditionTimer;

   public PowerCondition()
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
         inputStream.readByte();

         // byte 3
         int b = inputStream.readUnsignedByte();
         this.idle = ((b >>> 1) & 1) == 1;
         this.standby = (b & 1) == 1;

         //bytes 4 - 7
         this.idleConditionTimer = inputStream.readInt();

         // bytes 8 - 11
         this.standbyConditionTimer = inputStream.readInt();
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
         output.writeByte(0);

         // byte 3
         int b = 0;
         if (this.idle)
         {
            b |= 2;
         }
         if (this.standby)
         {
            b |= 1;
         }
         output.writeByte(b);

         // bytes 4 - 7
         output.writeInt(this.idleConditionTimer);

         // bytes 8 - 11
         output.writeInt(this.standbyConditionTimer);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   public boolean isIdle()
   {
      return this.idle;
   }

   public void setIdle(boolean idle)
   {
      this.idle = idle;
   }

   public boolean isStandby()
   {
      return this.standby;
   }

   public void setStandby(boolean standby)
   {
      this.standby = standby;
   }

   public int getIdleConditionTimer()
   {
      return this.idleConditionTimer;
   }

   public void setIdleConditionTimer(int idleConditionTimer)
   {
      this.idleConditionTimer = idleConditionTimer;
   }

   public int getStandbyConditionTimer()
   {
      return this.standbyConditionTimer;
   }

   public void setStandbyConditionTimer(int standbyConditionTimer)
   {
      this.standbyConditionTimer = standbyConditionTimer;
   }
}
