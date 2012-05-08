/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
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

import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Encodable;
import org.jscsi.scsi.protocol.Serializer;

/**
 * Base class for Inquiry Data Parsers. See Specification SPC-3, pg. 144.
 */
public class StandardInquiryData implements Encodable, Serializer
{
   // Inquiry Data Fields
   private byte peripheralQualifier; // 3 bits
   private byte peripheralDeviceType; // 5 bits
   private boolean RMB; // 1 bit
   private short version; // 8 bits
   private boolean normACA; // 1 bit
   private boolean hiSup; // 1 bit
   private byte responseDataFormat; // 4 bits
   private short additionalLength = 0;
   private boolean SCCS; // 1 bit
   private boolean ACC; // 1 bit
   private byte TPGS; // 2 bits
   private boolean threePC; // 1 bit
   private boolean protect; // 1 bit
   private boolean BQue; // 1 bit
   private boolean encServ; // 1 bit
   private boolean VS1;
   private boolean multiP; // 1 bit
   private boolean MChngr; // 1 bit
   private boolean addr16;
   private boolean wbus16;
   private boolean sync;
   private boolean linked; // 1 bit
   private boolean cmdQue;
   private boolean VS2;
   private byte[] T10VendorIdentification = new byte[8]; // 8 bytes
   private byte[] productIdentification = new byte[16]; // 16 bytes
   private byte[] productRevisionLevel = new byte[4]; // 4 bytes
   private byte[] vendorSpecific1 = new byte[20]; // 20 bytes
   private byte clocking;
   private boolean QAS;
   private boolean IUS;
   private int versionDescriptor1;
   private int versionDescriptor2;
   private int versionDescriptor3;
   private int versionDescriptor4;
   private int versionDescriptor5;
   private int versionDescriptor6;
   private int versionDescriptor7;
   private int versionDescriptor8;
   private byte[] vendorSpecific2 = null;

   private static final int STD_LENGTH = 96;

   public StandardInquiryData()
   {
   }

