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
// @author: mmotwani
//
// Date: Nov 14, 2007
//---------------------

package org.jscsi.scsi.protocol.sense;

import static org.junit.Assert.fail;

import org.jscsi.scsi.protocol.Serializer;
import org.jscsi.scsi.protocol.SerializerTest;
import org.jscsi.scsi.protocol.sense.additional.ActualRetryCount;
import org.jscsi.scsi.protocol.sense.additional.FieldPointer;
import org.jscsi.scsi.protocol.sense.additional.NoSenseKeySpecific;
import org.jscsi.scsi.protocol.sense.additional.ProgressIndication;
import org.junit.Test;

// TODO: Describe class or interface
public class SenseKeySpecificFieldTest
{
   private static final String DEFAULT_PACKAGE = "org.jscsi.scsi.protocol.sense.additional";

   private static String ACTUAL_RETRY_COUNT =
         "ActualRetryCount,reserved=1:0b1,reserved=7:0x00,ActualRetryCount=16:std";

   private static String PROGRESS_INDICATION =
         "ProgressIndication,reserved=1:0b1,reserved=7:0x00,ProgressIndication=16:std";

   private static String FIELD_POINTER =
         "FieldPointer,reserved=1:0b1,CD=1:0b1,reserved=2:0b0,BPV=1:0b1,BitPointer=3:std,FieldPointer=16:std";

   private static String NO_SENSE_KEY_SPECIFIC =
         "NoSenseKeySpecific,reserved=1:0b0,reserved=7:0x00,reserved=16:0x00";

   private void runTest(Serializer serializer, String specification)
   {
      try
      {
         new SerializerTest(serializer, DEFAULT_PACKAGE, specification).runTest();
      }
      catch (Exception e)
      {
         fail(e.getMessage());
      }
   }

   @Test
   public void parseActualRetryCount()
   {
      runTest(new ActualRetryCount(), ACTUAL_RETRY_COUNT);
   }

   @Test
   public void parseProgressIndication()
   {
      runTest(new ProgressIndication(), PROGRESS_INDICATION);
   }

   @Test
   public void parseFieldPointer()
   {
      runTest(new FieldPointer(), FIELD_POINTER);
   }

   @Test
   public void parseNoSenseKeySpecific()
   {
      runTest(new NoSenseKeySpecific(), NO_SENSE_KEY_SPECIFIC);
   }
}
