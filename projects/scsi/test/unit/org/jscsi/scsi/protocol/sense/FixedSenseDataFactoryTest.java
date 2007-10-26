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

import static org.junit.Assert.fail;

import org.jscsi.scsi.protocol.Serializer;
import org.jscsi.scsi.protocol.SerializerTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

// TODO: Describe class or interface
public class FixedSenseDataFactoryTest
{
   private static final String DEFAULT_PACKAGE = "org.jscsi.scsi.protocol.sense";

   private static Serializer serializer;
   
   private static String FIXED_SENSE_DATA_INVALID_INFORMATION =
      "FixedSenseData,Valid=1:0b0,ResponseCode=7:[0x70;0x71],reserved=8:0x00,SenseKeyValue=8:0x0B,Information=32:0x00,reserved=8:0x0A,CommandSpecificinformation=32:std,SenseCode=8:0x49,SenseCodeQualifier=8:0x00,reserved=8:0x00,reserved=24:std";  
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      serializer = new SenseDataFactory();
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
   }

   @Before
   public void setUp() throws Exception
   {
   }

   @After
   public void tearDown() throws Exception
   {
   }
   
   private void runTest( String specification )
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
   public void parseFixedSenseData_InvalidInformation()
   {
      runTest(FIXED_SENSE_DATA_INVALID_INFORMATION);
   }
   

}