   public byte[] encode()
   {
      byte[] encodedData = new byte[STD_LENGTH + additionalLength];

      /////////////////////////////////////////////////////////////////////////
      // Add Peripheral Qualifier
      encodedData[0] = (byte) ((this.peripheralQualifier & 0x07) << 5);
      // Add Peripheral Device Type
      encodedData[0] |= (byte) (this.peripheralDeviceType & 0x1F);

      /////////////////////////////////////////////////////////////////////////
      // Add RMB
      encodedData[1] = (byte) (this.RMB ? 0x80 : 0x00);

      /////////////////////////////////////////////////////////////////////////
      // Add Version
      encodedData[2] = (byte) (this.version & 0xFF);

      /////////////////////////////////////////////////////////////////////////
      // Add NormACA
      encodedData[3] = (byte) (this.normACA ? 0x20 : 0x00);
      // Add HiSup
      encodedData[3] |= (byte) (this.hiSup ? 0x10 : 0x00);
      // Response Data Format
      encodedData[3] |= (byte) (this.responseDataFormat & 0x0F);

      /////////////////////////////////////////////////////////////////////////
      // Add Additional Length
      encodedData[4] = (byte) (this.additionalLength & 0xFF); // number of remaining bytes

      /////////////////////////////////////////////////////////////////////////
      // Add SCCS
      encodedData[5] = (byte) (this.SCCS ? (1 << 7) : 0x00);
      // Add ACC
      encodedData[5] |= (byte) (this.ACC ? (1 << 6) : 0x00);
      // Add TGPS
      encodedData[5] |= (byte) ((this.TPGS & 0x03) << 4);
      // Add 3PC
      encodedData[5] |= (byte) (this.threePC ? (1 << 3) : 0x00);
      // Add Protect
      encodedData[5] |= (byte) (this.protect ? (0x01) : 0x00);

      /////////////////////////////////////////////////////////////////////////
      // Add BQue
      encodedData[6] = (byte) (this.BQue ? (1 << 7) : 0x00);
      // Add EncServ
      encodedData[6] |= (byte) (this.encServ ? (1 << 6) : 0x00);
      // Add VS1
      encodedData[6] |= (byte) (this.VS1 ? (1 << 5) : 0x00);
      // Add MultiP
      encodedData[6] |= (byte) (this.multiP ? (1 << 4) : 0x00);
      // Add MChngr
      encodedData[6] |= (byte) (this.MChngr ? (1 << 3) : 0x00);
      // Add addr16
      encodedData[6] |= (byte) (this.addr16 ? (0x01) : 0x00);

      /////////////////////////////////////////////////////////////////////////
      // Add WBus16
      encodedData[7] = (byte) (this.wbus16 ? (1 << 5) : 0x00);
      // Add sync
      encodedData[7] |= (byte) (this.sync ? (1 << 4) : 0x00);
      // Add linked
      encodedData[7] |= (byte) (this.linked ? (1 << 3) : 0x00);
      // Add CmdQue
      encodedData[7] |= (byte) (this.cmdQue ? (1 << 1) : 0x00);
      // Add VS2
      encodedData[7] |= (byte) (this.VS2 ? (0x01) : 0x00);

      /////////////////////////////////////////////////////////////////////////
      // Add T10 Vender ID
      System.arraycopy(T10VendorIdentification, 0, encodedData, 8, T10VendorIdentification.length);

      /////////////////////////////////////////////////////////////////////////
      // Add Product ID
      System.arraycopy(productIdentification, 0, encodedData, 16, productIdentification.length);

      /////////////////////////////////////////////////////////////////////////
      // Add Product Revision Level
      System.arraycopy(productRevisionLevel, 0, encodedData, 32, productRevisionLevel.length);

      /////////////////////////////////////////////////////////////////////////
      // Add Vendor Specific (1)
      if (this.VS1)
      {
         System.arraycopy(vendorSpecific1, 0, encodedData, 36, vendorSpecific1.length);
      }

      /////////////////////////////////////////////////////////////////////////
      // Add Clocking
      encodedData[56] = (byte) ((this.clocking & 0x03) << 2);
      // Add QAS
      encodedData[56] |= (byte) (this.QAS ? (0x02) : 0x00);
      // Add IUS
      encodedData[56] |= (byte) (this.IUS ? (0x01) : 0x00);

      /////////////////////////////////////////////////////////////////////////
      // Add Version Descriptors
      encodedData[58] = (byte) (this.versionDescriptor1 >>> 8 & 0xFF);
      encodedData[59] = (byte) (this.versionDescriptor1 & 0xFF);
      encodedData[60] = (byte) (this.versionDescriptor2 >>> 8 & 0xFF);
      encodedData[61] = (byte) (this.versionDescriptor2 & 0xFF);
      encodedData[62] = (byte) (this.versionDescriptor3 >>> 8 & 0xFF);
      encodedData[63] = (byte) (this.versionDescriptor3 & 0xFF);
      encodedData[64] = (byte) (this.versionDescriptor4 >>> 8 & 0xFF);
      encodedData[65] = (byte) (this.versionDescriptor4 & 0xFF);
      encodedData[66] = (byte) (this.versionDescriptor5 >>> 8 & 0xFF);
      encodedData[67] = (byte) (this.versionDescriptor5 & 0xFF);
      encodedData[68] = (byte) (this.versionDescriptor6 >>> 8 & 0xFF);
      encodedData[69] = (byte) (this.versionDescriptor6 & 0xFF);
      encodedData[70] = (byte) (this.versionDescriptor7 >>> 8 & 0xFF);
      encodedData[71] = (byte) (this.versionDescriptor7 & 0xFF);
      encodedData[72] = (byte) (this.versionDescriptor8 >>> 8 & 0xFF);
      encodedData[73] = (byte) (this.versionDescriptor8 & 0xFF);

      /////////////////////////////////////////////////////////////////////////
      // Add Vendor Specific (2)
      if (this.VS2 && this.vendorSpecific2 != null)
      {
         // total length = n + 1
         // additionalLength = n - 4
         //                  = (total length - 1) - 4
         //                  = total length - 5
         // vendorSpecific2 length = total length - 96
         //                        = (additional length + 5) - 96
         //                        = additional length - 91
         int vendorSpecific2Length = additionalLength - STD_LENGTH + 5;
         System.arraycopy(vendorSpecific2, 0, encodedData, STD_LENGTH, vendorSpecific2Length);
      }

      /////////////////////////////////////////////////////////////////////////

      return encodedData;
   }

