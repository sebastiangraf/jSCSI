/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
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

package org.jscsi.scsi.protocol.inquiry.vpd;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class SupportedVPDPages extends VPDPage
{
   public static final int PAGE_CODE = 0x00;

   private List<Integer> supportedCodes;

   public SupportedVPDPages(
         int peripheralQualifier,
         int peripheralDeviceType,
         List<Integer> supportedCodes)
   {
      this.setPageCode(PAGE_CODE);
      this.setPeripheralQualifier(peripheralQualifier);
      this.setPeripheralDeviceType(peripheralDeviceType);
      this.supportedCodes = supportedCodes;
   }

   public SupportedVPDPages(int peripheralQualifier, int peripheralDeviceType)
   {
      this(peripheralQualifier, peripheralDeviceType, new LinkedList<Integer>());
   }

   /////////////////////////////////////////////////////////////////////////////
   // constructors

   public void decode(byte[] header, ByteBuffer buffer) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(buffer));

      // byte 0
      int b0 = in.readUnsignedByte();
      this.setPeripheralQualifier(b0 >>> 5);
      this.setPeripheralDeviceType(b0 & 0x1F);

      // byte 1
      int b1 = in.readUnsignedByte();

      if (b1 != PAGE_CODE)
      {
         throw new IOException("invalid page code: " + Integer.toHexString(b1));
      }

      this.setPageCode(b1);

      // byte 2
      in.readUnsignedByte();

      // byte 3
      int b3 = in.readUnsignedByte();

      // supported codes list
      for (int i = b3; i > 0; i--)
      {
         this.supportedCodes.add(in.readUnsignedByte());
      }
   }

   public byte[] encode()
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(this.supportedCodes.size() + 4);
      DataOutputStream out = new DataOutputStream(baos);

      try
      {
         // byte 0
         out.writeByte((this.getPeripheralQualifier() << 5) | this.getPeripheralDeviceType());

         // byte 1
         out.writeByte(this.getPageCode());

         // byte 2
         out.writeByte(0);

         // byte 3
         out.writeByte(this.supportedCodes.size());

         // supported codes list
         for (int code : this.supportedCodes)
         {
            out.writeByte(code);
         }

         return baos.toByteArray();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   /////////////////////////////////////////////////////////////////////////////

   public void addSupportedCode(int pageCode)
   {
      this.supportedCodes.add(pageCode);
   }

   public List<Integer> getSupportedCodes()
   {
      return supportedCodes;
   }
}
