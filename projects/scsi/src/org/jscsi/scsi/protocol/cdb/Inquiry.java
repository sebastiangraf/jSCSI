
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class Inquiry extends AbstractParameterCDB
{
   public static final int OPERATION_CODE = 0x12;

   private boolean EVPD;
   private int pageCode;

   public Inquiry()
   {
      super(OPERATION_CODE);
   }

   public Inquiry(
         boolean evpd,
         byte pageCode,
         boolean linked,
         boolean normalACA,
         short allocationLength)
   {
      super(OPERATION_CODE, linked, normalACA, allocationLength, 0);

      this.EVPD = evpd;
      this.pageCode = pageCode;
   }

   public Inquiry(boolean evpd, byte pageCode, short allocationLength)
   {
      this(evpd, pageCode, false, false, allocationLength);
   }

   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      this.EVPD = (in.readUnsignedByte() & 0x01) == 0x01;
      this.pageCode = in.readUnsignedByte();
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
         out.writeByte(this.EVPD ? 0x01 : 0x00);
         out.writeByte(this.pageCode);
         out.writeShort((short) getAllocationLength());
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
   
   public String toString()
   {
      return "<Inquiry>";
   }
}
