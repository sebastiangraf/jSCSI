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
   private int version; // 8 bits
   private boolean normACA; // 1 bit
   private boolean hiSup; // 1 bit
   private byte responseDataFormat; // 4 bits
   private int additionalLength = 36 - 4; // 8 bits
   private boolean SCCS; // 1 bit
   private boolean ACC; // 1 bit
   private byte TPGS; // 2 bits
   private boolean threePC; // 1 bit
   private boolean protect; // 1 bit
   private boolean BQue; // 1 bit
   private boolean encServ; // 1 bit
   private boolean multiP; // 1 bit
   private boolean MChngr; // 1 bit
   private boolean addr16; // 1 bit
   private boolean WBus16; // 1 bit
   private boolean sync; // 1 bit
   private boolean linked; // 1 bit
   private byte[] T10VendorIdentification = new byte[8]; // 8 bytes
   private byte[] productIdentification = new byte[16]; // 16 bytes
   private byte[] productRevisionLevel = new byte[4]; // 4 bytes
   private byte clocking; // 2 bits
   private boolean QAS; // 1 bit
   private boolean UIS; // 1 bit

   public StandardInquiryData()
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
      encodedData[4] = (byte) (this.additionalLength & 0xFF);

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
      // Add MultiP
      encodedData[6] |= (byte) (this.multiP ? (1 << 4) : 0x00);
      // Add MChngr
      encodedData[6] |= (byte) (this.MChngr ? (1 << 3) : 0x00);
      // Add addr16
      encodedData[6] |= (byte) (this.addr16 ? (0x01) : 0x00);

      /////////////////////////////////////////////////////////////////////////
      // Add wbus16
      encodedData[7] = (byte) (this.WBus16 ? (1 << 5) : 0x00);
      // Add sync
      encodedData[7] |= (byte) (this.sync ? (1 << 4) : 0x00);
      // Add linked
      encodedData[7] |= (byte) (this.linked ? (1 << 3) : 0x00);
      // Add CmdQue
      encodedData[7] |= (byte) (this.MChngr ? (1 << 1) : 0x00);

      /////////////////////////////////////////////////////////////////////////
      // Add T10 Vender ID
      System.arraycopy(T10VendorIdentification, 0, encodedData, 8, T10VendorIdentification.length);

      /////////////////////////////////////////////////////////////////////////
      // Add Product ID
      System.arraycopy(productIdentification, 0, encodedData, 16, productIdentification.length);

      /////////////////////////////////////////////////////////////////////////
      // Add Product Revision Level
      System.arraycopy(productRevisionLevel, 0, encodedData, 32, productIdentification.length);

      /////////////////////////////////////////////////////////////////////////
      // Add clocking
      encodedData[56] = (byte) ((this.clocking & 0x03) << 2);
      // Add qas
      encodedData[56] |= (byte) (this.QAS ? (1 << 1) : 0x00);
      // Add ius
      encodedData[56] |= (byte) (this.UIS ? 0x01 : 0x00);

      return encodedData;
   }

   public void decode(byte[] header, ByteBuffer buffer)
   {
      decode(buffer);
   }

   @SuppressWarnings("unchecked")
   public StandardInquiryData decode(ByteBuffer buffer)
   {
      byte[] header = new byte[5];
      buffer.get(header);

      this.peripheralQualifier = (byte) ((header[0] >> 5) & 0x07);
      this.peripheralDeviceType = (byte) (header[0] & 0x1F);

      this.RMB = ((header[1] >> 7) & 1) == 1;

      this.version = (header[2] & 0xFF);

      this.normACA = ((header[3] >> 5) & 1) == 1;
      this.hiSup = ((header[3] >> 4) & 1) == 1;
      this.responseDataFormat = (byte) (header[3] & 0x0F);

      this.additionalLength = header[4];

      byte[] payload = new byte[additionalLength];

      buffer.get(payload);

      this.SCCS = ((payload[5] >> 7) & 1) == 1;
      this.ACC = ((payload[5] >> 6) & 1) == 1;
      this.TPGS = (byte) ((payload[5] >> 4) & 0x03);
      this.threePC = ((payload[5] >> 3) & 1) == 1;
      this.protect = (payload[5] & 1) == 1;

      this.BQue = ((payload[6] >> 7) & 1) == 1;
      this.encServ = ((payload[6] >> 6) & 1) == 1;
      this.multiP = ((payload[6] >> 4) & 1) == 1;
      this.MChngr = ((payload[6] >> 3) & 1) == 1;
      this.addr16 = (payload[6] & 1) == 1;

      this.WBus16 = ((payload[7] >> 5) & 1) == 1;
      this.sync = ((payload[7] >> 4) & 1) == 1;
      this.linked = ((payload[7] >> 3) & 1) == 1;
      this.MChngr = ((payload[7] >> 1) & 1) == 1;

      System.arraycopy(payload, 8, T10VendorIdentification, 0, T10VendorIdentification.length);

      System.arraycopy(payload, 16, productIdentification, 0, productIdentification.length);

      System.arraycopy(payload, 32, productRevisionLevel, 0, productIdentification.length);

      this.clocking = (byte) ((payload[56] >> 2) & 0x03);
      this.QAS = ((payload[56] >> 1) & 1) == 1;
      this.UIS = (payload[56] & 1) == 1;

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

   public int getVersion()
   {
      return this.version;
   }

   public void setVersion(int version)
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

   public int getAdditionalLength()
   {
      return this.additionalLength;
   }

   public void setAdditionalLength(int additionalLength)
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

   public boolean isWBus16()
   {
      return this.WBus16;
   }

   public void setWBus16(boolean bus16)
   {
      this.WBus16 = bus16;
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

   public boolean isUIS()
   {
      return this.UIS;
   }

   public void setUIS(boolean uis)
   {
      this.UIS = uis;
   }
}
