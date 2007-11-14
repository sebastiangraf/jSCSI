
package org.jscsi.scsi.protocol.inquiry;

import org.jscsi.core.exceptions.NotImplementedException;
import org.jscsi.scsi.protocol.inquiry.vpd.SupportedVPDPages;

// TODO: this class must be implemented
public class StaticInquiryDataRegistry extends InquiryDataRegistry
{
   public StaticInquiryDataRegistry()
   {
      super();
      this.populateStandardInquiryData();
      //this.populateDeviceIdentificationVPD();
      this.populateSupportedVPDPages();
   }

   protected void populateSupportedVPDPages()
   {
      SupportedVPDPages supportedPages = new SupportedVPDPages(0, 0);
      supportedPages.addSupportedCode(0x00);
      supportedPages.addSupportedCode(0x83);
   }

   protected void populateDeviceIdentificationVPD()
   {
      throw new NotImplementedException("Device Identification VPD must be populated");
      // TODO: 
   }

   protected void populateStandardInquiryData()
   {
      StandardInquiryData std = new StandardInquiryData();

      std.setPeripheralQualifier((byte) 0x00); // Peripheral device is connected or cannot detect
      std.setPeripheralDeviceType((byte) 0x00); // Direct access block device (SBC-2) attached
      std.setRMB(false); // Medium is not removable 
      std.setVersion((short) 0x05); // This device complies with ANSI 408-2005 (SPC-3)
      std.setNormACA(false); // ACA task attribute not supported
      std.setHiSup(false); // Hierarchical addressing is not used to assign LUNs
      std.setResponseDataFormat((byte) 2); // Response data complies with SPC-3
      std.setSCCS(false); // This device does not contain an embedded storage array controller
      std.setACC(false); // This device does not contain an access controls coordinator
      std.setTPGS((byte) 0x00); // Asymmetric logical unit access not supported
      std.setThreePC(false); // Third-Party Copy not supported
      std.setProtect(false); // Protection information not supported
      std.setBQue(false); // Basic task management not supported
      std.setEncServ(false); // This device does not contain an embedded enclosure services component
      std.setMultiP(false); // This is not a multi-port device
      std.setMChngr(false); // This device does not have an attached media changer
      std.setLinked(false); // Linked commands not supported

      std.setT10VendorIdentification("JSCSI   ".getBytes()); // 8 bytes left aligned
      std.setProductIdentification("Virtual SCSI Dev".getBytes()); // 16 bytes left aligned 
      std.setProductRevisionLevel("0.0 ".getBytes()); // 4 bytes left aligned

      std.setVersionDescriptor1(0x0055); // SAM-2 T10/1157-D revision 24
      std.setVersionDescriptor2(0x033B); // SBC-2 T10/1417-D revision 16
      std.setVersionDescriptor3(0x030F); // SPC-3 T10/1416-D revision 22
      std.setVersionDescriptor4(0x0960); // iSCSI (no version claimed)

      this.setStandardInquiryData(std);
   }

}
