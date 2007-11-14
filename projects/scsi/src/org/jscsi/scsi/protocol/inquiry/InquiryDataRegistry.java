
package org.jscsi.scsi.protocol.inquiry;

import org.jscsi.scsi.protocol.inquiry.vpd.DeviceIdentificationVPD;
import org.jscsi.scsi.protocol.inquiry.vpd.SupportedVPDPages;
import org.jscsi.scsi.protocol.inquiry.vpd.VPDPage;

public abstract class InquiryDataRegistry
{
   private StandardInquiryData standardInquiryData;
   private DeviceIdentificationVPD deviceIdentificationVPD;
   private SupportedVPDPages supportedVPDPages;

   public SupportedVPDPages getSupportedVPDPages()
   {
      return this.supportedVPDPages;
   }

   public void setSupportedVPDPages(SupportedVPDPages supportedVPDPages)
   {
      this.supportedVPDPages = supportedVPDPages;
   }

   public DeviceIdentificationVPD getDeviceIdentificationVPD()
   {
      return this.deviceIdentificationVPD;
   }

   public void setDeviceIdentificationVPD(DeviceIdentificationVPD deviceIdentificationVPD)
   {
      this.deviceIdentificationVPD = deviceIdentificationVPD;
   }

   public StandardInquiryData getStandardInquiryData()
   {
      return this.standardInquiryData;
   }

   public void setStandardInquiryData(StandardInquiryData standardInquiryData)
   {
      this.standardInquiryData = standardInquiryData;
   }

   /**
    * Returns null if a VPD is not found. 
    * <p>
    * This method must be overridden if the another
    * implementation of {@link InquiryDataRegistry} is written that contains pages that are not
    * encapsulated in this abstract {@link InquiryDataRegistry}.
    * 
    * @param pageCode
    * @return
    */
   public VPDPage getVPDPage(int pageCode)
   {
      switch (pageCode)
      {
         case SupportedVPDPages.PAGE_CODE :
            return supportedVPDPages;
         case DeviceIdentificationVPD.PAGE_CODE :
            return deviceIdentificationVPD;
         default :
            return null;
      }
   }
}
