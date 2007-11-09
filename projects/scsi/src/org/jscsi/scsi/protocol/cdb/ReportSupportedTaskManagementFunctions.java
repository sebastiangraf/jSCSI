
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class ReportSupportedTaskManagementFunctions extends AbstractParameterCDB
{
   public static final int OPERATION_CODE = 0xA3;
   public static final int SERVICE_ACTION = 0x0D;

   public ReportSupportedTaskManagementFunctions()
   {
      super(OPERATION_CODE);
   }

   public ReportSupportedTaskManagementFunctions(
         boolean linked,
         boolean normalACA,
         long allocationLength)
   {
      super(OPERATION_CODE, linked, normalACA, allocationLength, 0);
      if (allocationLength > 65536)
      {
         throw new IllegalArgumentException("Allocation length out of bounds for command type");
      }
   }

   public ReportSupportedTaskManagementFunctions(long allocationLength)
   {
      this(false, false, allocationLength);
   }

   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      int serviceAction = in.readUnsignedByte() & 0x1F;
      in.readInt();
      long mss = in.readUnsignedShort();
      long lss = in.readUnsignedShort();
      setAllocationLength( (mss << 16) | lss );
      in.readByte();
      super.setControl(in.readUnsignedByte());

      if (operationCode != OPERATION_CODE)
      {
         throw new IllegalArgumentException("Invalid operation code: "
               + Integer.toHexString(operationCode));
      }
      if (serviceAction != SERVICE_ACTION)
      {
         throw new IOException("Invalid service action: " + Integer.toHexString(serviceAction));
      }
   }

   public byte[] encode()
   {
      ByteArrayOutputStream cdb = new ByteArrayOutputStream(this.size());
      DataOutputStream out = new DataOutputStream(cdb);

      try
      {
         out.writeByte(OPERATION_CODE);
         out.writeByte(SERVICE_ACTION);
         out.writeInt(0);
         out.writeInt((int)getAllocationLength());
         out.writeByte(0);
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
   
   public int getServiceAction()
   {
      return SERVICE_ACTION;
   }
}
