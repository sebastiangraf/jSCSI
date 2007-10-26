
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

public class ReceiveDiagnosticResults extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x1C;

   private boolean pcv;
   private int pageCode;
   private int allocationLength;

   protected ReceiveDiagnosticResults()
   {
      super();
   }

   public ReceiveDiagnosticResults(
         boolean pcv,
         int pageCode,
         int allocationLength,
         boolean linked,
         boolean normalACA)
   {
      super(linked, normalACA);

      this.pcv = pcv;
      this.pageCode = pageCode;
      this.allocationLength = allocationLength;
   }

   public ReceiveDiagnosticResults(boolean pcv, int pageCode, int allocationLength)
   {
      this(pcv, pageCode, allocationLength, false, false);
   }

   @Override
   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      this.pcv = (in.readUnsignedByte() & 1) == 1;
      this.pageCode = in.readUnsignedByte();
      this.allocationLength = in.readUnsignedShort();
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
         out.writeByte((this.pcv == true) ? 1 : 0);
         out.writeByte(this.pageCode);
         out.writeShort(this.allocationLength);
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
      return this.allocationLength;
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

   public boolean isPcv()
   {
      return pcv;
   }

   public void setPcv(boolean pcv)
   {
      this.pcv = pcv;
   }

   public int getPageCode()
   {
      return pageCode;
   }

   public void setPageCode(int pageCode)
   {
      this.pageCode = pageCode;
   }

   public void setAllocationLength(int allocationLength)
   {
      this.allocationLength = allocationLength;
   }
}
