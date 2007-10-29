
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class Write16 extends Write10
{
   public static final int OPERATION_CODE = 0x8A;

   protected Write16()
   {
      super(OPERATION_CODE);
   }

   public Write16(
         int groupNumber,
         boolean dpo,
         boolean fua,
         boolean fua_nv,
         boolean linked,
         boolean normalACA,
         long logicalBlockAddress,
         long transferLength)
   {
      super(OPERATION_CODE, groupNumber, dpo, fua, fua_nv, linked, normalACA, logicalBlockAddress,
            transferLength);

      // Don't check out of bounds, LONG_MAX is less than ULONG_MAX
   }

   public Write16(long logicalBlockAddress, long transferLength)
   {
      this(0, false, false, false, false, false, logicalBlockAddress, transferLength);
   }

   @Override
   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      super.decodeByte1(in.readUnsignedByte());

      // CAUTION: Signed longs represent unsigned longs
      setLogicalBlockAddress(in.readLong());
      setTransferLength(in.readInt());

      setGroupNumber(in.readUnsignedByte() & 0x1F);
      super.setControl(in.readUnsignedByte());

      if (operationCode != OPERATION_CODE)
      {
         throw new IOException("Invalid operation code: " + Integer.toHexString(operationCode));
      }
   }

   @Override
   public byte[] encode()
   {
      ByteArrayOutputStream cdb = new ByteArrayOutputStream(this.size());
      DataOutputStream out = new DataOutputStream(cdb);

      try
      {
         out.writeByte(OPERATION_CODE);

         out.writeByte(super.encodeByte1());

         // CAUTION: Signed longs represent unsigned longs
         out.writeLong(getLogicalBlockAddress());
         out.writeInt((int) getTransferLength());

         out.writeByte(getGroupNumber() & 0x1F);
         out.writeByte(super.getControl());

         return cdb.toByteArray();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   public BigInteger getFullLogicalBlockAddress()
   {
      if (getLogicalBlockAddress() > 0)
      {
         return BigInteger.valueOf(getLogicalBlockAddress());
      }
      else
      {
         return BigInteger.valueOf(getLogicalBlockAddress()).abs().add(
               BigInteger.valueOf(1).shiftLeft(63));
      }
   }

   public BigInteger getFullTransferLength()
   {
      if (getTransferLength() > 0)
      {
         return BigInteger.valueOf(getTransferLength());
      }
      else
      {
         return BigInteger.valueOf(getTransferLength()).abs().add(
               BigInteger.valueOf(1).shiftLeft(63));
      }
   }

   public int size()
   {
      return 16;
   }
}
