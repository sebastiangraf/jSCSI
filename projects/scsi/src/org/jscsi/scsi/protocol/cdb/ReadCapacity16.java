
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ReadCapacity16 extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x9E;

   private long lba;
   private int serviceAction;
   private int allocationLength;
   private boolean pmi;

   public ReadCapacity16()
   {
      super();
   }

   public ReadCapacity16(
         long lba,
         int serviceAction,
         int allocationLength,
         boolean pmi,
         boolean linked,
         boolean normalACA)
   {
      super(linked, normalACA);

      this.lba = lba;
      this.serviceAction = serviceAction;
      this.allocationLength = allocationLength;
      this.pmi = pmi;
   }

   public ReadCapacity16(long lba, int serviceAction, int allocationLength, boolean pmi)
   {
      this(lba, serviceAction, allocationLength, pmi, false, false);
   }

   @Override
   public void decode(ByteBuffer input) throws IllegalArgumentException
   {
      byte[] cdb = new byte[this.size()];
      input.get(cdb);
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(cdb));

      try
      {
         int operationCode = in.readUnsignedByte();
         this.serviceAction = in.readUnsignedByte() & 0x1F;
         this.lba = in.readLong();
         this.allocationLength = in.readInt();
         this.pmi = (in.readByte() & 0x01) != 0;
         super.setControl(in.readUnsignedByte());

         if (operationCode != OPERATION_CODE)
         {
            throw new IllegalArgumentException("Invalid operation code: "
                  + Integer.toHexString(operationCode));
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
         out.writeByte(this.serviceAction);
         out.writeLong(this.lba);
         out.writeInt(this.allocationLength);
         out.writeByte(this.pmi ? 1 : 0);
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
      return 0;
   }

   @Override
   public int size()
   {
      return 16;
   }

   public long getLba()
   {
      return lba;
   }

   public void setLba(long lba)
   {
      this.lba = lba;
   }

   public int getServiceAction()
   {
      return serviceAction;
   }

   public void setServiceAction(int serviceAction)
   {
      this.serviceAction = serviceAction;
   }

   public boolean isPmi()
   {
      return pmi;
   }

   public void setPmi(boolean pmi)
   {
      this.pmi = pmi;
   }

   public void setAllocationLength(int allocationLength)
   {
      this.allocationLength = allocationLength;
   }
}
