
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class ModeSense10 extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x5A;

   private boolean dbd;
   private boolean llbaa;
   private int pageControl;
   private int pageCode;
   private int subPageCode;
   private long allocationLength;

   public ModeSense10()
   {
      super();
   }

   public ModeSense10(
         boolean dbd,
         boolean llbaa,
         int pageControl,
         int pageCode,
         int subPageCode,
         long allocationLength,
         boolean linked,
         boolean normalACA)
   {
      super(linked, normalACA);

      if (allocationLength > 65536)
      {
         throw new IllegalArgumentException("Allocation length out of bounds for command type");
      }

      this.dbd = dbd;
      this.pageControl = pageControl;
      this.pageCode = pageCode;
      this.subPageCode = subPageCode;
      this.allocationLength = (int) allocationLength;
   }

   public ModeSense10(
         boolean dbd,
         boolean llbaa,
         int pageControl,
         int pageCode,
         int subPageCode,
         long allocationLength)
   {
      this(dbd, llbaa, pageControl, pageCode, subPageCode, allocationLength, false, false);
   }

   @Override
   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));
      int tmp;


      int operationCode = in.readUnsignedByte();
      tmp = in.readUnsignedByte();
      this.dbd = (tmp & 0x08) != 0;
      tmp >>>= 4;
      this.llbaa = (tmp & 0x01) != 0;
      tmp = in.readUnsignedByte();
      this.pageCode = tmp & 0x3F;
      this.pageControl = tmp >>> 6;
      this.subPageCode = in.readUnsignedByte();
      in.readShort(); // first part of RESERVED block
      in.readByte(); // remaining RESERVED block
      this.allocationLength = in.readUnsignedShort();

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
         out.writeByte(((this.llbaa ? 0x10 : 0x00) | (this.dbd ? 0x08 : 0x00)));
         out.writeByte((this.pageControl << 6) | this.pageCode);
         out.writeByte(this.subPageCode);
         out.writeShort(0);
         out.writeByte(0);
         out.writeShort((int) this.allocationLength);
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
      return this.allocationLength;
   }

   @Override
   public long getLogicalBlockAddress()
   {
      return 0;
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

   public boolean isDbd()
   {
      return dbd;
   }

   public void setDbd(boolean dbd)
   {
      this.dbd = dbd;
   }

   public boolean isLLBAA()
   {
      return llbaa;
   }

   public void setLlbaa(boolean llbaa)
   {
      this.llbaa = llbaa;
   }

   public int getPageControl()
   {
      return pageControl;
   }

   public void setPageControl(int pageControl)
   {
      this.pageControl = pageControl;
   }

   public int getPageCode()
   {
      return pageCode;
   }

   public void setPageCode(int pageCode)
   {
      this.pageCode = pageCode;
   }

   public int getSubPageCode()
   {
      return subPageCode;
   }

   public void setSubPageCode(int subPageCode)
   {
      this.subPageCode = subPageCode;
   }

   public void setAllocationLength(long allocationLength)
   {
      this.allocationLength = allocationLength;
   }
}