   public void decode(byte[] header, ByteBuffer buffer)
   {
      decode(buffer);
   }

   @SuppressWarnings("unchecked")
   public StandardInquiryData decode(ByteBuffer buffer)
   {
      byte[] data = new byte[STD_LENGTH];
      buffer.get(data);

      this.peripheralQualifier = (byte) ((data[0] >>> 5) & 0x07);
      this.peripheralDeviceType = (byte) (data[0] & 0x1F);

      this.RMB = ((data[1] >>> 7) & 0x01) == 1;

      this.version = (short) (data[2] & 0xFF);

      this.normACA = ((data[3] >>> 5) & 1) == 1;
      this.hiSup = ((data[3] >>> 4) & 1) == 1;
      this.responseDataFormat = (byte) (data[3] & 0x0F);

      this.additionalLength = (short) (data[4] & 0xFF);

      this.SCCS = ((data[5] >>> 7) & 1) == 1;
      this.ACC = ((data[5] >>> 6) & 1) == 1;
      this.TPGS = (byte) ((data[5] >>> 4) & 0x03);
      this.threePC = ((data[5] >>> 3) & 1) == 1;
      this.protect = (data[5] & 1) == 1;

      this.BQue = ((data[6] >>> 7) & 1) == 1;
      this.encServ = ((data[6] >>> 6) & 1) == 1;
      this.VS1 = ((data[6] >>> 5) & 1) == 1;
      this.multiP = ((data[6] >>> 4) & 1) == 1;
      this.MChngr = ((data[6] >>> 3) & 1) == 1;
      this.addr16 = (data[6] & 1) == 1;

      this.wbus16 = ((data[7] >>> 5) & 1) == 1;
      this.sync = ((data[7] >>> 4) & 1) == 1;
      this.linked = ((data[7] >>> 3) & 1) == 1;
      this.cmdQue = ((data[7] >>> 1) & 1) == 1;
      this.VS2 = (data[7] & 1) == 1;

      System.arraycopy(data, 8, T10VendorIdentification, 0, T10VendorIdentification.length);

      System.arraycopy(data, 16, productIdentification, 0, productIdentification.length);

      System.arraycopy(data, 32, productRevisionLevel, 0, productRevisionLevel.length);

      if (this.VS1)
      {
         System.arraycopy(data, 36, vendorSpecific1, 0, vendorSpecific1.length);
      }

      this.clocking = (byte) ((data[56] >>> 2) & 0x02);
      this.QAS = ((data[56] >>> 1) & 1) == 1;
      this.IUS = (data[56] & 1) == 1;

      this.versionDescriptor1 = (data[58] & 0xFF) << 8 | (data[59] & 0xFF);
      this.versionDescriptor2 = (data[60] & 0xFF) << 8 | (data[61] & 0xFF);
      this.versionDescriptor3 = (data[62] & 0xFF) << 8 | (data[63] & 0xFF);
      this.versionDescriptor4 = (data[64] & 0xFF) << 8 | (data[65] & 0xFF);
      this.versionDescriptor5 = (data[66] & 0xFF) << 8 | (data[67] & 0xFF);
      this.versionDescriptor6 = (data[68] & 0xFF) << 8 | (data[69] & 0xFF);
      this.versionDescriptor7 = (data[70] & 0xFF) << 8 | (data[71] & 0xFF);
      this.versionDescriptor8 = (data[72] & 0xFF) << 8 | (data[73] & 0xFF);

      int vendorSpecific2Length = additionalLength - STD_LENGTH + 5;
      if (this.VS2 && vendorSpecific2Length > 0)
      {
         this.vendorSpecific2 = new byte[vendorSpecific2Length];
         buffer.get(this.vendorSpecific2);
      }

      return this;
   }

