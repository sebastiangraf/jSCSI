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

package org.jscsi.scsi.protocol.sense;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.sense.additional.NoSenseKeySpecific;
import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException.ResponseCode;
import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

//TODO: Describe class or interface
public class FixedSenseData extends SenseData
{

   private static final int FIXED_SENSE_DATA_LENGTH = 18;

   private byte[] senseKeySpecificBuffer = null;

   public FixedSenseData()
   {
      super();
   }

   public FixedSenseData(
         boolean current,
         KCQ kcq,
         byte[] information,
         byte[] commandSpecificInformation,
         SenseKeySpecificField senseKeySpecific)
   {
      super(ResponseCode.valueOf(current, false), kcq, information, commandSpecificInformation,
            senseKeySpecific);
   }

   @Override
   public void decodeSenseKeySpecific(SenseKeySpecificField field) throws BufferUnderflowException,
         IOException
   {
      field.decode(ByteBuffer.wrap(this.senseKeySpecificBuffer));
   }

   @Override
   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      assert header != null && header.length == 1 : "input header is invalid";
      boolean valid = (header[0] & 0x80) != 0;
      this.setResponseCode(ResponseCode.valueOf((byte) (header[0] & 0x7F)));

      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      // read in first segment of fixed format sense data
      in.readByte();
      int key = in.readUnsignedByte() & 0x0F; // TODO: FILEMARK, EOM, and ILI are unsupported
      byte[] info = new byte[4];
      in.read(info);
      int length = in.readUnsignedByte() - 10; // length of next segment, minus required read-in
      length = length < 0 ? 0 : length;

      // read in the next segment of the fixed format sense data
      byte[] cmdi = new byte[4];
      in.read(cmdi);
      int code = in.readUnsignedByte();
      int qualifier = in.readUnsignedByte();
      in.readByte();
      this.senseKeySpecificBuffer = new byte[3];
      in.read(this.senseKeySpecificBuffer);
      // the rest of the additional sense bytes are ignored
      // (vendor specific bytes not supported)
      in.skip(length);

      KCQ kcq = KCQ.valueOf(key, code, qualifier); // throws IOException on invalid values

      // Set appropriate fields

      this.setKcq(kcq);
      this.setInformation(valid ? info : null);
      this.setCommandSpecificInformation(cmdi);

      // sense key specific buffer already set, will be decoded when exception is constructed.
   }

   @Override
   public byte[] encode()
   {
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bs);

      try
      {
         byte[] info = this.getInformation();
         byte[] cmdi = this.getCommandSpecificInformation();
         KCQ kcq = this.getKCQ();
         SenseKeySpecificField field = this.getSenseKeySpecific();
         if (field == null)
         {
            field = new NoSenseKeySpecific();
         }

         // returned response code is byte with max value 0x7F (7-bit).
         int response = this.getResponseCode();

         // We mark VALID as 0 if info is null or over max size (4-byte).
         if (info != null && info.length == 4)
         {
            response |= 0x80;
         }
         else if (info.length < 4)
         {
            throw new RuntimeException("Returned sense information has invalid length: "
                  + info.length);
         }
         else
         {
            info = new byte[4]; // Ignore invalid or null value, will write all zeros to field
         }

         if (cmdi == null || cmdi.length != 4)
         {
            cmdi = new byte[4]; // Ignore invalid command specific information lengths.
         }

         out.writeByte(response); // VALID and RESPONSE CODE
         out.writeByte(0);
         out.writeByte(kcq.key().value()); // TODO: FILEMARK, EOM, and ILI not current supported
         out.write(info);
         out.writeByte(10); // no "Additional sense bytes" will be written, last byte is #17
         out.write(cmdi);
         out.writeByte(kcq.code());
         out.writeByte(kcq.qualifier());
         out.writeByte(0);
         out.write(field.encode());

         assert bs.toByteArray().length == FIXED_SENSE_DATA_LENGTH : "Invalid encoded sense data";
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode fixed format sense data.");
      }

      return bs.toByteArray();
   }

}
