
package org.jscsi.scsi.protocol.mode;

import java.io.ByteArrayInputStream;
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
   protected void decodeModeParameters(int dataLength, ByteBuffer input)
         throws BufferUnderflowException, IllegalArgumentException
   {
      try
      {
         byte[] page = new byte[dataLength];
         input.get(page);
         DataInputStream in = new DataInputStream(new ByteArrayInputStream(page));

         // byte 4
         int b = in.readUnsignedByte();
         this.S_L_FULL = ((b >>> 2) & 1) == 1;
         this.LOWIR = ((b >>> 1) & 1) == 1;
         this.EN_BMS = b == 1;

         // byte 5
         this.EN_PS = in.readUnsignedByte() == 1;

         // bytes 6 - 7
         this.backgroundMediumScanIntervalTime = in.readUnsignedShort();

         // bytes 8 - 9
         this.backgroundPrescanTimeLimit = in.readUnsignedShort();

         // bytes 10 - 11
         this.minimumIdleTimeBeforeBackgroundScan = in.readUnsignedShort();

         // bytes 12 - 13
         this.maximumTimeToSuspectBackgroundScan = in.readUnsignedShort();

         // bytes 14 - 15
         in.readShort();
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
}
