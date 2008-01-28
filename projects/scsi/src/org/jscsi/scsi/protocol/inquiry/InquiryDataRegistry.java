//Cleversafe open-source code header - Version 1.1 - December 1, 2006
//
//Cleversafe Dispersed Storage(TM) is software for secure, private and
//reliable storage of the world's data using information dispersal.
//
//Copyright (C) 2005-2007 Cleversafe, Inc.
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
//USA.
//
//Contact Information: 
// Cleversafe, 10 W. 35th Street, 16th Floor #84,
// Chicago IL 60616
// email: licensing@cleversafe.org
//
//END-OF-HEADER
//-----------------------
//@author: John Quigley <jquigley@cleversafe.com>
//@date: January 1, 2008
//---------------------

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
