package org.jscsi.scsi.protocol.mode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ControlExtension extends ModePage
{
   private static final int PAGE_CODE = 0x0A;
   private static final int SUBPAGE_CODE = 0x01;
   private static final int PAGE_LENGTH = 0x1C;
   
   static
   {
      ModePage.register((byte)PAGE_CODE, (byte)SUBPAGE_CODE, ControlExtension.class);
   }
   
   private boolean TCMOS;
   private boolean SCSIP;
   private boolean IALUAE;
   private int initialPriority;
      
   public ControlExtension()
   {
      super((byte)PAGE_CODE, SUBPAGE_CODE);
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
         
         int b5 = in.readUnsignedByte();
         int b6 = in.readUnsignedByte();
         
         // byte 5
         this.TCMOS = ((b5 >>> 3) & 1) == 1;
         this.SCSIP = ((b5 >>> 2) & 1) == 1;
         this.IALUAE = (b5 & 1) == 1;
         
         // byte 6
         this.initialPriority = (b6 & 0xF);
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
         // byte #5
         int b = 0;
         if (this.TCMOS)
         {
            b |= 4;
         }
         if (this.SCSIP)
         {
            b |= 2;
         }
         if (this.IALUAE)
         {
            b |= 1;
         }
         out.writeByte(b);
         
         // byte #6
         out.writeByte(this.initialPriority);
         
         // byte #7 - 32
         for (int i=0; i < 26; i++)
         {
            out.writeByte(0);
         }

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
