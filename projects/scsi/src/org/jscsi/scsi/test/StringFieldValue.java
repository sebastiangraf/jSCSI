//
// Cleversafe open-source code header - Version 1.1 - December 1, 2006
//
// Cleversafe Dispersed Storage(TM) is software for secure, private and
// reliable storage of the world's data using information dispersal.
//
// Copyright (C) 2005-2007 Cleversafe, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
// USA.
//
// Contact Information: Cleversafe, 10 W. 35th Street, 16th Floor #84,
// Chicago IL 60616
// email licensing@cleversafe.org
//
// END-OF-HEADER
//-----------------------
// Author: wleggette
//
// Date: Oct 29, 2007
//---------------------

package org.jscsi.scsi.test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

// TODO: Describe class or interface
public class StringFieldValue implements FieldValue
{
   private static Logger _logger = Logger.getLogger(StringFieldValue.class);
   private String value;

   public int getBitLength()
   {
      return value.length() * 8;
   }
   
   public List<Object> getValues()
   {
      List<Object> vals = new ArrayList<Object>();
      vals.add(this.value);
      return vals;
   }
   
   


   public Iterator<Iterable<Boolean>> iterator()
   {
      List<Iterable<Boolean>> list = new ArrayList<Iterable<Boolean>>(1);
      list.add( new Iterable<Boolean>()
         {
            public Iterator<Boolean> iterator()
            {
               return new ByteArrayFieldValue.BitIterator(value.getBytes());
            }
            
         } );
      return list.iterator();
   }

   public int getLength()
   {
      return value.length();
   }


   public FieldType getType()
   {
      return FieldType.STRING;
   }
   
   @Override
   public String toString()
   {
      return "string(" + this.getLength() + "):\"" + this.value + "\"";
   }

   public Parser parse(String input, int offset) throws IOException
   {
      String[] elems = input.split(":");
      if ( elems.length != 2 )
      {
         throw new IOException("field value does not indicate type or value (column " + offset + ")");
      }
      
      int length = BitFieldValue.parseFieldLength("string", elems[0], offset);
      
      if ( elems[1].length() != length )
      {
         throw new IOException(
               "indicated field length (" + length + ") not equal to value length (column " +
               (offset + "string(".length()) + ")" );
      }
      
      this.value = elems[1];
      return this;
   }

}


