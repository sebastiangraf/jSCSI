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
   private boolean SCCS; // 1 bit
   private boolean ACC; // 1 bit
   private byte TPGS; // 2 bits
   private boolean threePC; // 1 bit
   private boolean protect; // 1 bit
   private boolean BQue; // 1 bit
   private boolean encServ; // 1 bit
   private boolean multiP; // 1 bit
   private boolean MChngr; // 1 bit
   private boolean linked; // 1 bit
   private byte[] T10VendorIdentification = new byte[8]; // 8 bytes
   private byte[] productIdentification = new byte[16]; // 16 bytes
   private byte[] productRevisionLevel = new byte[4]; // 4 bytes
   private int versionDescriptor1;
   private int versionDescriptor2;
   private int versionDescriptor3;
   private int versionDescriptor4;
   private int versionDescriptor5;
   private int versionDescriptor6;
   private int versionDescriptor7;
   private int versionDescriptor8;

   private static int ENCODE_LENGTH = 74;
   
   public StandardInquiryData()
   {
   }

   public byte[] encode()
   {
      byte[] encodedData = new byte[ENCODE_LENGTH];

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
      encodedData[4] = (byte) (ENCODE_LENGTH - 4 & 0xFF); // number of remaining bytes

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

      /////////////////////////////////////////////////////////////////////////
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
      
      encodedData[58] = (byte)(this.versionDescriptor1 >>> 8 | 0xFF);
      encodedData[59] = (byte)(this.versionDescriptor1 | 0xFF);
      encodedData[60] = (byte)(this.versionDescriptor2 >>> 8 | 0xFF);
      encodedData[61] = (byte)(this.versionDescriptor2 | 0xFF);
      encodedData[62] = (byte)(this.versionDescriptor3 >>> 8 | 0xFF);
      encodedData[63] = (byte)(this.versionDescriptor3 | 0xFF);
      encodedData[64] = (byte)(this.versionDescriptor4 >>> 8 | 0xFF);
      encodedData[65] = (byte)(this.versionDescriptor4 | 0xFF);
      encodedData[66] = (byte)(this.versionDescriptor5 >>> 8 | 0xFF);
      encodedData[67] = (byte)(this.versionDescriptor5 | 0xFF);
      encodedData[68] = (byte)(this.versionDescriptor6 >>> 8 | 0xFF);
      encodedData[69] = (byte)(this.versionDescriptor6 | 0xFF);
      encodedData[70] = (byte)(this.versionDescriptor7 >>> 8 | 0xFF);
      encodedData[71] = (byte)(this.versionDescriptor7 | 0xFF);
      encodedData[72] = (byte)(this.versionDescriptor8 >>> 8 | 0xFF);
      encodedData[73] = (byte)(this.versionDescriptor8 | 0xFF);
      
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
      byte[] header = new byte[5];
      buffer.get(header);

      this.peripheralQualifier = (byte) ((header[0] >> 5) & 0x07);
      this.peripheralDeviceType = (byte) (header[0] & 0x1F);

      this.RMB = ((header[1] >> 7) & 1) == 1;

      this.version = (header[2] & 0xFF);

      this.normACA = ((header[3] >> 5) & 1) == 1;
      this.hiSup = ((header[3] >> 4) & 1) == 1;
      this.responseDataFormat = (byte) (header[3] & 0x0F);

      int additionalLength = header[4];

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

      this.linked = ((payload[7] >> 3) & 1) == 1;
      this.MChngr = ((payload[7] >> 1) & 1) == 1;

      System.arraycopy(payload, 8, T10VendorIdentification, 0, T10VendorIdentification.length);

      System.arraycopy(payload, 16, productIdentification, 0, productIdentification.length);

      System.arraycopy(payload, 32, productRevisionLevel, 0, productIdentification.length);
      
      this.versionDescriptor1 = payload[58] << 8 | (payload[59] & 0xFF);
      this.versionDescriptor2 = payload[60] << 8 | (payload[61] & 0xFF);
      this.versionDescriptor3 = payload[62] << 8 | (payload[63] & 0xFF);
      this.versionDescriptor4 = payload[64] << 8 | (payload[65] & 0xFF);
      this.versionDescriptor5 = payload[66] << 8 | (payload[67] & 0xFF);
      this.versionDescriptor6 = payload[68] << 8 | (payload[69] & 0xFF);
      this.versionDescriptor7 = payload[70] << 8 | (payload[71] & 0xFF);
      this.versionDescriptor8 = payload[72] << 8 | (payload[73] & 0xFF);

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
   
   
   

   public int getVersionDescriptor1()
   {
      return versionDescriptor1;
   }

   public void setVersionDescriptor1(int versionDescriptor1)
   {
      this.versionDescriptor1 = versionDescriptor1;
   }

   public int getVersionDescriptor2()
   {
      return versionDescriptor2;
   }

   public void setVersionDescriptor2(int versionDescriptor2)
   {
      this.versionDescriptor2 = versionDescriptor2;
   }

   public int getVersionDescriptor3()
   {
      return versionDescriptor3;
   }

   public void setVersionDescriptor3(int versionDescriptor3)
   {
      this.versionDescriptor3 = versionDescriptor3;
   }

   public int getVersionDescriptor4()
   {
      return versionDescriptor4;
   }

   public void setVersionDescriptor4(int versionDescriptor4)
   {
      this.versionDescriptor4 = versionDescriptor4;
   }

   public int getVersionDescriptor5()
   {
      return versionDescriptor5;
   }

   public void setVersionDescriptor5(int versionDescriptor5)
   {
      this.versionDescriptor5 = versionDescriptor5;
   }

   public int getVersionDescriptor6()
   {
      return versionDescriptor6;
   }

   public void setVersionDescriptor6(int versionDescriptor6)
   {
      this.versionDescriptor6 = versionDescriptor6;
   }

   public int getVersionDescriptor7()
   {
      return versionDescriptor7;
   }

   public void setVersionDescriptor7(int versionDescriptor7)
   {
      this.versionDescriptor7 = versionDescriptor7;
   }

   public int getVersionDescriptor8()
   {
      return versionDescriptor8;
   }

   public void setVersionDescriptor8(int versionDescriptor8)
   {
      this.versionDescriptor8 = versionDescriptor8;
   }

}
