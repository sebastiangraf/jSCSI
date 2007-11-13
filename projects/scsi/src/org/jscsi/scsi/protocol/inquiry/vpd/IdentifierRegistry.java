package org.jscsi.scsi.protocol.inquiry.vpd;

public class IdentifierRegistry
{  
   public class VendorSpecificIdentifier
   {
      private byte[] data;
      
      public VendorSpecificIdentifier(byte[] data)
      {
         this.setData(data);
      }
      
      public void setData(byte[] data)
      {
         this.data = data;
      }
      
      public byte[] getData()
      {
         return this.data;
      }
   }
   
   public VendorSpecificIdentifier makeVendorSpecificIdentifier(byte[] data)
   {
      return new VendorSpecificIdentifier(data);
   }   
}
