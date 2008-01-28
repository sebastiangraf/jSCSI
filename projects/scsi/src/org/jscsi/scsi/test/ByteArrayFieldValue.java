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

import org.apache.log4j.Logger;

//TODO: Describe class or interface
public class ByteArrayFieldValue implements FieldValue
{
   private static Logger _logger = Logger.getLogger(ByteArrayFieldValue.class);

   private List<Byte> value;

   public static class BitIterator implements Iterator<Boolean>
   {
      private List<Byte> value;
      private int pos;

      public BitIterator(List<Byte> value)
      {
         this.value = value;
         this.pos = 0;
      }

      public BitIterator(byte[] value)
      {
         this.value = new ArrayList<Byte>();
         for (byte b : value)
         {
            this.value.add(b);
         }
      }

      public boolean hasNext()
      {
         return pos < (this.value.size() * 8);
      }

      public Boolean next()
      {
         int mask = 0x80 >>> (pos % 8);
         return (value.get(pos++ / 8) & mask) != 0;
      }

      public void remove()
      {
         throw new UnsupportedOperationException("Not implemented");

      }

   }

   public int getBitLength()
   {
      return this.value.size() * 8;
   }

   public int getLength()
   {
      return this.value.size();
   }

   public FieldType getType()
   {
      return FieldType.BYTES;
   }

   public List<Object> getValues()
   {
      // returning a list of lists of bytes
      List<Object> vals = new ArrayList<Object>();
      vals.add(this.value);
      return vals;
   }

   public Iterator<Iterable<Boolean>> iterator()
   {
      List<Iterable<Boolean>> list = new ArrayList<Iterable<Boolean>>(1);
      list.add(new Iterable<Boolean>()
      {
         public Iterator<Boolean> iterator()
         {
            return new ByteArrayFieldValue.BitIterator(value);
         }

      });
      return list.iterator();
   }

   @Override
   public String toString()
   {
      StringBuffer buf = new StringBuffer();
      buf.append("bytes(" + this.getLength() + "):0x");
      for (Byte b : this.value)
      {
         buf.append(String.format("%02X", b));
      }
      return buf.toString();
   }

   public Parser parse(String input, int offset) throws IOException
   {
      String[] elems = input.split(":");
      if (elems.length != 2)
      {
         throw new IOException("field value does not indicate type or value (column " + offset
               + ")");
      }

      int length = 0;

      if (elems[0].startsWith("bytes("))
      {
         try
         {
            length = Integer.parseInt(elems[0].replaceAll("bytes(", "").replaceAll(")", ""));
         }
         catch (NumberFormatException e)
         {
            throw new IOException("field length not a number (column "
                  + (offset + "bytes(".length()) + ")");
         }
      }
      else
      {
         // Caller should have verified field type
         throw new RuntimeException("Improper field type: string");
      }

      if (!elems[1].startsWith("0x"))
      {
         throw new IOException("field value is not in hexidecimal format (column "
               + (offset + elems[0].length() + 1) + ")");
      }
      else
      {
         this.value = new ArrayList<Byte>(length);
         int pos = 2; // starting after "0x"
         if ((elems[1].length() % 2) != 0)
         {
            try
            {
               this.value.add(Byte.parseByte(elems[1].substring(pos, pos + 1), 16));
            }
            catch (NumberFormatException e)
            {
               throw new IOException("field value not in hexidecimal format (column "
                     + (offset + elems[0].length() + "0x".length() + pos) + ")");
            }
            pos++;
         }

         for (; pos < elems[1].length(); pos += 2)
         {
            try
            {
               this.value.add(Byte.parseByte(elems[1].substring(pos, pos + 2), 16));
            }
            catch (NumberFormatException e)
            {
               throw new IOException("field value not in hexidecimal format (column "
                     + (offset + elems[0].length() + "0x".length() + pos) + ")");
            }
         }
      }

      if (this.value.size() != length)
      {
         throw new IOException("indicated field length (" + length + ") not equal to value "
               + "length (column " + (offset + elems[0].length() + 1) + ")");
      }

      return this;
   }

}
