
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class Write12 extends Write10
{
   public static final int OPERATION_CODE = 0xAA;

   protected Write12()
   {
      super(OPERATION_CODE);
   }

   public Write12(
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
      if (transferLength > 4294967296L)
      {
         throw new IllegalArgumentException("Transfer length out of bounds for command type");
      }
      if (logicalBlockAddress > 4294967296L)
      {
         throw new IllegalArgumentException("Logical Block Address out of bounds for command type");
      }
   }

   public Write12(long logicalBlockAddress, long transferLength)
   {
      this(0, false, false, false, false, false, logicalBlockAddress, transferLength);
   }

   @Override
   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      super.decodeByte1(in.readUnsignedByte());

      long mss = in.readUnsignedShort();
      long lss = in.readUnsignedShort();
      setLogicalBlockAddress( (mss << 16) | lss );
      mss = in.readUnsignedShort();
      lss = in.readUnsignedShort();
      setTransferLength( (mss << 16) | lss );

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

         out.writeInt((int) getLogicalBlockAddress());
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

   public int size()
   {
      return 12;
   }
}
