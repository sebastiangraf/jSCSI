
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class Write12 extends Write10
{
   public static final int OPERATION_CODE = 0xAA;

   private int groupNumber;
   private long logicalBlockAddress;
   private long transferLength;

   protected Write12()
   {
      super();
   }

   public Write12(
         long logicalBlockAddress,
         long transferLength,
         int groupNumber,
         boolean dpo,
         boolean fua,
         boolean fua_nv,
         boolean linked,
         boolean normalACA)
   {
      super(dpo, fua, fua_nv, linked, normalACA);
      if (transferLength > 4294967296L)
      {
         throw new IllegalArgumentException("Transfer length out of bounds for command type");
      }
      if (logicalBlockAddress > 4294967296L)
      {
         throw new IllegalArgumentException("Logical Block Address out of bounds for command type");
      }
      this.transferLength = transferLength;
      this.logicalBlockAddress = logicalBlockAddress;
      this.groupNumber = groupNumber;
   }

   public Write12(long logicalBlockAddress, long transferLength)
   {
      this(logicalBlockAddress, transferLength, 0, false, false, false, false, false);
   }

   @Override
   public void decode(ByteBuffer input) throws BufferUnderflowException, IOException
   {
      byte[] cdb = new byte[this.size()];
      input.get(cdb);
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(cdb));

      int operationCode = in.readUnsignedByte();
      super.decodeByte1(in.readUnsignedByte());

      long mss = in.readUnsignedShort();
      long lss = in.readUnsignedShort();
      this.logicalBlockAddress = (mss >>> 32) | lss;

      mss = in.readUnsignedShort();
      lss = in.readUnsignedShort();
      this.transferLength = (mss >>> 32) | lss;

      this.groupNumber = in.readUnsignedByte() & 0x1F;
      super.setControl(in.readUnsignedByte());

      if (operationCode != OPERATION_CODE)
      {
         throw new IllegalArgumentException("Invalid operation code: "
               + Integer.toHexString(operationCode));
      }
   }

   @Override
   public void encode(ByteBuffer output)
   {
      ByteArrayOutputStream cdb = new ByteArrayOutputStream(this.size());
      DataOutputStream out = new DataOutputStream(cdb);

      try
      {
         out.writeByte(OPERATION_CODE);

         out.writeByte(super.encodeByte1());

         int mss = (int) (this.logicalBlockAddress << 32);
         int lss = (int) this.logicalBlockAddress & 0xFFFF;
         out.writeShort(mss);
         out.writeShort(lss);

         mss = (int) (this.transferLength << 32);
         lss = (int) this.transferLength & 0xFFFF;
         out.writeShort(mss);
         out.writeShort(lss);

         out.writeByte(this.groupNumber & 0x1F);
         out.writeByte(super.getControl());

         output.put(cdb.toByteArray());
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   @Override
   public long getAllocationLength()
   {
      return 0;
   }

   @Override
   public int getGroupNumber()
   {
      return this.groupNumber;
   }

   @Override
   public long getLogicalBlockAddress()
   {
      return this.logicalBlockAddress;
   }

   @Override
   public int getOperationCode()
   {
      return OPERATION_CODE;
   }

   @Override
   public long getTransferLength()
   {
      return this.transferLength;
   }

   @Override
   public int size()
   {
      return 12;
   }

   @Override
   public void setGroupNumber(int groupNumber)
   {
      this.groupNumber = groupNumber;
   }

   @Override
   public void setLogicalBlockAddress(long logicalBlockAddress)
   {
      this.logicalBlockAddress = logicalBlockAddress;
   }

   @Override
   public void setTransferLength(long transferLength)
   {
      this.transferLength = transferLength;
   }
}
