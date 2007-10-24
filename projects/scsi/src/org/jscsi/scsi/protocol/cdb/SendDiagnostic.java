
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class SendDiagnostic extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x1D;

   public static final int BACKGROUND_SHORT_SELF_TEST = 0x01;
   public static final int BACKGROUND_EXTENDED_SELF_TEST = 0x02;
   public static final int ABORT_BACKGROUND_SELF_TEST = 0x04;
   public static final int FOREGROUND_SHORT_SELF_TEST = 0x05;
   public static final int FOREGROUND_EXTENDED_SELF_TEST = 0x06;

   private int selfTestCode;
   private boolean pf;
   private boolean selfTest;
   private boolean devOffL;
   private boolean unitOffL;
   private int parameterListLength;

   protected SendDiagnostic()
   {
      super();
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
      super(linked, normalACA);

      this.selfTestCode = selfTestCode;
      this.pf = pf;
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

   @Override
   public void decode(ByteBuffer input) throws BufferUnderflowException, IOException
   {
      byte[] cdb = new byte[this.size()];
      input.get(cdb);
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(cdb));

      int tmp = 0;
      try
      {
         int operationCode = in.readUnsignedByte();
         tmp = in.readUnsignedByte();
         this.unitOffL = (tmp & 0x01) == 1;
         this.devOffL = ((tmp >>> 1) & 1) == 1;
         this.selfTest = ((tmp >>> 2) & 1) == 1;
         this.pf = ((tmp >>> 4) & 1) == 1;
         this.selfTestCode = (tmp >>> 5) & 7;
         in.readByte();
         this.parameterListLength = in.readUnsignedShort();
         super.setControl(in.readUnsignedByte());

         if (operationCode != OPERATION_CODE)
         {
            throw new IllegalArgumentException("Invalid operation code: "
                  + Integer.toHexString(operationCode));
         }
      }
      catch (IOException e)
      {
         throw new IllegalArgumentException("Error reading input data.");
      }
   }

   @Override
   public void encode(ByteBuffer output) throws BufferOverflowException
   {
      ByteArrayOutputStream cdb = new ByteArrayOutputStream(this.size());
      DataOutputStream out = new DataOutputStream(cdb);

      try
      {
         out.writeByte(OPERATION_CODE);
         out.writeByte(this.encodeByte1());
         out.writeShort(this.parameterListLength);
         out.writeByte(super.getControl());

         output.put(cdb.toByteArray());
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
      if (this.pf)
      {
         b |= 0x10;
      }
      if (this.selfTest)
      {
         b |= 0x08;
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

   @Override
   public long getAllocationLength()
   {
      return 0;
   }

   @Override
   public long getLogicalBlockAddress()
   {
      return 0;
   }

   @Override
   public int getOperationCode()
   {
      return OPERATION_CODE;
   }

   @Override
   public long getTransferLength()
   {
      return 0;
   }

   @Override
   public int size()
   {
      return 6;
   }

   public int getSelfTestCode()
   {
      return selfTestCode;
   }

   public void setSelfTestCode(int selfTestCode)
   {
      this.selfTestCode = selfTestCode;
   }

   public boolean isPf()
   {
      return pf;
   }

   public void setPf(boolean pf)
   {
      this.pf = pf;
   }

   public boolean isSelfTest()
   {
      return selfTest;
   }

   public void setSelfTest(boolean selfTest)
   {
      this.selfTest = selfTest;
   }

   public boolean isDevOffL()
   {
      return devOffL;
   }

   public void setDevOffL(boolean devOffL)
   {
      this.devOffL = devOffL;
   }

   public boolean isUnitOffL()
   {
      return unitOffL;
   }

   public void setUnitOffL(boolean unitOffL)
   {
      this.unitOffL = unitOffL;
   }

   public int getParameterListLength()
   {
      return parameterListLength;
   }

   public void setParameterListLength(int parameterListLength)
   {
      this.parameterListLength = parameterListLength;
   }
}
