
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class Write6 extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x0A;

   private long logicalBlockAddress;
   private int transferLength;

   protected Write6()
   {
      super();
   }

   public Write6(long logicalBlockAddress, long transferLength, boolean linked, boolean normalACA)
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
      this.logicalBlockAddress = logicalBlockAddress;
      this.transferLength = (int) transferLength;
   }

   public Write6(long logicalBlockAddress, long transferLength)
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
      this.logicalBlockAddress = (msb >>> 32) | lss;

      this.transferLength = in.readUnsignedByte();
      if (this.transferLength == 0)
      {
         this.transferLength = 256;
      }
      super.setControl(in.readUnsignedByte());

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

         int msb = (int) (this.logicalBlockAddress << 32) & 0x1F;
         int lss = (int) this.logicalBlockAddress & 0xFFFF;
         out.writeByte(msb);
         out.writeShort(lss);
         if (this.transferLength == 256)
         {
            out.writeByte(0);
         }
         else
         {
            out.writeByte(this.transferLength);
         }
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
      return 6;
   }

   public void setLogicalBlockAddress(long logicalBlockAddress)
   {
      this.logicalBlockAddress = logicalBlockAddress;
   }

   public void setTransferLength(int transferLength)
   {
      this.transferLength = transferLength;
   }
}
