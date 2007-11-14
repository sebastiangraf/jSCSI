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

/**
 * Valid sense key values as specified in SPC-3. Sense key descriptions are quoted from 
 * section 4.5.6 [SPC3].
 */
public enum SenseKey
{
   /**
    * Indicates that there is no specific sense key information to be reported.
    */
   NO_SENSE(0x00),
   
   /**
    * Indicates that the command completed successfully, with some recovery action performed
    * by the device server.
    */
   RECOVERED_ERROR(0x01),
   
   /**
    * Indicates that the logical unit is not accessible.
    */
   NOT_READY(0x02),
   
   /**
    * Indicates that the command terminated with a non-recovered error condition that may
    * have been caused by a flaw in the medium or an error in the recorded data.
    */
   MEDIUM_ERROR(0x03),
   
   /**
    * Indicates that the device server detected a non-recoverable hardware failure while
    * performing the command or during self test.
    */
   HARDWARE_ERROR(0x04),
   
   /**
    * Indicates one of several conditions caused by invalid parameters in the CDB or parameter
    * data.
    */
   ILLEGAL_REQUEST(0x05),
   
   /**
    * Indicates that a unit attention condition has been established.
    */
   UNIT_ATTENTION(0x06),
   
   /**
    * Indicates that a command that reads or writes the medium was attempted on a block that
    * is protected. The read or write operation is not performed.
    */
   DATA_PROTECT(0x07),
   
   /**
    * Indicates that a write-once device or a sequential-access device encountered a blank medium
    * or format-defined end-of-data indicatation while reading or that a write-once device
    * encountered a non-blank medium while writing.
    */
   BLANK_CHECK(0x08),
   
   /**
    * This sense key is available for reporting vendor specific conditions.
    */
   VENDOR_SPECIFIC(0x09),
   
   /**
    * Indicates an EXTENDED COPY command was aborted due to an error condition on the source
    * device, the destination device, or both.
    */
   COPY_ABORTED(0x0A),
   
   /**
    * Indicates that the device server aborted the command. The application client may be able to
    * recover by trying the command again.
    */
   ABORTED_COMMAND(0x0B),
   
   /**
    * Indicates that a buffered SCSI device has reached the end-of-partition and data may
    * remain in the buffer that has not been written to the medium.
    */
   VOLUME_OVERFLOW(0x0D),
   
   /**
    * Indicates that the source data did not match the data read from the medium.
    */
   MISCOMPARE(0x0E);
   
   private final int value;
   
   private static Map<Integer, SenseKey> mapping;
   
   private SenseKey(final int value)
   {
      if ( SenseKey.mapping == null )
      {
         SenseKey.mapping = new HashMap<Integer,SenseKey>();
      }
      Map<Integer,SenseKey> map = SenseKey.mapping;
      SenseKey.mapping.put(value, this);
      this.value = value;
   }
   
   public final int value()
   {
      return value;
   }
   
   public static final SenseKey valueOf( int value ) throws IOException
   {
      SenseKey v = SenseKey.mapping.get(value);
      if ( v == null )
      {
         throw new IOException("Invalid sense key value: " + value);
      }
      else
      {
         return v;
      }
   }
   
   
   

}


