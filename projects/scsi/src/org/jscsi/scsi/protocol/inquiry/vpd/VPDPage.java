package org.jscsi.scsi.protocol.inquiry.vpd;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Encodable;

public abstract class VPDPage implements Encodable
{  
   private int peripheralQualifier;
   private int peripheralDeviceType;
   private int pageCode;   
   
   
   /////////////////////////////////////////////////////////////////////////////
   
   
   public abstract byte[] encode() throws IOException;

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

   public void setPageCode(int pageCode)
   {
      this.pageCode = pageCode;
   }
}
