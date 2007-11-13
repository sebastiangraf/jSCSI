package org.jscsi.scsi.protocol.inquiry.vpd;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class DeviceIdentificationVPD extends VPDPage
{
   private static Logger _logger = Logger.getLogger(DeviceIdentificationVPD.class);
   
   public static final int PAGE_CODE = 0x83;
   
   private int pageLength = 0;
   private List<IdentificationDescriptor> descriptorList = new LinkedList<IdentificationDescriptor>();

   public DeviceIdentificationVPD(int peripheralQualifier, int peripheralDeviceType, List<IdentificationDescriptor> descriptorList)
   {
      this.setPeripheralQualifier(peripheralQualifier);
      this.setPeripheralDeviceType(peripheralDeviceType);
      this.descriptorList = descriptorList;
   }
   
   public DeviceIdentificationVPD(int peripheralQualifier, int peripheralDeviceType)
   {
      this(peripheralQualifier, peripheralDeviceType, new LinkedList<IdentificationDescriptor>());
   }
   
   @Override
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
      
      // byte 2 - 3
      this.pageLength = in.readUnsignedShort();
      
      // byte 3
      int b3 = in.readUnsignedByte();
      
      // identification descriptor list
      descriptorList = this.parseDescriptorList(in);
   }

   @Override
   public byte[] encode() throws IOException
   {
      int pageLength = 0;
      for (IdentificationDescriptor id : descriptorList)
      {
         pageLength += id.getIdentifierLength() + 4;
      }
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream(pageLength + 4);
      DataOutputStream out = new DataOutputStream(baos);

      try
      {
         // byte 0
         out.writeByte((this.getPeripheralQualifier() << 5)|this.getPeripheralDeviceType());

         // byte 1
         out.writeByte(this.getPageCode());
         
         // byte 2 - 3
         out.writeShort(pageLength);
         
         // identification descriptor list
         for (IdentificationDescriptor id : this.descriptorList)
         {
            out.write(id.encode());
         }

         return baos.toByteArray();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   /////////////////////////////////////////////////////////////////////////////
   // utility
   
   
   private List<IdentificationDescriptor> parseDescriptorList(DataInputStream in) throws IOException
   {
      List<IdentificationDescriptor> descriptorList = new LinkedList<IdentificationDescriptor>();
      
      int readPageLength = 0;
      while (readPageLength < this.pageLength)
      {
         IdentificationDescriptor desc = new IdentificationDescriptor();

         // byte 0
         int b0 = in.readUnsignedByte();
         desc.setProtocolIdentifier(b0 >>> 4);
         desc.setCodeSet(b0 & 0x0F);
         
         // byte 1
         int b1 = in.readUnsignedByte();
         desc.setPIV((b1 >>> 7) == 1);
         desc.setAssociation((b1 & 0x30) >>> 4);
         desc.setIdentifierType(IdentifierType.valueOf(b1 & 0x15));
         
         // byte 2
         in.readUnsignedByte();
         
         // byte 3
         desc.setIdentifierLength(in.readUnsignedByte());
         
         // identifier
         byte[] ident = new byte[desc.getIdentifierLength()];
         in.read(ident);
         desc.setIdentifier(ident);
         
         descriptorList.add(desc);
         readPageLength += desc.getIdentifierLength() + 4;
      }
      
      return descriptorList;
   }
   
   /////////////////////////////////////////////////////////////////////////////
   // getters/setters

   
   public void addIdentificationDescriptor(IdentificationDescriptor identificationDescriptor)
   {
      this.descriptorList.add(identificationDescriptor);
   }

   public List<IdentificationDescriptor> getDescriptorList()
   {
      return descriptorList;
   }

   public void setDescriptorList(List<IdentificationDescriptor> descriptorList)
   {
      this.descriptorList = descriptorList;
   }
}
