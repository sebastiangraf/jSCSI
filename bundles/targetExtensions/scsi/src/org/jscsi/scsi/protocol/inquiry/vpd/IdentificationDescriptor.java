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
import java.io.DataOutputStream;
import java.io.IOException;

public class IdentificationDescriptor
{
   private IdentifierType identifierType;

   private int protocolIdentifier;
   private int codeSet;
   private boolean PIV;
   private int association;
   private byte[] identifier;

   public IdentificationDescriptor()
   {
   }

   public IdentificationDescriptor(
         IdentifierType identifierType,
         int protocolIdentifier,
         int codeSet,
         boolean PIV,
         int association,
         byte[] identifier)
   {
      this.identifierType = identifierType;
      this.protocolIdentifier = protocolIdentifier;
      this.codeSet = codeSet;
      this.PIV = PIV;
      this.association = association;
      this.identifier = identifier;
   }

   public byte[] encode() throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(this.identifier.length + 4);
      DataOutputStream out = new DataOutputStream(baos);

      try
      {
         // byte 0
         out.writeByte((this.getProtocolIdentifier() << 4) | this.getCodeSet());

         // byte 1
         int b1 = (this.isPIV() ? 1 : 0) << 7;
         b1 |= this.getAssociation() << 4;
         b1 |= this.getIdentifierType().value();
         out.writeByte(b1);

         // byte 2
         out.writeByte(0);

         // byte 3
         out.writeByte(this.identifier.length);

         // identifier
         out.write(this.identifier);

         return baos.toByteArray();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   /////////////////////////////////////////////////////////////////////////////
   // getters/setters

   public int getProtocolIdentifier()
   {
      return protocolIdentifier;
   }

   public void setProtocolIdentifier(int protocolIdentifier)
   {
      this.protocolIdentifier = protocolIdentifier;
   }

   public int getCodeSet()
   {
      return codeSet;
   }

   public void setCodeSet(int codeSet)
   {
      this.codeSet = codeSet;
   }

   public boolean isPIV()
   {
      return PIV;
   }

   public void setPIV(boolean piv)
   {
      PIV = piv;
   }

   public int getAssociation()
   {
      return association;
   }

   public void setAssociation(int association)
   {
      this.association = association;
   }

   public IdentifierType getIdentifierType()
   {
      return identifierType;
   }

   public void setIdentifierType(IdentifierType identifierType)
   {
      this.identifierType = identifierType;
   }

   public byte[] getIdentifier()
   {
      return identifier;
   }

   public void setIdentifier(byte[] identifier)
   {
      this.identifier = identifier;
   }

   public int getIdentifierLength()
   {
      return this.identifier.length;
   }
}
