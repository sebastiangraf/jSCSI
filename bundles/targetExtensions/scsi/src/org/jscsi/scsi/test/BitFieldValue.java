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

package org.jscsi.scsi.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//TODO: Describe class or interface
public class BitFieldValue implements FieldValue
{
   public static final String LIST_HEAD = "[";
   public static final String LIST_TAIL = "]";
   public static final String HEX_HEAD = "0x";
   public static final String BIN_HEAD = "0b";

   private List<List<Boolean>> value;
   private int size;

   public int getBitLength()
   {
      return this.size;
   }

   public int getLength()
   {
      return this.size;
   }

   public FieldType getType()
   {
      return FieldType.BITS;
   }

   public List<Object> getValues()
   {
      List<Object> list = new ArrayList<Object>();
      list.addAll(this.value);
      return list;
   }

   public Iterator<Iterable<Boolean>> iterator()
   {
      return new Iterator<Iterable<Boolean>>()
      {
         private Iterator<List<Boolean>> it = value.iterator();

         public boolean hasNext()
         {
            return it.hasNext();
         }

         public Iterable<Boolean> next()
         {
            return it.next();
         }

         public void remove()
         {
            it.remove();
         }

      };
   }

   public Parser parse(String input, int offset) throws IOException
   {
      String[] elems = input.split(":");
      if (elems.length != 2)
      {
         throw new IOException("field value does not indicate type or value (column " + offset
               + ")");
      }

      int length = parseFieldLength("bits", elems[0], offset);

      if (elems[1].startsWith(LIST_HEAD))
      {
         if (elems[1].lastIndexOf(']') != elems[1].length() - 1)
         {
            offset += elems[1].length() - 1;
            throw new IOException("field value list not terminated with '[' (column " + offset
                  + ")");
         }

         offset += LIST_HEAD.length(); // move offset after '[' character
         this.value =
               parseValueList(elems[1].substring(LIST_HEAD.length(), elems[1].length()
                     - LIST_TAIL.length()), length, offset);
      }
      else if (elems[1].startsWith(HEX_HEAD))
      {
         offset += HEX_HEAD.length(); // move offset after '0x' characters
         this.value = new ArrayList<List<Boolean>>();
         this.value.add(parseHexValue(elems[1].substring(HEX_HEAD.length()), length, offset));
      }
      else if (elems[1].startsWith(BIN_HEAD))
      {
         offset += BIN_HEAD.length(); // move offset after '0b' characters
         this.value = new ArrayList<List<Boolean>>();
         this.value.add(parseBinaryValue(elems[1].substring(BIN_HEAD.length()), length, offset));
      }
      else
      {
         throw new IOException("unrecognized field value string (column " + offset + ")");
      }

      this.size = length;
      return this;
   }

   public static int parseFieldLength(String expectedFieldType, String fieldLengthString, int offset)
         throws IOException
   {
      int length = 0;

      if (fieldLengthString.startsWith(expectedFieldType + "("))
      {
         try
         {
            length =
                  Integer.parseInt(fieldLengthString.substring(0, fieldLengthString.length() - 1).substring(
                        expectedFieldType.length() + 1));
         }
         catch (NumberFormatException e)
         {
            offset += expectedFieldType.length() + 1; // e.g. "string(".length()
            throw new IOException("field length not a number (column " + offset + ")");
         }
      }
      else
      {
         // Caller should have verified field type
         throw new RuntimeException("Improper field type: " + expectedFieldType);
      }

      return length;
   }

   // Parses binary value; i.e. "0b0010110" should be passed in as "0010110"
   public static List<Boolean> parseBinaryValue(
         String fieldValueString,
         int expectedLength,
         int offset) throws IOException
   {
      List<Boolean> value = new ArrayList<Boolean>();
      for (int i = 0; i < fieldValueString.length(); i++)
      {
         switch (fieldValueString.charAt(i))
         {
            case '0' :
               value.add(false);
               break;
            case '1' :
               value.add(true);
               break;
            default :
               offset += i;
               throw new IOException("Invalid character for binary value (column " + offset + ")");
         }
      }

      // truncate overflow and fill overflow
      if (value.size() < expectedLength)
      {
         for (int i = value.size(); i < expectedLength; i++)
         {
            value.add(0, false);
         }
      }
      else if (value.size() > expectedLength)
      {
         for (int i = value.size() - expectedLength; i > 0; i--)
         {
            value.remove(0);
         }
      }

      return value;
   }

