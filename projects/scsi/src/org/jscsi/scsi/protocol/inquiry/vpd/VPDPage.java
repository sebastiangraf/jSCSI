package org.jscsi.scsi.protocol.inquiry.vpd;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Encodable;
import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public abstract class VPDPage implements Encodable
{
   private static Logger _logger = Logger.getLogger(VPDPage.class);
   
   private int peripheralQualifier;
   private int peripheralDeviceType;
   private byte pageCode;
   private int pageLength;

   /**
    * 
    * @param pageCode
    * @param pageLength
    */
   protected VPDPage(byte pageCode, int pageLength)
   {
      this.pageCode = pageCode;
      this.pageLength = pageLength;
   }

   /**
    * Encodes mode parameters of length {@link #getPageLength()} to an output byte buffer.
    * 
    * @param output
    */
   protected abstract void encodeVPDParameters(DataOutputStream output);

   /**
    * Decodes mode parameters from an input byte buffer. Input page length must be equal to
    * {@link #getPageLength()} specific to the particular mode page.
    * 
    * @throws Exception
    *            If the input byte buffer was too short or contained invalid information.
    */
   protected abstract void decodeVPDParameters(int dataLength, DataInputStream inputStream)
   throws BufferUnderflowException, IllegalArgumentException;

   public byte[] encode() throws BufferOverflowException
   {
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      DataOutputStream dataOut = new DataOutputStream(byteOut);

      try
      {
         // byte 1
         int b0 = this.getPeripheralQualifier() << 5;
         b0 |= this.getPeripheralDeviceType();
         dataOut.writeByte(b0);

         // byte 2
         dataOut.writeByte(this.getPageCode());

         // Write mode parameters
         this.encodeVPDParameters(dataOut);

         return byteOut.toByteArray();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode mode page.");
      }
   }

   public void decode(byte[] header, ByteBuffer buffer) throws IOException
   {
      int dataLength;
      int pageLength;

      this.parametersSavable = ((header[0] >>> 7) & 0x01) == 1;
      this.subPageFormat = ((header[0] >>> 6) & 0x01) == 1;
      this.pageCode = (byte) (header[0] & 0x3F);

      if (this.subPageFormat)
      {
         this.subPageCode = header[1];
         pageLength = ((int) header[2] << 8) | header[3];
         dataLength = pageLength - 4;
      }
      else
      {
         this.subPageCode = -1;
         pageLength = header[1];
         dataLength = pageLength - 2;
      }

      DataInputStream dataIn = new DataInputStream(new ByteBufferInputStream(buffer));
      decodeModeParameters(dataLength, dataIn);
   }

   
   /////////////////////////////////////////////////////////////////////////////
   // getters/setters
   
   
   public int getPeripheralQualifier()
   {
      return peripheralQualifier;
   }

   public void setPeripheralQualifier(int peripheralQualifier)
   {
      this.peripheralQualifier = peripheralQualifier;
   }

   public int getPeripheralDeviceType()
   {
      return peripheralDeviceType;
   }

   public void setPeripheralDeviceType(int peripheralDeviceType)
   {
      this.peripheralDeviceType = peripheralDeviceType;
   }

   public int getPageCode()
   {
      return pageCode;
   }

   public void setPageCode(int pageCode)
   {
      this.pageCode = pageCode;
   }

   public int getPageLength()
   {
      return pageLength;
   }

   public void setPageLength(int pageLength)
   {
      this.pageLength = pageLength;
   }
}
