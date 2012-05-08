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
      this.subPageCode = 0;
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
         dataLength = pageLength - 2;
      }
      else
      {
         this.subPageCode = 0;
         pageLength = header[1];
         dataLength = pageLength - 2;
      }

      DataInputStream dataIn = new DataInputStream(new ByteBufferInputStream(buffer));
      decodeModeParameters(dataLength, dataIn);
   }
}
