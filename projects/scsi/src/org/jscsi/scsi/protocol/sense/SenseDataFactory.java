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
// Date: Oct 26, 2007
//---------------------

package org.jscsi.scsi.protocol.sense;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Serializer;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException.ResponseCode;
import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

// TODO: Describe class or interface
public class SenseDataFactory implements Serializer
{
   private static Logger _logger = Logger.getLogger(SenseDataFactory.class);

   public SenseData decode(ByteBuffer buffer) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(buffer));
      
      int b1 = in.readUnsignedByte();
      ResponseCode code = ResponseCode.valueOf( (byte)(b1 & 0x7F) ); // throws IOException
      
      SenseData sense = null;
      
      switch (code)
      {
         case CURRENT_FIXED:
            sense = new FixedSenseData();
            break;
         case CURRENT_DESCRIPTOR:
            sense = new DescriptorSenseData();
            break;
         case DEFERRED_FIXED:
            sense = new FixedSenseData();
            break;
         case DEFERRED_DESCRIPTOR:
            sense = new DescriptorSenseData();
            break;
      }
      
      sense.decode( new byte[] {(byte)b1}, buffer);
      return sense;
   }
   
   

}


