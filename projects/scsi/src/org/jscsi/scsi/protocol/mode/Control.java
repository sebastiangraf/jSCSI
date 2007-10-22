
package org.jscsi.scsi.protocol.mode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class Control extends ModePage
{
   public static final byte PAGE_CODE = 0x0A;
   public static final int PAGE_LENGTH = 0x0A;

   private int TST = 0x00; // MAX VALUE 0x07 (3-bit)
   private boolean TMF_ONLY = false;
   private boolean D_SENSE = true;
   private boolean GLTSD;
   private boolean RLEC;
   private int QUEUE_ALGORIHTM_MODIFIER; // MAX VALUE 0x0F (4-bit)
   private int QERR;
   private boolean VS;
   private boolean RAC;
   private int UA_INTLCK_CTRL;
   private boolean SWP;
   private boolean ATO;
   private boolean TAS;
   private int AUTOLOAD_MODE;
   private int BUSY_TIMEOUT_PERIOD; // MAX USHORT_MAX
   private int EXTENDED_SELF_TEST_COMPLETION_TIME; // MAX USHORT_MAX

   public Control()
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

         int b1 = in.readUnsignedByte();
         int b2 = in.readUnsignedByte();
         int b3 = in.readUnsignedByte();
         int b4 = in.readUnsignedByte();

         // byte 1
         this.TST = (b1 >>> 5) & 0x07;
         this.TMF_ONLY = ((b1 >>> 4) & 0x01) == 1;
         this.D_SENSE = ((b1 >>> 2) & 0x01) == 1;
         this.GLTSD = ((b1 >>> 1) & 0x01) == 1;
         this.RLEC = (b1 & 0x01) == 1;

         // byte 2
         this.QUEUE_ALGORIHTM_MODIFIER = (b2 >>> 4) & 0x0F;
         this.QERR = (b2 >>> 1) & 0x3;

         // byte 3
         this.VS = ((b3 >>> 7) & 0x01) == 1;
         this.RAC = ((b3 >>> 6) & 0x01) == 1;
         this.UA_INTLCK_CTRL = (b3 >>> 4) & 0x03;
         this.SWP = ((b3 >>> 3) & 0x01) == 1;

         // byte 4
         this.ATO = ((b4 >>> 7) & 0x01) == 1;
         this.TAS = ((b4 >>> 6) & 0x01) == 1;
         this.AUTOLOAD_MODE = b4 & 0x07;

         in.readShort();

         this.BUSY_TIMEOUT_PERIOD = in.readUnsignedShort();
         this.EXTENDED_SELF_TEST_COMPLETION_TIME = in.readUnsignedShort();
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
         // byte #3
         int b = 0;
         b = this.TST << 5;
         if (this.TMF_ONLY)
         {
            b |= 0x10;
         }
         if (this.D_SENSE)
         {
            b |= 4;
         }
         if (this.GLTSD)
         {
            b |= 2;
         }
         if (this.RLEC)
         {
            b |= 1;
         }
         out.writeByte(b);

         // byte #4
         b = 0;
         b = this.QUEUE_ALGORIHTM_MODIFIER << 4;
         b |= ((this.QERR << 1) & 0x06);
         out.writeByte(b);

         // byte #5
         b = 0;
         if (this.VS)
         {
            b |= 0x80;
         }
         if (this.RAC)
         {
            b |= 0x40;
         }
         b |= ((this.UA_INTLCK_CTRL << 4) & 0x30);
         if (this.SWP)
         {
            b |= 0x08;
         }
         out.writeByte(b);

         // byte #6
         b = 0;
         if (this.ATO)
         {
            b |= 0x80;
         }
         if (this.TAS)
         {
            b |= 0x40;
         }
         b |= this.AUTOLOAD_MODE & 0x07;
         out.writeByte(b);

         // byte #7
         out.writeByte(0);

         //byte #8
         out.writeByte(0);

         // byte #9 - 10
         out.writeShort(this.BUSY_TIMEOUT_PERIOD);

         // byte #11 - 12
         out.writeShort(this.EXTENDED_SELF_TEST_COMPLETION_TIME);

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
