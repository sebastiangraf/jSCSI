
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Read10 extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x28;

   private boolean dpo;
   private boolean fua;
   private boolean fua_nv;

   private int groupNumber;
   private long lba;
   private long transferLength;

   public Read10()
   {
      super();
   }

   protected Read10(boolean dpo, boolean fua, boolean fua_nv, boolean linked, boolean normalACA)
   {
      super(linked, normalACA);
      this.dpo = dpo;
      this.fua = fua;
      this.fua_nv = fua_nv;
   }

   public Read10(
         long logicalBlockAddress,
         long transferLength,
         int groupNumber,
         boolean dpo,
         boolean fua,
         boolean fua_nv,
         boolean linked,
         boolean normalACA)
   {
      this(dpo, fua, fua_nv, linked, normalACA);
      if (transferLength > 65536)
      {
         throw new IllegalArgumentException("Transfer length out of bounds for command type");
      }
      if (logicalBlockAddress > 4294967296L)
      {
         throw new IllegalArgumentException("Logical Block Address out of bounds for command type");
      }
      this.transferLength = transferLength;
      this.lba = logicalBlockAddress;
      this.groupNumber = groupNumber;
   }

   @Override
   public void decode(ByteBuffer input) throws IOException
   {
      byte[] cdb = new byte[this.size()];
      input.get(cdb);
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(cdb));

      int operationCode = in.readUnsignedByte();
      this.decodeByte1(in.readUnsignedByte());

      long mss = in.readUnsignedShort();
      long lss = in.readUnsignedShort();
      this.lba = (mss >> 32) | lss;

      this.groupNumber = in.readUnsignedByte() & 0x1F;
      this.transferLength = in.readUnsignedShort();
      super.setControl(in.readUnsignedByte());

      if (operationCode != OPERATION_CODE)
      {
         throw new IllegalArgumentException("Invalid operation code: "
               + Integer.toHexString(operationCode));
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

         out.writeByte(this.encodeByte1());

         int mss = (int) (this.lba << 32);
         int lss = (int) (this.lba & 0xFFFF);
         out.writeShort(mss);
         out.writeShort(lss);
         out.writeByte(this.groupNumber & 0x1F);
         out.writeShort((int) this.transferLength);
         out.writeByte(super.getControl());

         output.put(cdb.toByteArray());
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   protected void decodeByte1(int unsignedByte) throws IllegalArgumentException
   {
      if (((unsignedByte >>> 5) | 0x07) != 0)
      {
         throw new IllegalArgumentException("Read protection information is not supported");
      }

      this.dpo = ((unsignedByte >>> 4) & 0x01) == 1;
      this.fua = ((unsignedByte >>> 3) & 0x01) == 1;
      this.fua_nv = ((unsignedByte >>> 1) & 0x01) == 1;
   }

   protected int encodeByte1()
   {
      int b = 0;
      if (dpo)
      {
         b |= 0x02;
      }
      if (fua)
      {
         b |= 0x08;
      }
      if (fua_nv)
      {
         b |= 0x10;
      }
      return b;
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
      return 10;
   }

   public int getGroupNumber()
   {
      return this.groupNumber;
   }

   public boolean isDPO()
   {
      return this.dpo;
   }

   public boolean isFUA()
   {
      return this.fua;
   }

   public boolean isFUA_NV()
   {
      return this.fua_nv;
   }

   public boolean isDpo()
   {
      return dpo;
   }

   public void setDpo(boolean dpo)
   {
      this.dpo = dpo;
   }

   public boolean isFua()
   {
      return fua;
   }

   public void setFua(boolean fua)
   {
      this.fua = fua;
   }

   public boolean isFua_nv()
   {
      return fua_nv;
   }

   public void setFua_nv(boolean fua_nv)
   {
      this.fua_nv = fua_nv;
   }

   public long getLba()
   {
      return lba;
   }

   public void setLba(long lba)
   {
      this.lba = lba;
   }

   public void setGroupNumber(int groupNumber)
   {
      this.groupNumber = groupNumber;
   }

   public void setTransferLength(long transferLength)
   {
      this.transferLength = transferLength;
   }
}
