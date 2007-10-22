
package org.jscsi.scsi.protocol.mode;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

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
      super(PAGE_CODE);
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
   protected void encodeModeParameters(ByteBuffer output)
   {
      ByteArrayOutputStream page = new ByteArrayOutputStream(this.getPageLength());
      DataOutputStream out = new DataOutputStream(page);

      try
      {
         // byte 2
         out.writeByte(0);

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
         out.writeByte(b);

         // bytes 4 - 7
         out.writeInt(this.idleConditionTimer);

         // bytes 8 - 11
         out.writeInt(this.standbyConditionTimer);

         output.put(page.toByteArray());
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   @Override
   protected int getPageLength()
   {
      return PAGE_LENGTH;
   }
}
