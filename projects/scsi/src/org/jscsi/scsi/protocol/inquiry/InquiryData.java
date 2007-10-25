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
// @author: jquigley
//
// Date: Oct 22, 2007
//---------------------

package org.jscsi.scsi.protocol.inquiry;

import java.nio.ByteBuffer;


/**
 * Base class for Inquiry Data Parsers
 */
public abstract class InquiryData
{
   // Inquiry Data Fields
   private byte peripheralQualifier; // 3 bits
   private byte peripheralDeviceType; // 5 bits
   private boolean rmb; // 1 bit
   private int version; // 8 bits
   private boolean normAsa; // 1 bit
   private boolean hiSup; // 1 bit
   private byte responseDataFormat; // 4 bits
   private int additionalLength = 36 - 4; // 8 bits
   private boolean sccs; // 1 bit
   private boolean acc; // 1 bit
   private byte tpgs; // 2 bits
   private boolean threepc; // 1 bit
   private boolean protect; // 1 bit
   private boolean bque; // 1 bit
   private boolean encServ; // 1 bit
   private boolean multiP; // 1 bit
   private boolean mChngr; // 1 bit
   private boolean addr16; // 1 bit
   private boolean wbus16; // 1 bit
   private boolean sync; // 1 bit
   private boolean linked; // 1 bit
   private boolean cmdQue; // 1 bit
   private byte[] t10vendorId = new byte[8]; // 8 bytes
   private byte[] productId = new byte[16]; // 16 bytes
   private byte[] productRevisionLevel = new byte[4]; // 4 bytes
   private byte clocking; // 2 bits
   private boolean qas; // 1 bit
   private boolean ius; // 1 bit
   
   public InquiryData()
   {
      
   }
   
   public byte[] encode()
   {
      byte[] encodedData = new byte[57];
      
      /////////////////////////////////////////////////////////////////////////
      // Add Peripheral Qualifier
      encodedData[0] = (byte) ((this.peripheralQualifier & 0x07) << 5);
      // Add Peripheral Device Type
      encodedData[0] |= (byte) (this.peripheralDeviceType & 0x1F);
      
      /////////////////////////////////////////////////////////////////////////
      // Add RMB
      encodedData[1] = (byte) (this.rmb ? 0x80 : 0x00);
      
      /////////////////////////////////////////////////////////////////////////
      // Add Version
      encodedData[2] = (byte) (this.version & 0xFF);
      
      /////////////////////////////////////////////////////////////////////////
      // Add NormACA
      encodedData[3] = (byte) (this.normAsa ? 0x20 : 0x00);
      // Add HiSup
      encodedData[3] |= (byte) (this.hiSup ? 0x10 : 0x00);
      // Response Data Format
      encodedData[3] |= (byte) (this.responseDataFormat & 0x0F);
      
      /////////////////////////////////////////////////////////////////////////
      // Add Additional Length
      encodedData[4] = (byte) (this.additionalLength & 0xFF);
      
      /////////////////////////////////////////////////////////////////////////
      // Add SCCS
      encodedData[5] = (byte) (this.sccs ? (1<<7) : 0x00);
      // Add ACC
      encodedData[5] |= (byte) (this.acc ? (1<<6) : 0x00);
      // Add TGPS
      encodedData[5] |= (byte) ((this.tpgs & 0x03) << 4);
      // Add 3PC
      encodedData[5] |= (byte) (this.threepc ? (1<<3) : 0x00);
      // Add Protect
      encodedData[5] |= (byte) (this.protect ? (0x01) : 0x00);
      
      /////////////////////////////////////////////////////////////////////////
      // Add BQue
      encodedData[6] = (byte) (this.bque ? (1<<7) : 0x00);
      // Add EncServ
      encodedData[6] |= (byte) (this.encServ ? (1<<6) : 0x00);
      // Add MultiP
      encodedData[6] |= (byte) (this.multiP ? (1<<4) : 0x00);
      // Add MChngr
      encodedData[6] |= (byte) (this.mChngr ? (1<<3) : 0x00);
      // Add addr16
      encodedData[6] |= (byte) (this.addr16 ? (0x01) : 0x00);
      
      /////////////////////////////////////////////////////////////////////////
      // Add wbus16
      encodedData[7] = (byte) (this.wbus16 ? (1<<5) : 0x00);
      // Add sync
      encodedData[7] |= (byte) (this.sync ? (1<<4) : 0x00);
      // Add linked
      encodedData[7] |= (byte) (this.linked ? (1<<3) : 0x00);
      // Add CmdQue
      encodedData[7] |= (byte) (this.mChngr ? (1<<1) : 0x00);
      
      /////////////////////////////////////////////////////////////////////////
      // Add T10 Vender ID
      System.arraycopy(t10vendorId, 0, encodedData, 8, t10vendorId.length);
      
      /////////////////////////////////////////////////////////////////////////
      // Add Product ID
      System.arraycopy(productId, 0, encodedData, 16, productId.length);
      
      /////////////////////////////////////////////////////////////////////////
      // Add Product Revision Level
      System.arraycopy(productRevisionLevel, 0, encodedData, 32, productId.length);
      
      /////////////////////////////////////////////////////////////////////////
      // Add clocking
      encodedData[56] = (byte) ((this.clocking & 0x03) << 2);
      // Add qas
      encodedData[56] |= (byte) (this.qas ? (1<<1) : 0x00);
      // Add ius
      encodedData[56] |= (byte) (this.ius ? 0x01 : 0x00);
      
      return encodedData;
   }
   
