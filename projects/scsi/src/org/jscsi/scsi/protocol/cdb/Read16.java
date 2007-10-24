
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class Read16 extends Read10
{
   public static final int OPERATION_CODE = 0x88;

   private int groupNumber;
   private long logicalBlockAddress;
   private long transferLength;

   protected Read16()
   {
      super();
   }

   public Read16(
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
      // Don't check out of bounds, LONG_MAX is less than ULONG_MAX
      this.transferLength = transferLength;
      this.logicalBlockAddress = logicalBlockAddress;
      this.groupNumber = groupNumber;
   }

   public Read16(long logicalBlockAddress, long transferLength)
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

      // CAUTION: Signed longs represent unsigned longs
      this.logicalBlockAddress = in.readLong();
      this.transferLength = in.readLong();

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

         // CAUTION: Signed longs represent unsigned longs
         out.writeLong(this.logicalBlockAddress);
         out.writeLong(this.transferLength);

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

   public BigInteger getFullLogicalBlockAddress()
   {
      if (this.logicalBlockAddress > 0)
      {
         return BigInteger.valueOf(this.logicalBlockAddress);
      }
      else
      {
         return BigInteger.valueOf(this.logicalBlockAddress).abs().add(
               BigInteger.valueOf(1).shiftLeft(63));
      }
   }

   public BigInteger getFullTransferLength()
   {
      if (this.transferLength > 0)
      {
         return BigInteger.valueOf(this.transferLength);
      }
      else
      {
         return BigInteger.valueOf(this.transferLength).abs().add(
               BigInteger.valueOf(1).shiftLeft(63));
      }
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
      return 16;
   }

   @Override
   public void setGroupNumber(int groupNumber)
   {
      this.groupNumber = groupNumber;
   }

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
