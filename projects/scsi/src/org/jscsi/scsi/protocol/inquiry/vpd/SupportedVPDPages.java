
package org.jscsi.scsi.protocol.inquiry.vpd;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class SupportedVPDPages extends VPDPage
{
   public static final int PAGE_CODE = 0x00;

   private List<Integer> supportedCodes;

   public SupportedVPDPages(
         int peripheralQualifier,
         int peripheralDeviceType,
         List<Integer> supportedCodes)
   {
      this.setPageCode(PAGE_CODE);
      this.setPeripheralQualifier(peripheralQualifier);
      this.setPeripheralDeviceType(peripheralDeviceType);
      this.supportedCodes = supportedCodes;
   }

   public SupportedVPDPages(int peripheralQualifier, int peripheralDeviceType)
   {
      this(peripheralQualifier, peripheralDeviceType, new LinkedList<Integer>());
   }

   /////////////////////////////////////////////////////////////////////////////
   // constructors

   public void decode(byte[] header, ByteBuffer buffer) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(buffer));

      // byte 0
      int b0 = in.readUnsignedByte();
      this.setPeripheralQualifier(b0 >>> 5);
      this.setPeripheralDeviceType(b0 & 0x1F);

      // byte 1
      int b1 = in.readUnsignedByte();

      if (b1 != PAGE_CODE)
      {
         throw new IOException("invalid page code: " + Integer.toHexString(b1));
      }

      this.setPageCode(b1);

      // byte 2
      in.readUnsignedByte();

      // byte 3
      int b3 = in.readUnsignedByte();

      // supported codes list
      for (int i = b3; i > 0; i--)
      {
         this.supportedCodes.add(in.readUnsignedByte());
      }
   }

   public byte[] encode()
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(this.supportedCodes.size() + 4);
      DataOutputStream out = new DataOutputStream(baos);

      try
      {
         // byte 0
         out.writeByte((this.getPeripheralQualifier() << 5) | this.getPeripheralDeviceType());

         // byte 1
         out.writeByte(this.getPageCode());

         // byte 2
         out.writeByte(0);

         // byte 3
         out.writeByte(this.supportedCodes.size());

         // supported codes list
         for (int code : this.supportedCodes)
         {
            out.writeByte(code);
         }

         return baos.toByteArray();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   /////////////////////////////////////////////////////////////////////////////

   public void addSupportedCode(int pageCode)
   {
      this.supportedCodes.add(pageCode);
   }

   public List<Integer> getSupportedCodes()
   {
      return supportedCodes;
   }
}
