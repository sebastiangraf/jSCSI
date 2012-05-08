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

package org.jscsi.scsi.protocol.inquiry.vpd;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class DeviceIdentificationVPD extends VPDPage
{
   public static final int PAGE_CODE = 0x83;

   private int pageLength = 0;
   private List<IdentificationDescriptor> descriptorList =
         new LinkedList<IdentificationDescriptor>();

   public DeviceIdentificationVPD(
         int peripheralQualifier,
         int peripheralDeviceType,
         List<IdentificationDescriptor> descriptorList)
   {
      this.setPeripheralQualifier(peripheralQualifier);
      this.setPeripheralDeviceType(peripheralDeviceType);
      this.descriptorList = descriptorList;
   }

   public DeviceIdentificationVPD(int peripheralQualifier, int peripheralDeviceType)
   {
      this(peripheralQualifier, peripheralDeviceType, new LinkedList<IdentificationDescriptor>());
   }

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

      // byte 2 - 3
      this.pageLength = in.readUnsignedShort();

      // identification descriptor list
      descriptorList = this.parseDescriptorList(in);
   }

   public byte[] encode()
   {
      int pageLength = 0;
      for (IdentificationDescriptor id : descriptorList)
      {
         pageLength += id.getIdentifierLength() + 4;
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream(pageLength + 4);
      DataOutputStream out = new DataOutputStream(baos);

      try
      {
         // byte 0
         out.writeByte((this.getPeripheralQualifier() << 5) | this.getPeripheralDeviceType());

         // byte 1
         out.writeByte(this.getPageCode());

         // byte 2 - 3
         out.writeShort(pageLength);

         // identification descriptor list
         for (IdentificationDescriptor id : this.descriptorList)
         {
            out.write(id.encode());
         }

         return baos.toByteArray();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   /////////////////////////////////////////////////////////////////////////////
   // utility

   private List<IdentificationDescriptor> parseDescriptorList(DataInputStream in)
         throws IOException
   {
      List<IdentificationDescriptor> descriptorList = new LinkedList<IdentificationDescriptor>();

      int readPageLength = 0;
      while (readPageLength < this.pageLength)
      {
         IdentificationDescriptor desc = new IdentificationDescriptor();

         // byte 0
         int b0 = in.readUnsignedByte();
         desc.setProtocolIdentifier(b0 >>> 4);
         desc.setCodeSet(b0 & 0x0F);

         // byte 1
         int b1 = in.readUnsignedByte();
         desc.setPIV((b1 >>> 7) == 1);
         desc.setAssociation((b1 & 0x30) >>> 4);
         desc.setIdentifierType(IdentifierType.valueOf(b1 & 0x15));

         // byte 2
         in.readUnsignedByte();

         // byte 3
         int idLength = in.readUnsignedByte();

         // identifier
         byte[] ident = new byte[idLength];
         in.read(ident);
         desc.setIdentifier(ident);

         descriptorList.add(desc);
         readPageLength += idLength + 4;
      }

      return descriptorList;
   }

   /////////////////////////////////////////////////////////////////////////////
   // getters/setters

   public void addIdentificationDescriptor(IdentificationDescriptor identificationDescriptor)
   {
      this.descriptorList.add(identificationDescriptor);
   }

   public List<IdentificationDescriptor> getDescriptorList()
   {
      return descriptorList;
   }

   public void setDescriptorList(List<IdentificationDescriptor> descriptorList)
   {
      this.descriptorList = descriptorList;
   }
}
