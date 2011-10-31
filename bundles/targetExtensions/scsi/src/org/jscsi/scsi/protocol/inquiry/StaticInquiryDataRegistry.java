/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

import org.jscsi.core.exceptions.NotImplementedException;
import org.jscsi.scsi.protocol.inquiry.vpd.SupportedVPDPages;

//TODO: this class must be implemented
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
