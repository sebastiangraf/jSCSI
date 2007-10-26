
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class ReadCapacity10 extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x25;

   private int lba;
   private boolean pmi;

   public ReadCapacity10()
   {
      super();
   }

   public ReadCapacity10(int lba, boolean pmi, boolean linked, boolean normalACA)
   {
      super(linked, normalACA);

      this.lba = lba;
      this.pmi = pmi;
   }

   public ReadCapacity10(int lba, boolean pmi)
   {
      this(lba, pmi, false, false);
   }

   @Override
   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      in.readByte();
      this.lba = in.readInt();
      in.readByte();
      in.readByte();
      in.readByte();
      this.pmi = (in.readUnsignedByte() & 1) != 0;
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
         out.writeByte(0);
         out.writeInt(this.lba);
         out.writeByte(0);
         out.writeByte(0);
         out.writeByte(0);
         out.writeByte(this.pmi ? 1 : 0);
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
      return 0;
   }

   @Override
   public int size()
   {
      return 10;
   }

   public int getLba()
   {
      return lba;
   }

   public void setLba(int lba)
   {
      this.lba = lba;
   }

   public boolean isPmi()
   {
      return pmi;
   }

   public void setPmi(boolean pmi)
   {
      this.pmi = pmi;
   }
}
