
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class Write6 extends AbstractTransferCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x0A;

   protected Write6()
   {
      super(OPERATION_CODE);
   }

   public Write6(boolean linked, boolean normalACA, long logicalBlockAddress, long transferLength)
   {
      super(OPERATION_CODE, linked, normalACA, logicalBlockAddress, transferLength);
      if (transferLength > 256)
      {
         throw new IllegalArgumentException("Transfer length out of bounds for command type");
      }
      if (logicalBlockAddress > 2097152)
      {
         throw new IllegalArgumentException("Logical Block Address out of bounds for command type");
      }
   }

   public Write6(long logicalBlockAddress, long transferLength)
   {
      this(false, false, logicalBlockAddress, transferLength);
   }

   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();

      long msb = in.readUnsignedByte() & 0x1F;
      long lss = in.readUnsignedShort();
      setLogicalBlockAddress((msb >>> 32) | lss);

      setTransferLength(in.readUnsignedByte());
      if (getTransferLength() == 0)
      {
         setTransferLength(256);
      }
      super.setControl(in.readUnsignedByte());

      if (operationCode != OPERATION_CODE)
      {
         throw new IOException("Invalid operation code: " + Integer.toHexString(operationCode));
      }
   }

   public byte[] encode()
   {
      ByteArrayOutputStream cdb = new ByteArrayOutputStream(this.size());
      DataOutputStream out = new DataOutputStream(cdb);

      try
      {
         out.writeByte(OPERATION_CODE);

         int msb = (int) (getLogicalBlockAddress() << 32) & 0x1F;
         int lss = (int) getLogicalBlockAddress() & 0xFFFF;
         out.writeByte(msb);
         out.writeShort(lss);
         if (getTransferLength() == 256)
         {
            out.writeByte(0);
         }
         else
         {
            out.writeByte((int) getTransferLength());
         }
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
      return 6;
   }
}
