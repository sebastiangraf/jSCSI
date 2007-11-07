
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class ReadCapacity16 extends AbstractTransferCDB
{
   public static final int OPERATION_CODE = 0x9E;
   public static final int SERVICE_ACTION = 0x10;

   private int allocationLength;
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
      super(OPERATION_CODE, linked, normalACA, logicalBlockAddress, 0);

      this.allocationLength = allocationLength;
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
      setLogicalBlockAddress(in.readLong());
      this.allocationLength = in.readInt();
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
         out.writeLong(getLogicalBlockAddress());
         out.writeInt(this.allocationLength);
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

   public int getAllocationLength()
   {
      return this.allocationLength;
   }

   public void setAllocationLength(int allocationLength)
   {
      this.allocationLength = allocationLength;
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
