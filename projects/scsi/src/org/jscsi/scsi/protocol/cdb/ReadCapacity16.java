
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class ReadCapacity16 extends AbstractParameterCDB
{
   public static final int OPERATION_CODE = 0x9E;
   public static final int SERVICE_ACTION = 0x10;

   private long logicalBlockAddress;
   private boolean PMI;

   public ReadCapacity16()
   {
      super(OPERATION_CODE);
   }

   public ReadCapacity16(
         int allocationLength,
         boolean pmi,
         boolean linked,
         boolean normalACA,
         long logicalBlockAddress)
   {
      super(OPERATION_CODE, linked, normalACA, allocationLength, 0);

      this.logicalBlockAddress = logicalBlockAddress;
      this.PMI = pmi;
   }

   public ReadCapacity16(int allocationLength, boolean pmi, long logicalBlockAddress)
   {
      this(allocationLength, pmi, false, false, logicalBlockAddress);
   }

   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      int serviceAction = in.readUnsignedByte() & 0x1F;
      this.logicalBlockAddress = in.readLong();
      this.setAllocationLength(in.readInt());
      this.PMI = (in.readByte() & 0x01) == 1;
      super.setControl(in.readUnsignedByte());

      if (operationCode != OPERATION_CODE)
      {
         throw new IOException("Invalid operation code: " + Integer.toHexString(operationCode));
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
         out.writeLong(this.logicalBlockAddress);
         out.writeInt((int)this.getAllocationLength());
         out.writeByte(this.PMI ? 1 : 0);
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
      return 16;
   }

   public int getServiceAction()
   {
      return SERVICE_ACTION;
   }

   public long getLogicalBlockAddress()
   {
      return logicalBlockAddress;
   }

   public void setLogicalBlockAddress(long logicalBlockAddress)
   {
      this.logicalBlockAddress = logicalBlockAddress;
   }

   public boolean isPMI()
   {
      return this.PMI;
   }

   public void setPMI(boolean pmi)
   {
      this.PMI = pmi;
   }
}
