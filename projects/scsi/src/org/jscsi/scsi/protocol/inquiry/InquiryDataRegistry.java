package org.jscsi.scsi.protocol.inquiry;

import org.jscsi.scsi.protocol.inquiry.vpd.DeviceIdentificationVPD;
import org.jscsi.scsi.protocol.inquiry.vpd.SupportedVPDPages;

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
}
