
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class RequestSense extends AbstractParameterCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x03;

   private boolean DESC;

   public RequestSense()
   {
      super(OPERATION_CODE);
   }

   public RequestSense(
         boolean useDescriptorFormat,
         boolean linked,
         boolean normalACA,
         long allocationLength)
   {
      super(OPERATION_CODE, linked, normalACA, allocationLength);
      if (allocationLength > 256)
      {
         throw new IllegalArgumentException("Allocation length out of bounds for command type");
      }
      this.DESC = useDescriptorFormat;
   }

   public RequestSense(boolean useDescriptorFormat, long allocationLength)
   {
      this(useDescriptorFormat, false, false, allocationLength);
   }

   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      int format = in.readUnsignedByte() & 0x01;
      this.DESC = (format == 1);
      in.readShort();
      setAllocationLength(in.readUnsignedByte());
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
         if (this.DESC)
         {
            out.writeByte(1);
         }
         else
         {
            out.writeByte(0);
         }
         out.writeShort(0);
         out.writeByte((int) getAllocationLength());
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

   public boolean isDESC()
   {
      return this.DESC;
   }

   public void setDESC(boolean desc)
   {
      this.DESC = desc;
   }
}