   public void decode(ByteBuffer buffer)
   {
      
   }

   public byte getPeripheralQualifier()
   {
      return peripheralQualifier;
   }

   public void setPeripheralQualifier(byte peripheralQualifier)
   {
      this.peripheralQualifier = peripheralQualifier;
   }

   public byte getPeripheralDeviceType()
   {
      return peripheralDeviceType;
   }

   public void setPeripheralDeviceType(byte peripheralDeviceType)
   {
      this.peripheralDeviceType = peripheralDeviceType;
   }

   public boolean isRmb()
   {
      return rmb;
   }

   public void setRmb(boolean rmb)
   {
      this.rmb = rmb;
   }

   public int getVersion()
   {
      return version;
   }

   public void setVersion(int version)
   {
      this.version = version;
   }

   public boolean isNormAsa()
   {
      return normAsa;
   }

   public void setNormAsa(boolean normAsa)
   {
      this.normAsa = normAsa;
   }

   public boolean isHiSup()
   {
      return hiSup;
   }

   public void setHiSup(boolean hiSup)
   {
      this.hiSup = hiSup;
   }

   public byte getResponseDataFormat()
   {
      return responseDataFormat;
   }

   public void setResponseDataFormat(byte responseDataFormat)
   {
      this.responseDataFormat = responseDataFormat;
   }

   public int getAdditionalLength()
   {
      return additionalLength;
   }

   public void setAdditionalLength(int additionalLength)
   {
      this.additionalLength = additionalLength;
   }

   public boolean isSccs()
   {
      return sccs;
   }

   public void setSccs(boolean sccs)
   {
      this.sccs = sccs;
   }

   public boolean isAcc()
   {
      return acc;
   }

   public void setAcc(boolean acc)
   {
      this.acc = acc;
   }

   public byte getTpgs()
   {
      return tpgs;
   }

   public void setTpgs(byte tpgs)
   {
      this.tpgs = tpgs;
   }

   public boolean isThreepc()
   {
      return threepc;
   }

   public void setThreepc(boolean threepc)
   {
      this.threepc = threepc;
   }

   public boolean isProtect()
   {
      return protect;
   }

   public void setProtect(boolean protect)
   {
      this.protect = protect;
   }

   public boolean isBque()
   {
      return bque;
   }

   public void setBque(boolean bque)
   {
      this.bque = bque;
   }

   public boolean isEncServ()
   {
      return encServ;
   }

   public void setEncServ(boolean encServ)
   {
      this.encServ = encServ;
   }

   public boolean isMultiP()
   {
      return multiP;
   }

   public void setMultiP(boolean multiP)
   {
      this.multiP = multiP;
   }

   public boolean isMChngr()
   {
      return mChngr;
   }

   public void setMChngr(boolean chngr)
   {
      mChngr = chngr;
   }

   public boolean isAddr16()
   {
      return addr16;
   }

   public void setAddr16(boolean addr16)
   {
      this.addr16 = addr16;
   }

   public boolean isWbus16()
   {
      return wbus16;
   }

   public void setWbus16(boolean wbus16)
   {
      this.wbus16 = wbus16;
   }

   public boolean isSync()
   {
      return sync;
   }

   public void setSync(boolean sync)
   {
      this.sync = sync;
   }

   public boolean isLinked()
   {
      return linked;
   }

   public void setLinked(boolean linked)
   {
      this.linked = linked;
   }

   public boolean isCmdQue()
   {
      return cmdQue;
   }

   public void setCmdQue(boolean cmdQue)
   {
      this.cmdQue = cmdQue;
   }

   public String getT10vendorId()
   {
      return new String(t10vendorId);
   }

   public void setT10vendorId(String id)
   {
      byte[] bytes = id.getBytes();  
      int copyLength = bytes.length > t10vendorId.length
                       ? t10vendorId.length : bytes.length;
      System.arraycopy(bytes, 0, t10vendorId, 0, copyLength);
   }

   public String getProductId()
   {
      return new String(productId);
   }

   public void setProductId(String id)
   {
      byte[] bytes = id.getBytes();  
      int copyLength = bytes.length > productId.length
                       ? productId.length : bytes.length;
      System.arraycopy(bytes, 0, productId, 0, copyLength);
   }

   public String getProductRevisionLevel()
   {
      return new String(productRevisionLevel);
   }

   public void setProductRevisionLevel(String id)
   {
      byte[] bytes = id.getBytes();  
      int copyLength = bytes.length > productRevisionLevel.length
                       ? productRevisionLevel.length : bytes.length;
      System.arraycopy(bytes, 0, productRevisionLevel, 0, copyLength);
   }
 

   public byte getClocking()
   {
      return clocking;
   }

   public void setClocking(byte clocking)
   {
      this.clocking = clocking;
   }

   public boolean isQas()
   {
      return qas;
   }

   public void setQas(boolean qas)
   {
      this.qas = qas;
   }

   public boolean isIus()
   {
      return ius;
   }

   public void setIus(boolean ius)
   {
      this.ius = ius;
   }
   
}


