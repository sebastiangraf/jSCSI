package org.jscsi.scsi.protocol.mode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.jscsi.scsi.protocol.Data;

/**
 * Base class for mode page parsers.
 */
public abstract class ModePage implements Data
{
   private static Map<Long, Class<? extends ModePage>> _pages =
      new HashMap<Long, Class<? extends ModePage>>();

   // set by decode
   private boolean parametersSavable;
   
   // set by constructor
   private boolean subPageFormat;
   private byte pageCode;  // MAX VALUE 0x3F (6-bit)
   private int subPageCode; // MAX VALUE UBYTE_MAX
   
   // Factory registration methods
   
   protected static void register(byte pageCode, Class<? extends ModePage> page)
   {
      ModePage._pages.put( getParserID(pageCode, -1), page );
   }
   
   protected static void register(byte pageCode, int subPageCode, Class<? extends ModePage> page)
   {
      ModePage._pages.put( getParserID(pageCode, subPageCode), page);
   }
   
   private static long getParserID(byte pageCode, int subPageCode)
   {
      return (pageCode << 32) | subPageCode;
   }
   
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
   
   public static ModePage decode(ByteBuffer input) 
   throws BufferUnderflowException, IOException
   {
      boolean parametersSavable;
      int dataLength;
      boolean subPageFormat;
      byte pageCode;
      int subPageCode;
      
      byte[] header = new byte[1];
      input.get(header);
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(header));
      
      int b1 = in.readUnsignedByte();
      parametersSavable = ((b1 >>> 7) & 0x01) == 1;
      subPageFormat = ((b1 >>> 6) & 0x01) == 1;
      pageCode = (byte)(b1 & 0x3F);
      
      if (subPageFormat)
      {
         header = new byte[3];
         input.get(header);
         in = new DataInputStream(new ByteArrayInputStream(header));
         
         subPageCode = in.readUnsignedByte();
         dataLength = in.readUnsignedShort() - 3;
      }
      else
      {
         header = new byte[1];
         input.get(header);
         in = new DataInputStream(new ByteArrayInputStream(header));
         
         subPageCode = -1;
         dataLength = in.readUnsignedByte() - 1;
      }
      
      try
      {
         ModePage page = ModePage._pages.get(getParserID(pageCode, subPageCode)).newInstance();
         page.decode(parametersSavable, dataLength, input);
         return page;
      }
      catch (InstantiationException e)
      {
         throw new IOException("Could not create new mode page parser: " + e.getMessage());
      }
      catch (IllegalAccessException e)
      {
         throw new IOException("Could not create new mode page parser: " + e.getMessage());
      }
   }
   
   protected void decode(boolean parametersSavable, int dataLength, ByteBuffer input )
   throws BufferUnderflowException, IOException
   {
      this.parametersSavable = parametersSavable;
      decodeModeParameters(dataLength, input);
   }

   public byte getPageCode()
   {
      return pageCode;
   }

   public int getSubPageCode()
   {
      return subPageCode;
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
    * @throws Exception If the input byte buffer was too short or contained invalid information.
    */
   protected abstract void decodeModeParameters(int dataLength, ByteBuffer input)
         throws BufferUnderflowException, IllegalArgumentException;
   
   
   public void encode(ByteBuffer output) throws BufferOverflowException
   {
      // Below, header is 2 bytes for page format, 4 bytes for subpage format.
      ByteArrayOutputStream page = new ByteArrayOutputStream( this.subPageFormat ? 4 : 2 );
      DataOutputStream out = new DataOutputStream(page);
      
      try
      {
         int b1 = 0;
         
         if ( this.parametersSavable )
         {
            b1 |= 0x80;
         }
         if ( this.subPageFormat )
         {
            b1 |= 0x40;
         }
         
         b1 |= (this.pageCode & 0x3F);
         
         out.writeByte(b1);
         
         if ( this.subPageFormat )
         {
            out.writeByte(this.subPageCode);
            out.writeShort(this.getPageLength());
         }
         else
         {
            out.writeByte(this.getPageLength());
         }
         
         output.put(page.toByteArray());
         this.encodeModeParameters(output);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode mode page.");
      }
      
   }
}
