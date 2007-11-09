
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class ModeSense10 extends ModeSense6
{
   public static final int OPERATION_CODE = 0x5A;

   private boolean LLBAA;

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
      super(dbd, pageControl, pageCode, subPageCode, linked, normalACA, allocationLength);
      
      this.LLBAA = llbaa;
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
      this.setDBD((tmp & 0x08) != 0);
      tmp >>>= 4;
      this.LLBAA = (tmp & 0x01) != 0;
      tmp = in.readUnsignedByte();
      this.setPageCode( tmp & 0x3F );
      this.setPC(tmp >>> 6);
      this.setSubPageCode(in.readUnsignedByte());
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
         out.writeByte(((this.LLBAA ? 0x10 : 0x00) | (this.isDBD() ? 0x08 : 0x00)));
         out.writeByte((this.getPC() << 6) | this.getPageCode());
         out.writeByte(this.getSubPageCode());
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


   public boolean isLLBAA()
   {
      return this.LLBAA;
   }

   public void setLLBAA(boolean llbaa)
   {
      this.LLBAA = llbaa;
   }
}
