
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class Read6 extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x08;

   private long lba;
   private int transferLength;

   public Read6()
   {
      super();
   }

   public Read6(long logicalBlockAddress, long transferLength, boolean linked, boolean normalACA)
   {
      super(linked, normalACA);
      if (transferLength > 256)
      {
         throw new IllegalArgumentException("Transfer length out of bounds for command type");
      }
      if (logicalBlockAddress > 2097152)
      {
         throw new IllegalArgumentException("Logical Block Address out of bounds for command type");
      }
      this.lba = logicalBlockAddress;
      this.transferLength = (int) transferLength;
   }

   public Read6(long logicalBlockAddress, long transferLength)
   {
      this(logicalBlockAddress, transferLength, false, false);
   }

   @Override
   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();

      long msb = in.readUnsignedByte() & 0x1F;
      long lss = in.readUnsignedShort();
      this.lba = (msb << 32) | lss;

      this.transferLength = in.readUnsignedByte();
      super.setControl(in.readUnsignedByte());

      if (this.transferLength == 0)
      {
         this.transferLength = 256;
      }

      if (operationCode != OPERATION_CODE)
      {
         throw new IOException("Invalid operation code: "
               + Integer.toHexString(operationCode));
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

         int msb = (int) (this.lba >>> 32) & 0x1F;
         int lss = (int) this.lba & 0xFFFF;
         out.writeByte(msb);
         out.writeShort(lss);
         out.writeByte(this.transferLength);
         out.writeByte(super.getControl());

         return cdb.toByteArray();
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
   public long getLogicalBlockAddress()
   {
      return this.lba;
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
      return 6;
   }

   public long getLba()
   {
      return lba;
   }

   public void setLba(long lba)
   {
      this.lba = lba;
   }

   public void setTransferLength(int transferLength)
   {
      this.transferLength = transferLength;
   }
}
