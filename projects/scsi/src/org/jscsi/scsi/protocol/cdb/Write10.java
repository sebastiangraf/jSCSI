
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class Write10 extends AbstractTransferCDB
{
   public static final int OPERATION_CODE = 0x2A;

   private boolean DPO;
   private boolean FUA;
   private boolean FUA_NV;

   private int groupNumber;

   public Write10()
   {
      this(OPERATION_CODE);
   }

   protected Write10(int operationCode)
   {
      super(operationCode);
   }

   public Write10(
         int groupNumber,
         boolean dpo,
         boolean fua,
         boolean fua_nv,
         boolean linked,
         boolean normalACA,
         long logicalBlockAddress,
         long transferLength)
   {
      this(OPERATION_CODE, groupNumber, dpo, fua, fua_nv, linked, normalACA, logicalBlockAddress,
            transferLength);
   }

   protected Write10(
         int operationCode,
         int groupNumber,
         boolean dpo,
         boolean fua,
         boolean fua_nv,
         boolean linked,
         boolean normalACA,
         long logicalBlockAddress,
         long transferLength)
   {
      super(operationCode, linked, normalACA, logicalBlockAddress, transferLength);
      this.groupNumber = groupNumber;
      this.DPO = dpo;
      this.FUA = fua;
      this.FUA_NV = fua_nv;
   }

   public Write10(long logicalBlockAddress, long transferLength)
   {
      this(0, false, false, false, false, false, logicalBlockAddress, transferLength);
   }

   protected void decodeByte1(int unsignedByte) throws IllegalArgumentException
   {
      if (((unsignedByte >>> 5) & 0x07) != 0)
      {
         throw new IllegalArgumentException("Write protection information is not supported");
      }

      this.DPO = ((unsignedByte >>> 4) & 0x01) == 1;
      this.FUA = ((unsignedByte >>> 3) & 0x01) == 1;
      this.FUA_NV = ((unsignedByte >>> 1) & 0x01) == 1;
   }

   protected int encodeByte1()
   {
      int b = 0;
      if (DPO)
      {
         b |= 0x02;
      }
      if (FUA)
      {
         b |= 0x08;
      }
      if (FUA_NV)
      {
         b |= 0x10;
      }
      return b;
   }

   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      this.decodeByte1(in.readUnsignedByte());

      long mss = in.readUnsignedShort();
      long lss = in.readUnsignedShort();
      setLogicalBlockAddress( (mss << 16) | lss );

      this.groupNumber = in.readUnsignedByte() & 0x1F;
      setTransferLength(in.readUnsignedShort());
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

         out.writeByte(this.encodeByte1());

         out.writeInt((int)getLogicalBlockAddress());
         out.writeByte(this.groupNumber & 0x1F);
         out.writeShort((int) getTransferLength());
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
      return 10;
   }

   public boolean isDPO()
   {
      return this.DPO;
   }

   public void setDPO(boolean dpo)
   {
      this.DPO = dpo;
   }

   public boolean isFUA()
   {
      return this.FUA;
   }

   public void setFUA(boolean fua)
   {
      this.FUA = fua;
   }

   public boolean isFUA_NV()
   {
      return this.FUA_NV;
   }

   public void setFUA_NV(boolean fua_nv)
   {
      this.FUA_NV = fua_nv;
   }

   public int getGroupNumber()
   {
      return this.groupNumber;
   }

   public void setGroupNumber(int groupNumber)
   {
      this.groupNumber = groupNumber;
   }
   
   @Override
   public String toString()
   {
      return "<Write10>";
   }
}
