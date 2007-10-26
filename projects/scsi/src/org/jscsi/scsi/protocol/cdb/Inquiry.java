
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class Inquiry extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x12;

   private boolean EVPD;
   private int pageCode;
   private int allocationLength;

   public Inquiry()
   {
      super();
   }

   public Inquiry(
         boolean evpd,
         byte pageCode,
         short allocationLength,
         boolean linked,
         boolean normalACA)
   {
      super(linked, normalACA);

      this.EVPD = evpd;
      this.pageCode = pageCode;
      this.allocationLength = allocationLength;
   }

   public Inquiry(boolean evpd, byte pageCode, short allocationLength)
   {
      this(evpd, pageCode, allocationLength, false, false);
   }

   @Override
   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      this.EVPD = (in.readUnsignedByte() & 0x01) == 0x01;
      this.pageCode = in.readUnsignedByte();
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
         out.writeByte(this.EVPD ? 0x01 : 0x00);
         out.writeByte(this.pageCode);
         out.writeShort(this.allocationLength);
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
      return 6;
   }

   public boolean isEVPD()
   {
      return EVPD;
   }

   public void setEVPD(boolean evpd)
   {
      this.EVPD = evpd;
   }

   public int getPageCode()
   {
      return pageCode;
   }

   public void setPageCode(int pageCode)
   {
      this.pageCode = pageCode;
   }

   public void setAllocationLength(int allocationLength)
   {
      this.allocationLength = allocationLength;
   }
}
