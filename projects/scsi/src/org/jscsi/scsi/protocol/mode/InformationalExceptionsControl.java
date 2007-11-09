
package org.jscsi.scsi.protocol.mode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;

public class InformationalExceptionsControl extends ModePage
{
   public static final byte PAGE_CODE = 0x1C;
   public static final int PAGE_LENGTH = 0x0A;

   private boolean PERF;
   private boolean EBF;
   private boolean EWASC;
   private boolean DEXCPT;
   private boolean TEST;
   private boolean LOGERR;
   private int MRIE;
   private int intervalTimer;
   private int reportCount;

   public InformationalExceptionsControl()
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
         this.PERF = ((b >>> 7) & 1) == 1;
         this.EBF = ((b >>> 5) & 1) == 1;
         this.EWASC = ((b >>> 4) & 1) == 1;
         this.DEXCPT = ((b >>> 3) & 1) == 1;
         this.TEST = ((b >>> 2) & 1) == 1;
         this.LOGERR = (b & 1) == 1;

         // byte 3
         this.MRIE = inputStream.readUnsignedByte();

         //bytes 4 - 7
         this.intervalTimer = inputStream.readInt();

         // bytes 8 - 11
         this.reportCount = inputStream.readInt();
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
         if (this.PERF)
         {
            b |= 0x80;
         }
         if (this.EBF)
         {
            b |= 0x20;
         }
         if (this.EWASC)
         {
            b |= 0x10;
         }
         if (this.DEXCPT)
         {
            b |= 0x08;
         }
         if (this.TEST)
         {
            b |= 0x04;
         }
         if (this.LOGERR)
         {
            b |= 0x01;
         }
         output.writeByte(b);

         // byte 3
         output.writeByte(this.MRIE);

         // bytes 4 - 7
         output.writeInt(this.intervalTimer);

         // bytes 8 - 11
         output.writeInt(this.reportCount);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   public boolean isPERF()
   {
      return this.PERF;
   }

   public void setPERF(boolean perf)
   {
      this.PERF = perf;
   }

   public boolean isEBF()
   {
      return this.EBF;
   }

   public void setEBF(boolean ebf)
   {
      this.EBF = ebf;
   }

   public boolean isEWASC()
   {
      return this.EWASC;
   }

   public void setEWASC(boolean ewasc)
   {
      this.EWASC = ewasc;
   }

   public boolean isDEXCPT()
   {
      return this.DEXCPT;
   }

   public void setDEXCPT(boolean dexcpt)
   {
      this.DEXCPT = dexcpt;
   }

   public boolean isTEST()
   {
      return this.TEST;
   }

   public void setTEST(boolean test)
   {
      this.TEST = test;
   }

   public boolean isLOGERR()
   {
      return this.LOGERR;
   }

   public void setLOGERR(boolean logerr)
   {
      this.LOGERR = logerr;
   }

   public int getMRIE()
   {
      return this.MRIE;
   }

   public void setMRIE(int mrie)
   {
      this.MRIE = mrie;
   }

   public int getIntervalTimer()
   {
      return this.intervalTimer;
   }

   public void setIntervalTimer(int intervalTimer)
   {
      this.intervalTimer = intervalTimer;
   }

   public int getReportCount()
   {
      return this.reportCount;
   }

   public void setReportCount(int reportCount)
   {
      this.reportCount = reportCount;
   }
}
