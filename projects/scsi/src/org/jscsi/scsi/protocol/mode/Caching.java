
package org.jscsi.scsi.protocol.mode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class Caching extends ModePage
{
   public static final byte PAGE_CODE = 0x08;
   public static final int PAGE_LENGTH = 0x12;

   // byte 2
   private boolean IC;
   private boolean ABPF;
   private boolean CAP;
   private boolean DISC;
   private boolean SIZE;
   private boolean WCE;
   private boolean MF;
   private boolean RCD;
   // byte 3
   private int demandReadRetentionPriority;
   private int writeRetentionPriority;
   // bytes 4 - 5
   private int disablePrefetchTransferLength;
   // bytes 6 - 7
   private int minimumPrefetch;
   // bytes 8 - 9
   private int maximumPrefetch;
   // bytes 10 - 11
   private int maximumPrefetchCeiling;
   // byte 12
   private boolean FSW;
   private boolean LBCSS;
   private boolean DRA;
   private int custom;
   private boolean NV_DIS;
   // byte 13
   private int numberOfCacheSegments;
   // bytes 14 - 15
   private int cacheSegmentSize;

   public Caching()
   {
      super(PAGE_CODE);
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

         // byte 2
         int b = in.readUnsignedByte();
         this.IC = ((b >>> 7) & 1) == 1;
         this.ABPF = ((b >>> 6) & 1) == 1;
         this.CAP = ((b >>> 5) & 1) == 1;
         this.DISC = ((b >>> 4) & 1) == 1;
         this.SIZE = ((b >>> 3) & 1) == 1;
         this.WCE = ((b >>> 2) & 1) == 1;
         this.MF = ((b >>> 1) & 1) == 1;
         this.RCD = (b & 1) == 1;

         // byte 3
         b = in.readUnsignedByte();
         this.demandReadRetentionPriority = ((b >>> 4) & 0xF);
         this.writeRetentionPriority = (b & 0xF);

         // bytes 4 - 5
         this.disablePrefetchTransferLength = in.readUnsignedShort();

         // bytes 6 - 7
         this.minimumPrefetch = in.readUnsignedShort();

         // bytes 8 - 9
         this.maximumPrefetch = in.readUnsignedShort();

         // bytes 10 - 11
         this.maximumPrefetchCeiling = in.readUnsignedShort();

         // byte 12
         b = in.readUnsignedByte();
         this.FSW = ((b >>> 7) & 1) == 1;
         this.LBCSS = ((b >>> 6) & 1) == 1;
         this.DRA = ((b >>> 5) & 1) == 1;
         this.custom = ((b >>> 3) & 2);
         this.NV_DIS = (b & 1) == 1;

         // byte 13
         this.numberOfCacheSegments = in.readUnsignedByte();

         // bytes 14 - 15
         this.cacheSegmentSize = in.readUnsignedShort();

         // byte 16
         in.readByte();

         // bytes 17 - 19
         in.readByte();
         in.readByte();
         in.readByte();
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
         int b = 0;
         if (this.IC)
         {
            b |= 0x80;
         }
         if (this.ABPF)
         {
            b |= 0x40;
         }
         if (this.CAP)
         {
            b |= 0x20;
         }
         if (this.DISC)
         {
            b |= 0x10;
         }
         if (this.SIZE)
         {
            b |= 0x08;
         }
         if (this.WCE)
         {
            b |= 0x04;
         }
         if (this.MF)
         {
            b |= 0x02;
         }
         if (this.RCD)
         {
            b |= 0x01;
         }
         out.writeByte(b);

         // byte 3
         b = (this.demandReadRetentionPriority << 4);
         b |= this.writeRetentionPriority;
         out.writeByte(b);

         // bytes 4 - 5
         out.writeShort(this.disablePrefetchTransferLength);

         // bytes 6 - 7
         out.writeShort(this.minimumPrefetch);

         // bytes 8 - 9
         out.writeShort(this.maximumPrefetch);

         // bytes 10 - 11
         out.writeShort(this.maximumPrefetchCeiling);

         // byte 12
         b = 0;
         if (this.FSW)
         {
            b |= 0x80;
         }
         if (this.LBCSS)
         {
            b |= 0x40;
         }
         if (this.DRA)
         {
            b |= 0x20;
         }
         b |= (this.custom << 3);
         if (this.NV_DIS)
         {
            b |= 0x01;
         }
         out.writeByte(b);

         // byte 13
         out.writeByte(this.numberOfCacheSegments);

         // bytes 14 - 15
         out.writeShort(this.cacheSegmentSize);

         // byte 16
         out.writeByte(0);

         // bytes 17 - 19
         out.writeByte(0);
         out.writeByte(0);
         out.writeByte(0);

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
