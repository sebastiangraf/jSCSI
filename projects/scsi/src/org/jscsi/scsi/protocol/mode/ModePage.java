
package org.jscsi.scsi.protocol.mode;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Encodable;
import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

/**
 * Base class for mode page parsers.
 */
public abstract class ModePage implements Encodable
{
   // set by decode
   private boolean parametersSavable;

   // set by constructor
   private boolean subPageFormat;
   private byte pageCode; // MAX VALUE 0x3F (6-bit)
   private int subPageCode; // MAX VALUE UBYTE_MAX
   private int pageLength;

   /**
    * Constructs a mode page.
    * 
    * @param pageCode
    */
   protected ModePage(byte pageCode, int pageLength)
   {
      this.pageCode = pageCode;
      this.subPageFormat = false;
      this.subPageCode = -1;
      this.pageLength = pageLength;
   }

   /**
    * Constructs a mode subpage.
    * 
    * @param pageCode
    * @param subPageCode
    */
   protected ModePage(byte pageCode, int subPageCode, int pageLength)
   {
      this.pageCode = pageCode;
      this.subPageFormat = true;
      this.subPageCode = subPageCode;
      this.pageLength = pageLength;
   }

   void setParametersSavable(boolean parametersSavable)
   {
      this.parametersSavable = parametersSavable;
   }

   public boolean isParametersSavable()
   {
      return this.parametersSavable;
   }

   public boolean isSubPageFormat()
   {
      return this.subPageFormat;
   }

   public byte getPageCode()
   {
      return this.pageCode;
   }

   public int getSubPageCode()
   {
      return this.subPageCode;
   }

   /**
    * Returns page length. Limited to UBYTE_MAX for pages and USHORT_MAX for subpages.
    */
   public final int getPageLength()
   {
      return this.pageLength;
   }

   /**
    * Encodes mode parameters of length {@link #getPageLength()} to an output byte buffer.
    */
   protected abstract void encodeModeParameters(DataOutputStream output);

   /**
    * Decodes mode parameters from an input byte buffer. Input page length must be equal to
    * {@link #getPageLength()} specific to the particular mode page.
    * 
    * @throws Exception
    *            If the input byte buffer was too short or contained invalid information.
    */
   protected abstract void decodeModeParameters(int dataLength, DataInputStream inputStream)
   throws BufferUnderflowException, IllegalArgumentException;

   public byte[] encode() throws BufferOverflowException
   {
      ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
      DataOutputStream dataOut = new DataOutputStream(byteOut);

      try
      {
         int b0 = 0;

         if (isParametersSavable())
         {
            b0 |= 0x80;
         }
         if (isSubPageFormat())
         {
            b0 |= 0x40;
         }

         b0 |= (getPageCode() & 0x3F);

         dataOut.writeByte(b0);

         if (isSubPageFormat())
         {
            dataOut.writeByte(getSubPageCode());
            dataOut.writeShort(getPageLength());
         }
         else
         {
            dataOut.writeByte(getPageLength());
         }

         // Write mode parameters
         encodeModeParameters(dataOut);

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
}
