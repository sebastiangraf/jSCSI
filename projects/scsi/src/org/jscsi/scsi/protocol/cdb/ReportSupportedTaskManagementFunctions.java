
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ReportSupportedTaskManagementFunctions extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0xA3;
   public static final int SERVICE_ACTION = 0x0D;

   private long allocationLength;

   public ReportSupportedTaskManagementFunctions()
   {
      super();
   }

   public ReportSupportedTaskManagementFunctions(
         long allocationLength,
         boolean linked,
         boolean normalACA)
   {
      super(linked, normalACA);
      if (allocationLength > 65536)
      {
         throw new IllegalArgumentException("Allocation length out of bounds for command type");
      }
      this.allocationLength = (int) allocationLength;
   }

   public ReportSupportedTaskManagementFunctions(long allocationLength)
   {
      this(allocationLength, false, false);
   }

   @Override
   public void decode(ByteBuffer input) throws BufferUnderflowException, IllegalArgumentException
   {
      byte[] cdb = new byte[this.size()];
      input.get(cdb);
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(cdb));

      try
      {
         int operationCode = in.readUnsignedByte();
         int serviceAction = in.readUnsignedByte() & 0x1F;
         in.readShort();
         this.allocationLength = in.readUnsignedShort();
         in.readByte();
         super.setControl(in.readUnsignedByte());

         if (operationCode != OPERATION_CODE)
         {
            throw new IllegalArgumentException("Invalid operation code: "
                  + Integer.toHexString(operationCode));
         }
         if (serviceAction != SERVICE_ACTION)
         {
            throw new IllegalArgumentException("Invalid service action: "
                  + Integer.toHexString(serviceAction));
         }
      }
      catch (IOException e)
      {
         throw new IllegalArgumentException("Error reading input data.");
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
         out.writeByte(SERVICE_ACTION);
         out.writeShort(0);
         out.writeShort((int) this.allocationLength);
         out.writeByte(0);
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
      return 12;
   }

   public void setAllocationLength(long allocationLength)
   {
      this.allocationLength = allocationLength;
   }
}
