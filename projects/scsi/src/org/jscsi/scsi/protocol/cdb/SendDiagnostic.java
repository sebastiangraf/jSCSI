
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class SendDiagnostic extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x1D;

   public static final int BACKGROUND_SHORT_SELF_TEST = 0x01;
   public static final int BACKGROUND_EXTENDED_SELF_TEST = 0x02;
   public static final int ABORT_BACKGROUND_SELF_TEST = 0x04;
   public static final int FOREGROUND_SHORT_SELF_TEST = 0x05;
   public static final int FOREGROUND_EXTENDED_SELF_TEST = 0x06;

   private int selfTestCode;
   private boolean PF;
   private boolean selfTest;
   private boolean devOffL;
   private boolean unitOffL;
   private int parameterListLength;

   protected SendDiagnostic()
   {
      super(OPERATION_CODE);
   }

   public SendDiagnostic(
         int selfTestCode,
         boolean pf,
         boolean selfTest,
         boolean devOffL,
         boolean unitOffL,
         int parameterListLength,
         boolean linked,
         boolean normalACA)
   {
      super(OPERATION_CODE, linked, normalACA);

      this.selfTestCode = selfTestCode;
      this.PF = pf;
      this.selfTest = selfTest;
      this.devOffL = devOffL;
      this.unitOffL = unitOffL;
      this.parameterListLength = parameterListLength;
   }

   public SendDiagnostic(
         int selfTestCode,
         boolean pf,
         boolean selfTest,
         boolean devOffL,
         boolean unitOffL,
         int parameterListLength)
   {
      this(selfTestCode, pf, selfTest, devOffL, unitOffL, parameterListLength, false, false);
   }

   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      int tmp = in.readUnsignedByte();
      this.unitOffL = (tmp & 0x01) == 1;
      this.devOffL = ((tmp >>> 1) & 1) == 1;
      this.selfTest = ((tmp >>> 2) & 1) == 1;
      this.PF = ((tmp >>> 4) & 1) == 1;
      this.selfTestCode = (tmp >>> 5) & 7;
      in.readByte();
      this.parameterListLength = in.readUnsignedShort();
      super.setControl(in.readUnsignedByte());

      if (operationCode != OPERATION_CODE)
      {
         throw new IOException("Invalid operation code: "
               + Integer.toHexString(operationCode));
      }
   }

   public byte[] encode()
   {
      ByteArrayOutputStream cdb = new ByteArrayOutputStream(this.size());
      DataOutputStream out = new DataOutputStream(cdb);

      try
      {
         out.writeByte(OPERATION_CODE);
         out.writeByte(this.encodeByte1());
         out.writeByte(0);
         out.writeShort(this.parameterListLength);
         out.writeByte(super.getControl());

         return cdb.toByteArray();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   protected int encodeByte1()
   {
      int b = this.selfTestCode;
      b = b << 5;
      if (this.PF)
      {
         b |= 0x10;
      }
      if (this.selfTest)
      {
         b |= 0x04;
      }
      if (this.devOffL)
      {
         b |= 0x02;
      }
      if (this.unitOffL)
      {
         b |= 0x01;
      }
      return b;
   }

   public int size()
   {
      return 6;
   }

   public int getSelfTestCode()
   {
      return this.selfTestCode;
   }

   public void setSelfTestCode(int selfTestCode)
   {
      this.selfTestCode = selfTestCode;
   }

   public boolean isPF()
   {
      return this.PF;
   }

   public void setPF(boolean pf)
   {
      this.PF = pf;
   }

   public boolean isSelfTest()
   {
      return this.selfTest;
   }

   public void setSelfTest(boolean selfTest)
   {
      this.selfTest = selfTest;
   }

   public boolean isDevOffL()
   {
      return this.devOffL;
   }

   public void setDevOffL(boolean devOffL)
   {
      this.devOffL = devOffL;
   }

   public boolean isUnitOffL()
   {
      return this.unitOffL;
   }

   public void setUnitOffL(boolean unitOffL)
   {
      this.unitOffL = unitOffL;
   }

   public int getParameterListLength()
   {
      return this.parameterListLength;
   }

   public void setParameterListLength(int parameterListLength)
   {
      this.parameterListLength = parameterListLength;
   }
}
