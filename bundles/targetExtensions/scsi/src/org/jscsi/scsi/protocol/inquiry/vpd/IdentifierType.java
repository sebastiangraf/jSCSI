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

import java.util.HashMap;
import java.util.Map;

public enum IdentifierType
{
   VENDOR_SPECIFIC(0x00),
   T10_VENDOR_ID_BASED(0x01),
   EUI_64_BASED(0x02),
   NAA(0x03),
   RELATIVE_TARGET_PORT_IDENTIFIER(0x04),
   TARGET_PORT_GROUP(0x05),
   LOGICAL_UNIT_GROUP(0x06),
   MD5_LOGICAL_UNIT_IDENTIFIER(0x07),
   SCSI_NAME_STRING(0x08);

   private final int value;

   private static Map<Integer, IdentifierType> mapping;

   private IdentifierType(final int newValue)
   {

      if (IdentifierType.mapping == null)
      {
         IdentifierType.mapping = new HashMap<Integer, IdentifierType>();
      }

      IdentifierType.mapping.put(newValue, this);
      value = newValue;
   }

   public final int value()
   {
      return value;
   }

   public static final IdentifierType valueOf(final int value)
   {
      return IdentifierType.mapping.get(value);
   }

   @Override
   public String toString()
   {
      String output = "<status:";

      switch (IdentifierType.valueOf(value))
      {
         case VENDOR_SPECIFIC :
            output += " vendor specific";
            break;
         case T10_VENDOR_ID_BASED :
            output += " T10 vendor id-based";
            break;
         case EUI_64_BASED :
            output += " EUI 64-based";
            break;
         case NAA :
            output += " NAA";
            break;
         case RELATIVE_TARGET_PORT_IDENTIFIER :
            output += " relative target port identifier";
            break;
         case TARGET_PORT_GROUP :
            output += " target port group";
            break;
         case LOGICAL_UNIT_GROUP :
            output += " logical unit group";
            break;
         case MD5_LOGICAL_UNIT_IDENTIFIER :
            output += " MD5 logical unit identifier";
            break;
         case SCSI_NAME_STRING :
            output += " SCSI name string";
            break;
      }
      return output + ">";
   }
}
