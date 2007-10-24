package org.jscsi.scsi.protocol.mode;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

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
      super(PAGE_CODE, SUBPAGE_CODE);
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
   protected void encodeModeParameters(ByteBuffer output)
   {
      ByteArrayOutputStream page = new ByteArrayOutputStream(this.getPageLength());
      DataOutputStream out = new DataOutputStream(page);

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
         out.writeByte(b);

         // byte 5
         b = 0;
         if (this.EN_PS)
         {
            b = 1;
         }
         out.writeByte(b);

         // bytes 6 - 7
         out.writeShort(this.backgroundMediumScanIntervalTime);

         // bytes 8 - 9
         out.writeShort(this.backgroundPrescanTimeLimit);

         // bytes 10 - 11
         out.writeShort(this.minimumIdleTimeBeforeBackgroundScan);

         // bytes 12 - 13
         out.writeShort(this.maximumTimeToSuspectBackgroundScan);

         // bytes 14 - 15
         out.writeShort(0);

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
