
package org.jscsi.scsi.protocol.mode;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

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
      super(PAGE_CODE);
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
   protected void encodeModeParameters(ByteBuffer output)
   {
      ByteArrayOutputStream page = new ByteArrayOutputStream(this.getPageLength());
      DataOutputStream out = new DataOutputStream(page);

      try
      {

         out.writeByte(this.bufferFullRatio);
         out.writeByte(this.bufferEmptyRatio);
         out.writeByte(this.busInactivityLimit);
         out.writeByte(this.disconnectTimeLimit);
         out.writeByte(this.connectTimeLimit);
         out.writeByte(this.maximumBurstSize);

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
         out.writeByte(b);

         //byte 13
         out.writeByte(0);

         // bytes 14 - 15
         out.writeShort(this.firstBurstSize);

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