   // Parses hex value; i.e. "0x0F7AB" should be passed in as "0F7AB"
   public static List<Boolean> parseHexValue(String fieldValueString, int expectedLength, int offset)
         throws IOException
   {
      List<Boolean> value = new ArrayList<Boolean>();

      for (int i = 0; i < fieldValueString.length(); i++)
      {
         try
         {
            List<Boolean> v =
                  parseBinaryValue(Integer.toBinaryString(Integer.parseInt(
                        fieldValueString.substring(i, i + 1), 16)), 4, 0);
            for (int j = v.size(); j < 4; j++)
            {
               value.add(false);
            }
            value.addAll(v);
         }
         catch (IOException e)
         {
            // IOException thrown by parseBinaryValue is a RuntimeException because toBinaryString
            // should return perfect values
            offset += i;
            throw new RuntimeException("Internal error parsing hex string (column " + offset + ")");
         }
         catch (NumberFormatException e)
         {
            offset += i;
            throw new IOException("Invalid character for hex value (column " + offset + ")");
         }
      }

      // truncate overflow and fill overflow
      if (value.size() < expectedLength)
      {
         for (int i = value.size(); i < expectedLength; i++)
         {
            value.add(0, false);
         }
      }
      else if (value.size() > expectedLength)
      {
         for (int i = value.size() - expectedLength; i > 0; i--)
         {
            value.remove(0);
         }
      }
      return value;
   }

   // Parses value list; i.e. "[VAL;VAL;VAL]" should be passed in as "VAL;VAL;VAL"
   public static List<List<Boolean>> parseValueList(
         String fieldValueString,
         int expectedLength,
         int offset) throws IOException
   {
      List<List<Boolean>> values = new ArrayList<List<Boolean>>();

      for (String val : fieldValueString.split(";"))
      {
         if (val.startsWith(HEX_HEAD))
         {
            values.add(parseHexValue(val.substring(2), expectedLength, offset + 2));
         }
         else if (val.startsWith(BIN_HEAD))
         {
            values.add(parseBinaryValue(val.substring(2), expectedLength, offset + 2));
         }
         else
         {
            throw new IOException("Invalid field value in list (column " + offset + ")");
         }

         offset += val.length() + 1; // "VAL;".length()
      }

      return values;
   }

   public static List<List<Boolean>> generateBitPattern(int fieldSize)
   {
      List<List<Boolean>> list = new ArrayList<List<Boolean>>();

      if (fieldSize == 1)
      {
         List<Boolean> l = new ArrayList<Boolean>();
         l.add(true);
         list.add(l);
         l = new ArrayList<Boolean>();
         l.add(false);
         list.add(l);
         return list;
      }

      List<Boolean> l = null;

      // 0x00 pattern
      l = new ArrayList<Boolean>(fieldSize);
      for (int i = 0; i < fieldSize; i++)
      {
         l.add(false);
      }
      list.add(l);

      // 0x01 pattern
      l = new ArrayList<Boolean>(fieldSize);
      for (int i = 0; i < fieldSize - 1; i++)
      {
         l.add(false);
      }
      l.add(true);
      list.add(l);

      // 0x7F pattern
      l = new ArrayList<Boolean>(fieldSize);
      l.add(false);
      for (int i = 1; i < fieldSize; i++)
      {
         l.add(true);
      }
      list.add(l);

      // 0xFF pattern
      l = new ArrayList<Boolean>(fieldSize);
      for (int i = 0; i < fieldSize; i++)
      {
         l.add(true);
      }
      list.add(l);

      return list;
   }

   public static List<List<Boolean>> generateBitPattern(int fieldSize, List<List<Boolean>> tailList)
   {
      if (fieldSize == 0)
         return tailList;

      if (fieldSize < 8)
      {
         // Generate the head lists and append the tail lists to them
         List<List<Boolean>> list = new ArrayList<List<Boolean>>();

         for (List<Boolean> head : generateBitPattern(fieldSize))
         {
            if (tailList != null)
            {
               for (List<Boolean> tail : tailList)
               {
                  List<Boolean> l = new ArrayList<Boolean>();
                  l.addAll(head);
                  l.addAll(tail);
                  list.add(l);
               }
            }
            else
            {
               list.add(head);
            }
         }

         return list;
      }
      else
      {
         // Generate this chain of 8-bit lists and append the tail lists to them
         List<List<Boolean>> list = new ArrayList<List<Boolean>>();

         for (List<Boolean> head : generateBitPattern(8))
         {
            if (tailList != null)
            {
               for (List<Boolean> tail : tailList)
               {
                  List<Boolean> l = new ArrayList<Boolean>();
                  l.addAll(head);
                  l.addAll(tail);
                  list.add(l);
               }
            }
            else
            {
               list.add(head);
            }
         }
         return generateBitPattern(fieldSize - 8, list);
      }
   }

}
