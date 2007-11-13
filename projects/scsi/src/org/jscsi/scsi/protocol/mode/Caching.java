
package org.jscsi.scsi.protocol.mode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;

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
   private boolean NV_DIS;
   // byte 13
   private int numberOfCacheSegments;
   // bytes 14 - 15
   private int cacheSegmentSize;

   public Caching()
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
         int b = inputStream.readUnsignedByte();
         this.IC = ((b >>> 7) & 1) == 1;
         this.ABPF = ((b >>> 6) & 1) == 1;
         this.CAP = ((b >>> 5) & 1) == 1;
         this.DISC = ((b >>> 4) & 1) == 1;
         this.SIZE = ((b >>> 3) & 1) == 1;
         this.WCE = ((b >>> 2) & 1) == 1;
         this.MF = ((b >>> 1) & 1) == 1;
         this.RCD = (b & 1) == 1;

         // byte 3
         b = inputStream.readUnsignedByte();
         this.demandReadRetentionPriority = ((b >>> 4) & 0xF);
         this.writeRetentionPriority = (b & 0xF);

         // bytes 4 - 5
         this.disablePrefetchTransferLength = inputStream.readUnsignedShort();

         // bytes 6 - 7
         this.minimumPrefetch = inputStream.readUnsignedShort();

         // bytes 8 - 9
         this.maximumPrefetch = inputStream.readUnsignedShort();

         // bytes 10 - 11
         this.maximumPrefetchCeiling = inputStream.readUnsignedShort();

         // byte 12
         b = inputStream.readUnsignedByte();
         this.FSW = ((b >>> 7) & 1) == 1;
         this.LBCSS = ((b >>> 6) & 1) == 1;
         this.DRA = ((b >>> 5) & 1) == 1;
         this.NV_DIS = (b & 1) == 1;

         // byte 13
         this.numberOfCacheSegments = inputStream.readUnsignedByte();

         // bytes 14 - 15
         this.cacheSegmentSize = inputStream.readUnsignedShort();

         // byte 16
         inputStream.readByte();

         // bytes 17 - 19
         inputStream.readByte();
         inputStream.readByte();
         inputStream.readByte();
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
         output.writeByte(b);

         // byte 3
         b = (this.demandReadRetentionPriority << 4);
         b |= this.writeRetentionPriority;
         output.writeByte(b);

         // bytes 4 - 5
         output.writeShort(this.disablePrefetchTransferLength);

         // bytes 6 - 7
         output.writeShort(this.minimumPrefetch);

         // bytes 8 - 9
         output.writeShort(this.maximumPrefetch);

         // bytes 10 - 11
         output.writeShort(this.maximumPrefetchCeiling);

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
         if (this.NV_DIS)
         {
            b |= 0x01;
         }
         output.writeByte(b);

         // byte 13
         output.writeByte(this.numberOfCacheSegments);

         // bytes 14 - 15
         output.writeShort(this.cacheSegmentSize);

         // byte 16
         output.writeByte(0);

         // bytes 17 - 19
         output.writeByte(0);
         output.writeByte(0);
         output.writeByte(0);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   public boolean isIC()
   {
      return this.IC;
   }

   public void setIC(boolean ic)
   {
      this.IC = ic;
   }

   public boolean isABPF()
   {
      return this.ABPF;
   }

   public void setABPF(boolean abpf)
   {
      this.ABPF = abpf;
   }

   public boolean isCAP()
   {
      return this.CAP;
   }

   public void setCAP(boolean cap)
   {
      this.CAP = cap;
   }

   public boolean isDISC()
   {
      return this.DISC;
   }

   public void setDISC(boolean disc)
   {
      this.DISC = disc;
   }

   public boolean isSIZE()
   {
      return this.SIZE;
   }

   public void setSIZE(boolean size)
   {
      this.SIZE = size;
   }

   public boolean isWCE()
   {
      return this.WCE;
   }

   public void setWCE(boolean wce)
   {
      this.WCE = wce;
   }

   public boolean isMF()
   {
      return this.MF;
   }

   public void setMF(boolean mf)
   {
      this.MF = mf;
   }

   public boolean isRCD()
   {
      return this.RCD;
   }

   public void setRCD(boolean rcd)
   {
      this.RCD = rcd;
   }

   public int getDemandReadRetentionPriority()
   {
      return this.demandReadRetentionPriority;
   }

   public void setDemandReadRetentionPriority(int demandReadRetentionPriority)
   {
      this.demandReadRetentionPriority = demandReadRetentionPriority;
   }

   public int getWriteRetentionPriority()
   {
      return this.writeRetentionPriority;
   }

   public void setWriteRetentionPriority(int writeRetentionPriority)
   {
      this.writeRetentionPriority = writeRetentionPriority;
   }

   public int getDisablePrefetchTransferLength()
   {
      return this.disablePrefetchTransferLength;
   }

   public void setDisablePrefetchTransferLength(int disablePrefetchTransferLength)
   {
      this.disablePrefetchTransferLength = disablePrefetchTransferLength;
   }

   public int getMinimumPrefetch()
   {
      return this.minimumPrefetch;
   }

   public void setMinimumPrefetch(int minimumPrefetch)
   {
      this.minimumPrefetch = minimumPrefetch;
   }

   public int getMaximumPrefetch()
   {
      return this.maximumPrefetch;
   }

   public void setMaximumPrefetch(int maximumPrefetch)
   {
      this.maximumPrefetch = maximumPrefetch;
   }

   public int getMaximumPrefetchCeiling()
   {
      return this.maximumPrefetchCeiling;
   }

   public void setMaximumPrefetchCeiling(int maximumPrefetchCeiling)
   {
      this.maximumPrefetchCeiling = maximumPrefetchCeiling;
   }

   public boolean isFSW()
   {
      return this.FSW;
   }

   public void setFSW(boolean fsw)
   {
      this.FSW = fsw;
   }

   public boolean isLBCSS()
   {
      return this.LBCSS;
   }

   public void setLBCSS(boolean lbcss)
   {
      this.LBCSS = lbcss;
   }

   public boolean isDRA()
   {
      return this.DRA;
   }

   public void setDRA(boolean dra)
   {
      this.DRA = dra;
   }

   public boolean isNV_DIS()
   {
      return this.NV_DIS;
   }

   public void setNV_DIS(boolean nv_dis)
   {
      this.NV_DIS = nv_dis;
   }

   public int getNumberOfCacheSegments()
   {
      return this.numberOfCacheSegments;
   }

   public void setNumberOfCacheSegments(int numberOfCacheSegments)
   {
      this.numberOfCacheSegments = numberOfCacheSegments;
   }

   public int getCacheSegmentSize()
   {
      return this.cacheSegmentSize;
   }

   public void setCacheSegmentSize(int cacheSegmentSize)
   {
      this.cacheSegmentSize = cacheSegmentSize;
   }
}
