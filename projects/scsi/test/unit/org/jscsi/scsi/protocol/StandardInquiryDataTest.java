//
// Cleversafe open-source code header - Version 1.1 - December 1, 2006
//
// Cleversafe Dispersed Storage(TM) is software for secure, private and
// reliable storage of the world's data using information dispersal.
//
// Copyright (C) 2005-2007 Cleversafe, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
// USA.
//
// Contact Information: Cleversafe, 10 W. 35th Street, 16th Floor #84,
// Chicago IL 60616
// email licensing@cleversafe.org
//
// END-OF-HEADER
//-----------------------
// @author: mmotwani
//
// Date: Nov 13, 2007
//---------------------

package org.jscsi.scsi.protocol;

import static org.junit.Assert.fail;

import org.jscsi.scsi.protocol.inquiry.StandardInquiryData;
import org.junit.BeforeClass;
import org.junit.Test;

// TODO: Describe class or interface
public class StandardInquiryDataTest
{
   private static final String DEFAULT_PACKAGE = "org.jscsi.scsi.protocol.inquiry";
      
   private static String INQUIRY_DATA_NO_VENDOR_SPECIFIC_VALUES = "StandardInquiryData," +
   		"PeripheralQualifier=3:[0b000;0b001;0b010;0b011]," +
   		"PeripheralDeviceType=5:0x00," +
   		"RMB=1:0x01," +
   		"reserved=7:0x00," +
   		"Version=8:[0x00;0x03;0x04;0x05]," +
   		"reserved=2:0x00," +
   		"NormACA=1:0x01," +
   		"HiSup=1:0x01," +
   		"ResponseDataFormat=4:0x2," +
   		"AdditionalLength=8:0x5B," +
   		"SCCS=1:0x01," +
   		"ACC=1:0x01," +
   		"TPGS=2:[0b00;0b01;0b10;0b11]," +
   		"ThreePC=1:0x01," +
   		"reserved=2:0x00," +
   		"Protect=1:0x01," +
   		"BQue=1:0x01," +
   		"EncServ=1:0x01," +
   		"VS1=1:0x00," +
   		"MultiP=1:0x01," +
   		"MChngr=1:0x01," +
   		"reserved=7:0x00," +
   		"Linked=1:0x01," +
   		"reserved=1:0x00," +
   		"CmdQue=1:0x01," +
   		"VS2=1:0x00," +
   		"T10VendorIdentification=64:random," +
   		"ProductIdentification=128:random," +
   		"ProductRevisionLevel=32:random," +
   		"reserved=160:0x00," +
   		"reserved=16:0x00," +
   		"VersionDescriptor1=16:random," +
   		"VersionDescriptor2=16:random," +
   		"VersionDescriptor3=16:random," +
   		"VersionDescriptor4=16:random," +
   		"VersionDescriptor5=16:random," +
   		"VersionDescriptor6=16:random," +
   		"VersionDescriptor7=16:random," +
   		"VersionDescriptor8=16:random," +
   		"reserved=176:0x00";
   
   private static Serializer serializer;

   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      serializer = new StandardInquiryData();
   }
   
   private void runTest(String specification)
   {
      try
      {
         new SerializerTest(serializer, DEFAULT_PACKAGE, specification).runTest();
      }
      catch (Exception e)
      {
         fail(e.getMessage());
      }
   }

   
   @Test
   public void parseInquiryDataNoVendorSpecificValues()
   {
      runTest(INQUIRY_DATA_NO_VENDOR_SPECIFIC_VALUES);
   }
}
