
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
