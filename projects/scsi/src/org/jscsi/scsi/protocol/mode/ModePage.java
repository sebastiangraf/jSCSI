
package org.jscsi.scsi.protocol.mode;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Base class for mode page parsers.
 */
public abstract class ModePage
{
   // set by decode
   private boolean parametersSavable;

   // set by constructor
   private boolean subPageFormat;
   private byte pageCode; // MAX VALUE 0x3F (6-bit)
   private int subPageCode; // MAX VALUE UBYTE_MAX

   /**
    * Constructs a mode page.
    * 
    * @param pageCode
    */
   protected ModePage(byte pageCode)
   {
      this.pageCode = pageCode;
      this.subPageFormat = false;
      this.subPageCode = -1;
   }

   /**
    * Constructs a mode subpage.
    * 
    * @param pageCode
    * @param subPageCode
    */
   protected ModePage(byte pageCode, int subPageCode)
   {
      this.pageCode = pageCode;
      this.subPageFormat = true;
      this.subPageCode = subPageCode;
   }

   void setParametersSavable(boolean parametersSavable)
   {
      this.parametersSavable = parametersSavable;
   }

   public boolean getParametersSavable()
   {
      return this.parametersSavable;
   }

   public boolean getSubPageFormat()
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
   protected abstract int getPageLength();

   /**
    * Encodes mode parameters of length {@link #getPageLength()} to an output byte buffer.
    */
   protected abstract void encodeModeParameters(ByteBuffer output);

   /**
    * Decodes mode parameters from an input byte buffer. Input page length must be equal to
    * {@link #getPageLength()} specific to the particular mode page.
    * 
    * @throws Exception
    *            If the input byte buffer was too short or contained invalid information.
    */
   protected abstract void decodeModeParameters(int dataLength, DataInputStream inputStream)
         throws BufferUnderflowException, IllegalArgumentException;

   public byte[] getEncoded() throws BufferOverflowException
   {
      // Below, header is 2 bytes for page format, 4 bytes for subpage format.
      ByteArrayOutputStream header = new ByteArrayOutputStream(getSubPageFormat() ? 4 : 2);
      DataOutputStream out = new DataOutputStream(header);

      try
      {
         int b0 = 0;

         if (getParametersSavable())
         {
            b0 |= 0x80;
         }
         if (getSubPageFormat())
         {
            b0 |= 0x40;
         }

         b0 |= (getPageCode() & 0x3F);

         out.writeByte(b0);

         if (getSubPageFormat())
         {
            out.writeByte(getSubPageCode());
            out.writeShort(getPageLength());
         }
         else
         {
            out.writeByte(getPageLength());
         }

         // Allocate page length
         ByteBuffer outputBuffer = ByteBuffer.allocate(getPageLength());

         // Write header
         outputBuffer.put(header.toByteArray());

         // Write mode parameters
         encodeModeParameters(outputBuffer);

         return outputBuffer.array();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode mode page.");
      }
   }
}
