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
// Date: Oct 10, 2007
//---------------------

package org.jscsi.scsi.protocol.sense;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// TODO: Describe class or interface
public enum KCQ
{
   NO_ERROR( 0, 0, 0 ),
   
   PERIPHERAL_DEVICE_WRITE_FAULT_RECOVERED( 1, 0x03, 0x00 ),
   INTERNAL_TARGET_FAILURE_RECOVERED( 1, 0x44, 0x00 ),
   
   LOGICAL_UNIT_IS_IN_PROCESS_OF_BECOMING_READY( 2, 0x04, 0x01 ),
   LOGICAL_UNIT_FAILED_SELF_CONFIGURATION( 2, 0x4C, 0x00 ),
   
   WRITE_ERROR( 3, 0x00, 0x00 ),
   PERIPHERAL_DEVICE_WRITE_FAULT_ON_MEDIUM( 3, 0x03, 0x00 ),
   UNRECOVERED_READ_ERROR( 3, 0x11, 0x00 ),
   CANNOT_READ_MEDIUM_UNKNOWN_FORMAT( 3, 0x30, 0x01 ),
   CANNOT_READ_MEDIUM_INCOMPATIBLE_FORMAT( 3, 0x30, 0x02 ),
   CANNOT_WRITE_MEDIUM_UNKNOWN_FORMAT( 3, 0x30, 0x04 ),
   CANNOT_WRITE_MEDIUM_INCOMPATIBLE_FORMAT( 3, 0x30, 0x05 ),
   
   PERIPHERAL_DEVICE_WRITE_FAULT_ON_HARDWARE( 4, 0x03, 0x00 ),
   INTERNAL_TARGET_FAILURE_ON_HARDWARE( 4, 0x44, 0x00 ),
   
   INVALID_COMMAND_OPERATION_CODE( 5, 0x20, 0x00 ),
   LOGICAL_BLOCK_ADDRESS_OUT_OF_RANGE( 5, 0x21, 0x00 ),
   INVALID_FIELD_IN_CDB( 5, 0x24, 0x00 ),
   LOGICAL_UNIT_NOT_SUPPORTED( 5, 0x25, 0x00 ),
   INVALID_FIELD_IN_PARAMETER_LIST( 5, 0x26, 0x00 ),
   PARAMETER_NOT_SUPPORTED( 5, 0x26, 0x01 ),
   PARAMETER_VALUE_INVALID( 5, 0x26, 0x02 ),
   COMMAND_SEQUENCE_ERROR( 5, 0x2C, 0x00 ),
   INVALID_MESSAGE_ERROR( 5, 0x49, 0x00 ),
   
   PARAMETERS_CHANGED( 6, 0x2A, 0x00 ),
   MODE_PARAMETERS_CHANGED( 6, 0x2A, 0x01 ),
   CAPACITY_DATA_HAS_CHANGED( 6, 0x2A, 0x09 ),
   INQUIRY_DATA_HAS_CHANGED( 6, 0x3F, 0x03 ),
   REPORTED_LUNS_DATA_HAS_CHANGED( 6, 0x3F, 0x0E ),
   
   WRITE_PROTECTED( 7, 0x27, 0x00 ),
   
   ABORTED_COMMAND_NO_ADDITIONAL_INFORMATION( 0xB, 0x00, 0x00 ),
   SYNCHRONOUS_DATA_TRANSFER_ERROR( 0xB, 0x1B, 0x00 ),
   LOGICAL_UNIT_NOT_SUPPORTED_ABORTED( 0xB, 0x25, 0x00 ),
   INTERNAL_TARGET_FAILURE_ABORTED( 0xB, 0x44, 0x00 ),
   INVALID_MESSAGE_ERROR_ABORTED( 0xB, 0x49, 0x00 ),
   
   ;
   
   
   private final SenseKey key;
   private final int code;  // UBYTE_MAX
   private final int qualifier; // UBYTE_MAX
   
   
   private static Map<Long, KCQ> mapping = new HashMap<Long, KCQ>();
   
   private KCQ(int key, int code, int qualifier)
   {
      try
      {
         this.key = SenseKey.valueOf(key);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Invalid sense key value specified");
      }
      this.code = code;
      this.qualifier = qualifier;
      
      KCQ.mapping.put( get20b(key, code, qualifier), this );
   }
   
   // Returns a long containing the 20-bit KCQ.
   private static long get20b(int key, int code, int qualifier)
   {
      long kcq = 0;
      kcq |= ((key & 0x0F) << 16);
      kcq |= ((code & 0xFF) << 8);
      kcq |= (qualifier & 0xFF);
      return kcq;
   }
   
   public SenseKey key()
   {
      return this.key;
   }
   
   public int code()
   {
      return this.code;
   }
   
   public int qualifier()
   {
      return this.qualifier;
   }
   
   public static final KCQ valueOf( SenseKey key, int code, int qualifier ) throws IOException
   {
      return valueOf(key.value(), code, qualifier);
   }
   
   public static final KCQ valueOf( int key, int code, int qualifier ) throws IOException
   {
      KCQ kcq = KCQ.mapping.get( get20b(key, code, qualifier) );
      if ( kcq == null )
      {
         throw new IOException("Invalid KCQ values: " + key + ", " + code + ", " + qualifier);
      }
      else
      {
         return kcq;
      }
   }
   
   

}


