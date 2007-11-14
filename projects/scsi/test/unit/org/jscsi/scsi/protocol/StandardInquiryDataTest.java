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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import org.jscsi.scsi.protocol.inquiry.StandardInquiryData;
import org.junit.BeforeClass;
import org.junit.Test;

// TODO: Describe class or interface
public class StandardInquiryDataTest
{
   private static final String DEFAULT_PACKAGE = "org.jscsi.scsi.protocol.inquiry";
      
   
   // Alternating boolean values
   private static String INQUIRY_DATA_NO_VENDOR_SPECIFIC_1 = "StandardInquiryData," +
   		"PeripheralQualifier=3:[0b000;0b001;0b010;0b011]," +
   		"PeripheralDeviceType=5:0x00," +
   		"RMB=1:0x00," +
   		"reserved=7:0x00," +
   		"Version=8:[0x00;0x03;0x04;0x05]," +
   		"reserved=2:0x00," +
   		"NormACA=1:0x01," +
   		"HiSup=1:0x00," +
   		"ResponseDataFormat=4:0x2," +
   		"AdditionalLength=8:0x5B," +
   		"SCCS=1:0x01," +
   		"ACC=1:0x00," +
   		"TPGS=2:[0b00;0b01;0b10;0b11]," +
   		"ThreePC=1:0x01," +
   		"reserved=2:0x00," +
   		"Protect=1:0x00," +
   		"BQue=1:0x01," +
   		"EncServ=1:0x00," +
   		"VS1=1:0x00," +
   		"MultiP=1:0x01," +
   		"MChngr=1:0x00," +
   		"reserved=7:0x00," +
   		"Linked=1:0x01," +
   		"reserved=1:0x00," +
   		"CmdQue=1:0x00," +
   		"VS2=1:0x00," +
   		"reserved=64:0x00," +
   		"reserved=128:0x00," +
   		"reserved=32:0x00," +
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
   
   
   // Alternating boolean values
   private static String INQUIRY_DATA_NO_VENDOR_SPECIFIC_2 = "StandardInquiryData," +
         "PeripheralQualifier=3:std," +
         "PeripheralDeviceType=5:0x00," +
         "RMB=1:0x01," +
         "reserved=7:0x00," +
         "Version=8:std," +
         "reserved=2:0x00," +
         "NormACA=1:0x00," +
         "HiSup=1:0x01," +
         "ResponseDataFormat=4:0x2," +
         "AdditionalLength=8:0x5B," +
         "SCCS=1:0x00," +
         "ACC=1:0x01," +
         "TPGS=2:std," +
         "ThreePC=1:0x00," +
         "reserved=2:0x00," +
         "Protect=1:0x01," +
         "BQue=1:0x00," +
         "EncServ=1:0x01," +
         "VS1=1:0x00," +
         "MultiP=1:0x00," +
         "MChngr=1:0x01," +
         "reserved=7:0x00," +
         "Linked=1:0x00," +
         "reserved=1:0x00," +
         "CmdQue=1:0x01," +
         "VS2=1:0x00," +
         "reserved=64:0x00," +
         "reserved=128:0x00," +
         "reserved=32:0x00," +
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
   public void parseInquiryDataNoVendorSpecific1()
   {
      runTest(INQUIRY_DATA_NO_VENDOR_SPECIFIC_1);
   }
   
   @Test
   public void parseInquiryDataNoVendorSpecific2()
   {
      runTest(INQUIRY_DATA_NO_VENDOR_SPECIFIC_2);
   }
   
   @Test
   public void testT10VendorIdentification() throws IOException
   {
      byte[] t10VendorIdentification = new byte[8];
      Random random = new Random();
      random.nextBytes(t10VendorIdentification);
      
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      DataOutputStream dataOut = new DataOutputStream(byteOut);
      
      // First 8 bytes
      dataOut.writeLong(0);
      
      // t10 vendor identification (8)
      dataOut.write(t10VendorIdentification);
      
      // 10 longs = 80 bytes
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      
      StandardInquiryData sid = new StandardInquiryData();
      
      // Decode
      sid.decode(ByteBuffer.wrap(byteOut.toByteArray()));
      
      // Decode encoded
      sid.decode(ByteBuffer.wrap(sid.encode()));
      
      // Compare t10VendorIdentification
      byte[] returnedT10VendorIdentification = sid.getT10VendorIdentification();
      assertTrue(Arrays.equals(returnedT10VendorIdentification, t10VendorIdentification));
      
      //System.out.println(new String(returnedT10VendorIdentification));
   }
   
   @Test
   public void testProductIdentification() throws IOException
   {
      byte[] productIdentification = new byte[16];
      Random random = new Random();
      random.nextBytes(productIdentification);
      
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      DataOutputStream dataOut = new DataOutputStream(byteOut);
      
      // First 16 bytes
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      
      // product identification (16)
      dataOut.write(productIdentification);
      
      // 10 longs = 80 bytes
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      
      StandardInquiryData sid = new StandardInquiryData();
      
      // Decode
      sid.decode(ByteBuffer.wrap(byteOut.toByteArray()));
      
      // Decode encoded
      sid.decode(ByteBuffer.wrap(sid.encode()));
      
      // Compare productIdentification
      byte[] returnedProductIdentification = sid.getProductIdentification();
      assertTrue(Arrays.equals(returnedProductIdentification, productIdentification));
      
      //System.out.println(new String(returnedProductIdentification));
   }
   
   @Test
   public void testProductRevisionLevel() throws IOException
   {
      byte[] productRevisionLevel = new byte[4];
      Random random = new Random();
      random.nextBytes(productRevisionLevel);
      
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      DataOutputStream dataOut = new DataOutputStream(byteOut);
      
      // First 32 bytes
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      
      // product revision level (4)
      dataOut.write(productRevisionLevel);
      
      // 10 longs = 80 bytes
      dataOut.writeInt(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      dataOut.writeLong(0);
      
      StandardInquiryData sid = new StandardInquiryData();
      
      // Decode
      sid.decode(ByteBuffer.wrap(byteOut.toByteArray()));
      
      // Decode encoded
      sid.decode(ByteBuffer.wrap(sid.encode()));
      
      // Compare productRevisionLevel
      byte[] returnedProductRevisionLevel = sid.getProductRevisionLevel();
      assertTrue(Arrays.equals(returnedProductRevisionLevel, productRevisionLevel));
      
      //System.out.println(new String(returnedProductRevisionLevel));
   }
}