   /////////////////////////////////////////////////////////////////////////////
   // getters/setters

   public byte getPeripheralQualifier()
   {
      return this.peripheralQualifier;
   }

   public void setPeripheralQualifier(byte peripheralQualifier)
   {
      this.peripheralQualifier = peripheralQualifier;
   }

   public byte getPeripheralDeviceType()
   {
      return this.peripheralDeviceType;
   }

   public void setPeripheralDeviceType(byte peripheralDeviceType)
   {
      this.peripheralDeviceType = peripheralDeviceType;
   }

   public boolean isRMB()
   {
      return this.RMB;
   }

   public void setRMB(boolean rmb)
   {
      this.RMB = rmb;
   }

   public short getVersion()
   {
      return this.version;
   }

   public void setVersion(short version)
   {
      this.version = version;
   }

   public boolean isNormACA()
   {
      return this.normACA;
   }

   public void setNormACA(boolean normACA)
   {
      this.normACA = normACA;
   }

   public boolean isHiSup()
   {
      return this.hiSup;
   }

   public void setHiSup(boolean hiSup)
   {
      this.hiSup = hiSup;
   }

   public byte getResponseDataFormat()
   {
      return this.responseDataFormat;
   }

   public void setResponseDataFormat(byte responseDataFormat)
   {
      this.responseDataFormat = responseDataFormat;
   }

   public short getAdditionalLength()
   {
      return this.additionalLength;
   }

   public void setAdditionalLength(short additionalLength)
   {
      this.additionalLength = additionalLength;
   }

   public boolean isSCCS()
   {
      return this.SCCS;
   }

   public void setSCCS(boolean sccs)
   {
      this.SCCS = sccs;
   }

   public boolean isACC()
   {
      return this.ACC;
   }

   public void setACC(boolean acc)
   {
      this.ACC = acc;
   }

   public byte getTPGS()
   {
      return this.TPGS;
   }

   public void setTPGS(byte tpgs)
   {
      this.TPGS = tpgs;
   }

   public boolean isThreePC()
   {
      return this.threePC;
   }

   public void setThreePC(boolean threePC)
   {
      this.threePC = threePC;
   }

   public boolean isProtect()
   {
      return this.protect;
   }

   public void setProtect(boolean protect)
   {
      this.protect = protect;
   }

   public boolean isBQue()
   {
      return this.BQue;
   }

   public void setBQue(boolean que)
   {
      this.BQue = que;
   }

   public boolean isEncServ()
   {
      return this.encServ;
   }

   public void setEncServ(boolean encServ)
   {
      this.encServ = encServ;
   }

   public boolean isVS1()
   {
      return this.VS1;
   }

   public void setVS1(boolean vs1)
   {
      this.VS1 = vs1;
   }

   public boolean isMultiP()
   {
      return this.multiP;
   }

   public void setMultiP(boolean multiP)
   {
      this.multiP = multiP;
   }

   public boolean isMChngr()
   {
      return this.MChngr;
   }

   public void setMChngr(boolean chngr)
   {
      this.MChngr = chngr;
   }

   public boolean isAddr16()
   {
      return this.addr16;
   }

   public void setAddr16(boolean addr16)
   {
      this.addr16 = addr16;
   }

   public boolean isWbus16()
   {
      return this.wbus16;
   }

   public void setWbus16(boolean wbus16)
   {
      this.wbus16 = wbus16;
   }

   public boolean isSync()
   {
      return this.sync;
   }

