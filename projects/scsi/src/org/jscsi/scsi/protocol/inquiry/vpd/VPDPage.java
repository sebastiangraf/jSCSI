package org.jscsi.scsi.protocol.inquiry.vpd;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Encodable;
import org.jscsi.scsi.protocol.Serializer;

public abstract class VPDPage implements Encodable, Serializer
{  
   private int peripheralQualifier;
   private int peripheralDeviceType;
   private byte pageCode;   
   
   
   /////////////////////////////////////////////////////////////////////////////
   
   
   public abstract byte[] encode() throws BufferOverflowException;

   public abstract void decode(byte[] header, ByteBuffer buffer) throws IOException;

   
   /////////////////////////////////////////////////////////////////////////////
   // getters/setters
   
   
   public int getPeripheralQualifier()
   {
      return peripheralQualifier;
   }

   public void setPeripheralQualifier(int peripheralQualifier)
   {
      this.peripheralQualifier = peripheralQualifier;
   }

   public int getPeripheralDeviceType()
   {
      return peripheralDeviceType;
   }

   public void setPeripheralDeviceType(int peripheralDeviceType)
   {
      this.peripheralDeviceType = peripheralDeviceType;
   }

   public int getPageCode()
   {
      return pageCode;
   }

   public void setPageCode(byte pageCode)
   {
      this.pageCode = pageCode;
   }
}
