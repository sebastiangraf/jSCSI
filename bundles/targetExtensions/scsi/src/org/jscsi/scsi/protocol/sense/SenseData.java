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

package org.jscsi.scsi.protocol.sense;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Encodable;
import org.jscsi.scsi.protocol.sense.additional.ActualRetryCount;
import org.jscsi.scsi.protocol.sense.additional.FieldPointer;
import org.jscsi.scsi.protocol.sense.additional.NoSenseKeySpecific;
import org.jscsi.scsi.protocol.sense.additional.ProgressIndication;
import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException.ResponseCode;

public abstract class SenseData implements Encodable
{
   private SenseException.ResponseCode responseCode;

   /*
    * We ignore the following bits (often because we only support SBC-2 type targets:
    * 
    * - FILEMARK
    * - EOM
    * - ILI          (don't currently support READ LONG, WRITE LONG commands
    */

   private byte[] information;
   private byte[] commandSpecificInformation;
   private SenseKeySpecificField senseKeySpecific;

   private KCQ kcq;

   public SenseData(
         ResponseCode responseCode,
         KCQ kcq,
         byte[] information,
         byte[] commandSpecificInformation,
         SenseKeySpecificField senseKeySpecific)
   {
      super();
      this.responseCode = responseCode;
      this.information = information;
      this.commandSpecificInformation = commandSpecificInformation;
      this.senseKeySpecific = senseKeySpecific;
      this.kcq = kcq;
   }

   public byte getResponseCode()
   {
      return this.responseCode.code();
   }

   public boolean isValid()
   {
      return this.information != null;
   }

   public boolean isCurrent()
   {
      switch (this.responseCode)
      {
         case CURRENT_FIXED :
            return true;
         case CURRENT_DESCRIPTOR :
            return true;
         case DEFERRED_FIXED :
            return false;
         case DEFERRED_DESCRIPTOR :
            return false;
      }
      return false;
   }

   public boolean isDeferred()
   {
      return !isCurrent();
   }

   public KCQ getKCQ()
   {
      return this.kcq;
   }

   public SenseKey getSenseKey()
   {
      return this.kcq.key();
   }

   /**
    * Workaround used by testing framework. Should remove once framework can interpret SenseKey
    * object.
    * @deprecated
    */
   public int getSenseKeyValue()
   {
      return this.kcq.key().value();
   }

   public int getSenseCode()
   {
      return this.kcq.code();
   }

   public int getSenseCodeQualifier()
   {
      return this.kcq.qualifier();
   }

   public byte[] getInformation()
   {
      return this.information == null ? new byte[]{
            0, 0, 0, 0
      } : this.information;
   }

   public byte[] getCommandSpecificInformation()
   {
      return commandSpecificInformation;
   }

   public SenseKeySpecificField getSenseKeySpecific()
   {
      return senseKeySpecific;
   }

   public abstract void decodeSenseKeySpecific(SenseKeySpecificField field) throws IOException;

   public abstract byte[] encode();

   public abstract void decode(byte[] header, ByteBuffer input) throws IOException;

   protected SenseData()
   {
   }

   protected static SenseKeySpecificField decodeSenseKeySpecificField(SenseKey key, ByteBuffer input)
         throws IOException
   {
      SenseKeySpecificField field;
      switch (key)
      {
         case NO_SENSE :
            field = new ProgressIndication();
         case RECOVERED_ERROR :
            field = new ActualRetryCount();
         case NOT_READY :
            field = new ProgressIndication();
         case MEDIUM_ERROR :
            field = new ActualRetryCount();
         case HARDWARE_ERROR :
            field = new ActualRetryCount();
         case ILLEGAL_REQUEST :
            field = new FieldPointer();
         case COPY_ABORTED :
            field = new NoSenseKeySpecific(); // TODO: decode segment pointer, not supported now
         default :
            field = new NoSenseKeySpecific();
      }
      field.decode(input);
      return field;
   }

   protected void setResponseCode(SenseException.ResponseCode responseCode)
   {
      this.responseCode = responseCode;
   }

   protected void setInformation(byte[] information)
   {
      this.information = information;
   }

   protected void setCommandSpecificInformation(byte[] commandSpecificInformation)
   {
      this.commandSpecificInformation = commandSpecificInformation;
   }

   protected void setKcq(KCQ kcq)
   {
      this.kcq = kcq;
   }

}