   public void setSync(boolean sync)
   {
      this.sync = sync;
   }

   public boolean isLinked()
   {
      return this.linked;
   }

   public void setLinked(boolean linked)
   {
      this.linked = linked;
   }

   public boolean isCmdQue()
   {
      return this.cmdQue;
   }

   public void setCmdQue(boolean cmdQue)
   {
      this.cmdQue = cmdQue;
   }

   public boolean isVS2()
   {
      return this.VS2;
   }

   public void setVS2(boolean vs2)
   {
      this.VS2 = vs2;
   }

   public byte[] getT10VendorIdentification()
   {
      return this.T10VendorIdentification;
   }

   public void setT10VendorIdentification(byte[] vendorIdentification)
   {
      this.T10VendorIdentification = vendorIdentification;
   }

   public byte[] getProductIdentification()
   {
      return this.productIdentification;
   }

   public void setProductIdentification(byte[] productIdentification)
   {
      this.productIdentification = productIdentification;
   }

   public byte[] getProductRevisionLevel()
   {
      return this.productRevisionLevel;
   }

   public void setProductRevisionLevel(byte[] productRevisionLevel)
   {
      this.productRevisionLevel = productRevisionLevel;
   }

   public byte[] getVendorSpecific1()
   {
      return this.vendorSpecific1;
   }

   public void setVendorSpecific1(byte[] vendorSpecific1)
   {
      this.vendorSpecific1 = vendorSpecific1;
   }

   public byte getClocking()
   {
      return this.clocking;
   }

   public void setClocking(byte clocking)
   {
      this.clocking = clocking;
   }

   public boolean isQAS()
   {
      return this.QAS;
   }

   public void setQAS(boolean qas)
   {
      this.QAS = qas;
   }

   public boolean isIUS()
   {
      return this.IUS;
   }

   public void setIUS(boolean ius)
   {
      this.IUS = ius;
   }

   public int getVersionDescriptor1()
   {
      return this.versionDescriptor1;
   }

   public void setVersionDescriptor1(int versionDescriptor1)
   {
      this.versionDescriptor1 = versionDescriptor1;
   }

   public int getVersionDescriptor2()
   {
      return this.versionDescriptor2;
   }

   public void setVersionDescriptor2(int versionDescriptor2)
   {
      this.versionDescriptor2 = versionDescriptor2;
   }

   public int getVersionDescriptor3()
   {
      return this.versionDescriptor3;
   }

   public void setVersionDescriptor3(int versionDescriptor3)
   {
      this.versionDescriptor3 = versionDescriptor3;
   }

   public int getVersionDescriptor4()
   {
      return this.versionDescriptor4;
   }

   public void setVersionDescriptor4(int versionDescriptor4)
   {
      this.versionDescriptor4 = versionDescriptor4;
   }

   public int getVersionDescriptor5()
   {
      return this.versionDescriptor5;
   }

   public void setVersionDescriptor5(int versionDescriptor5)
   {
      this.versionDescriptor5 = versionDescriptor5;
   }

   public int getVersionDescriptor6()
   {
      return this.versionDescriptor6;
   }

   public void setVersionDescriptor6(int versionDescriptor6)
   {
      this.versionDescriptor6 = versionDescriptor6;
   }

   public int getVersionDescriptor7()
   {
      return this.versionDescriptor7;
   }

   public void setVersionDescriptor7(int versionDescriptor7)
   {
      this.versionDescriptor7 = versionDescriptor7;
   }

   public int getVersionDescriptor8()
   {
      return this.versionDescriptor8;
   }

   public void setVersionDescriptor8(int versionDescriptor8)
   {
      this.versionDescriptor8 = versionDescriptor8;
   }

   public byte[] getVendorSpecific2()
   {
      return this.vendorSpecific2;
   }

   public void setVendorSpecific2(byte[] vendorSpecific2)
   {
      this.vendorSpecific2 = vendorSpecific2;
   }

}
