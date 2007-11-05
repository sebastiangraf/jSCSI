
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
