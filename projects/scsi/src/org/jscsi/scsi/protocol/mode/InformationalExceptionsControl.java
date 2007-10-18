package org.jscsi.scsi.protocol.mode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class InformationalExceptionsControl extends ModePage
{
   private static final int PAGE_CODE = 0x1C;
   private static final int PAGE_LENGTH = 0x0A;
   
   static
   {
      ModePage.register((byte)PAGE_CODE, InformationalExceptionsControl.class);
   }
   
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
      super((byte)PAGE_CODE);
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
         this.PERF = ((b >>> 7) & 1) == 1;
         this.EBF = ((b >>> 5) & 1) == 1;
         this.EWASC = ((b >>> 4) & 1) == 1;
         this.DEXCPT = ((b >>> 3) & 1) == 1;
         this.TEST = ((b >>> 2) & 1) == 1;
         this.LOGERR = (b & 1) == 1;
         
         // byte 3
         this.MRIE = in.readUnsignedByte();
         
         //bytes 4 - 7
         this.intervalTimer = in.readInt();
         
         // bytes 8 - 11
         this.reportCount = in.readInt();
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
         out.writeByte(b);
         
         // byte 3
         out.writeByte(this.MRIE);
         
         // bytes 4 - 7
         out.writeInt(this.intervalTimer);
         
         // bytes 8 - 11
         out.writeInt(this.reportCount);
    
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
