package org.jscsi.scsi.protocol.inquiry;


/**
 * 
 */
public abstract class InquiryDataRegistry
{
   private StandardInquiryData standardInquiryData;

   
   public StandardInquiryData getStandardInquiryData()
   {
      return standardInquiryData;
   }

   public void setStandardInquiryData(StandardInquiryData standardInquiryData)
   {
      this.standardInquiryData = standardInquiryData;
   }
   
   
   
   
}
