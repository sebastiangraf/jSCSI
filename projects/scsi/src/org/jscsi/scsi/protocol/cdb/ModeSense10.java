
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class ModeSense10 extends AbstractParameterCDB
{
   public static final int OPERATION_CODE = 0x5A;

   private boolean DBD;
   private boolean LLBAA;
   private int PC;
   private int pageCode;
   private int subPageCode;

   public ModeSense10()
   {
      super(OPERATION_CODE);
   }

   public ModeSense10(
         boolean dbd,
         boolean llbaa,
         int pageControl,
         int pageCode,
         int subPageCode,
         boolean linked,
         boolean normalACA,
         long allocationLength)
   {
      super(OPERATION_CODE, linked, normalACA, (int) allocationLength, 0);

      if (allocationLength > 65536)
      {
         throw new IllegalArgumentException("Allocation length out of bounds for command type");
      }

      this.DBD = dbd;
      this.PC = pageControl;
      this.pageCode = pageCode;
      this.subPageCode = subPageCode;
   }

   public ModeSense10(
         boolean dbd,
         boolean llbaa,
         int pageControl,
         int pageCode,
         int subPageCode,
         long allocationLength)
   {
      this(dbd, llbaa, pageControl, pageCode, subPageCode, false, false, allocationLength);
   }

   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));
      int tmp;

      int operationCode = in.readUnsignedByte();
      tmp = in.readUnsignedByte();
      this.DBD = (tmp & 0x08) != 0;
      tmp >>>= 4;
      this.LLBAA = (tmp & 0x01) != 0;
      tmp = in.readUnsignedByte();
      this.pageCode = tmp & 0x3F;
      this.PC = tmp >>> 6;
      this.subPageCode = in.readUnsignedByte();
      in.readShort(); // first part of RESERVED block
      in.readByte(); // remaining RESERVED block
      setAllocationLength(in.readUnsignedShort());

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
         out.writeByte(((this.LLBAA ? 0x10 : 0x00) | (this.DBD ? 0x08 : 0x00)));
         out.writeByte((this.PC << 6) | this.pageCode);
         out.writeByte(this.subPageCode);
         out.writeShort(0);
         out.writeByte(0);
         out.writeShort((int) getAllocationLength());
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

   public boolean isDBD()
   {
      return this.DBD;
   }

   public void setDBD(boolean dbd)
   {
      this.DBD = dbd;
   }

   public boolean isLLBAA()
   {
      return this.LLBAA;
   }

   public void setLLBAA(boolean llbaa)
   {
      this.LLBAA = llbaa;
   }

   public int getPC()
   {
      return this.PC;
   }

   public void setPC(int pc)
   {
      this.PC = pc;
   }

   public int getPageCode()
   {
      return this.pageCode;
   }

   public void setPageCode(int pageCode)
   {
      this.pageCode = pageCode;
   }

   public int getSubPageCode()
   {
      return this.subPageCode;
   }

   public void setSubPageCode(int subPageCode)
   {
      this.subPageCode = subPageCode;
   }
}
