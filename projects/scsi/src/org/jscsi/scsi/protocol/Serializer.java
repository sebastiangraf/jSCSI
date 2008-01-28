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

package org.jscsi.scsi.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A factory object capable of selecting the proper class to decode data from an incoming byte
 * buffer.
 */
public interface Serializer
{
   /**
    * Returns the object represented by the incoming byte buffer data.
    * @param <T> An object representing the incoming data.
    * @param buffer A byte buffer set at the beginning of the serialized data.
    * @return An encodable object decoded from the input data.
    * @throws IOException If an object could not be decoded from the incoming data.
    */
   <T extends Encodable> T decode(ByteBuffer buffer) throws IOException;

}
