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

package org.jscsi.scsi.protocol;

import static org.junit.Assert.fail;

import org.jscsi.scsi.protocol.cdb.CDBFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

//TODO: Describe class or interface
public class CommandDescriptorBlockFactoryTest
{

   private static final String DEFAULT_PACKAGE = "org.jscsi.scsi.protocol.cdb";

   private static Serializer serializer;

   private static String INQUIRY =
         "Inquiry,OperationCode=8:0x12,reserved=7:0x0,EVPD=1:std,PageCode=8:std,AllocationLength=16:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String MODE_SELECT_6 =
         "ModeSelect6,OperationCode=8:0x15,reserved=3:0x0,PF=1:std,reserved=3:0x0,SP=1:std,reserved=16:0x0,ParameterLength=8:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String MODE_SELECT_10 =
         "ModeSelect10,OperationCode=8:0x55,reserved=3:0x0,PF=1:std,reserved=3:0x0,SP=1:std,reserved=40:0x0,ParameterLength=16:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String MODE_SENSE_6 =
         "ModeSense6,OperationCode=8:0x1A,reserved=4:0x0,DBD=1:std,reserved=3:0x0,PC=2:std,PageCode=6:std,SubPageCode=8:std,AllocationLength=8:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String MODE_SENSE_10 =
         "ModeSense10,OperationCode=8:0x5A,reserved=3:0x0,LLBAA=1:std,DBD=1:std,reserved=3:0x0,PC=2:std,PageCode=6:std,SubPageCode=8:std,reserved=24:0x0,AllocationLength=16:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String READ_6 =
         "Read6,OperationCode=8:0x08,reserved=3:0x0,LogicalBlockAddress=21:std,TransferLength=8:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String READ_10 =
         "Read10,OperationCode=8:0x28,reserved=3:0x0,DPO=1:std,FUA=1:std,reserved=1:0x0,FUA_NV=1:std,reserved=1:0x0,LogicalBlockAddress=32:std,reserved=3:0x0,GroupNumber=5:std,TransferLength=16:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String READ_12 =
         "Read12,OperationCode=8:0xA8,reserved=3:0x0,DPO=1:std,FUA=1:std,reserved=1:0x0,FUA_NV=1:std,reserved=1:0x0,LogicalBlockAddress=32:std,TransferLength=32:std,reserved=3:0x0,GroupNumber=5:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String READ_16 =
         "Read16,OperationCode=8:0x88,reserved=3:0x0,DPO=1:std,FUA=1:std,reserved=1:0x0,FUA_NV=1:std,reserved=1:0x0,LogicalBlockAddress=64:std,TransferLength=32:std,reserved=3:0x0,GroupNumber=5:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String READ_CAPACITY_10 =
         "ReadCapacity10,OperationCode=8:0x25,reserved=8:0x0,LogicalBlockAddress=32:std,reserved=23:0x0,PMI=1:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String READ_CAPACITY_16 =
         "ReadCapacity16,OperationCode=8:0x9E,reserved=3:0x0,ServiceAction=5:0x10,LogicalBlockAddress=64:std,AllocationLength=32:std,reserved=7:0x0,PMI=1:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String REMOVE_DIAGNOSTIC_RESULTS =
         "ReceiveDiagnosticResults,OperationCode=8:0x1C,reserved=7:0x0,PCV=1:std,PageCode=8:std,AllocationLength=16:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String REPORT_LUNS =
         "ReportLuns,OperationCode=8:0xA0,reserved=8:0x0,SelectReport=8:std,reserved=24:0x0,AllocationLength=32:std,reserved=8:0x0,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String REPORT_SUPPORTED_TASK_MANAGEMENT_FUNCTIONS =
         "ReportSupportedTaskManagementFunctions,OperationCode=8:0xA3,reserved=3:0x0,ServiceAction=5:0x0D,reserved=32:0x0,AllocationLength=32:std,reserved=8:0x0,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String REQUEST_SENSE =
         "RequestSense,OperationCode=8:0x03,reserved=7:0x0,DESC=1:std,reserved=16:0x0,AllocationLength=8:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String SEND_DIAGNOSTIC =
         "SendDiagnostic,OperationCode=8:0x1D,SelfTestCode=3:std,PF=1:std,reserved=1:0x0,SelfTest=1:std,DevOffL=1:std,UnitOffL=1:std,reserved=8:0x0,ParameterListLength=16:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String TEST_UNIT_READY =
         "TestUnitReady,OperationCode=8:0x00,reserved=32:0x0,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String WRITE_6 =
         "Write6,OperationCode=8:0x0A,reserved=3:0x0,LogicalBlockAddress=21:std,TransferLength=8:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String WRITE_10 =
         "Write10,OperationCode=8:0x2A,reserved=3:0x0,DPO=1:std,FUA=1:std,reserved=1:0x0,FUA_NV=1:std,reserved=1:0x0,LogicalBlockAddress=32:std,reserved=3:0x0,GroupNumber=5:std,TransferLength=16:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String WRITE_12 =
         "Write12,OperationCode=8:0xAA,reserved=3:0x0,DPO=1:std,FUA=1:std,reserved=1:0x0,FUA_NV=1:std,reserved=1:0x0,LogicalBlockAddress=32:std,TransferLength=32:std,reserved=3:0x0,GroupNumber=5:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String WRITE_16 =
         "Write16,OperationCode=8:0x8A,reserved=3:0x0,DPO=1:std,FUA=1:std,reserved=1:0x0,FUA_NV=1:std,reserved=1:0x0,LogicalBlockAddress=64:std,TransferLength=32:std,reserved=3:0x0,GroupNumber=5:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      serializer = new CDBFactory();
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

   private void runTest(String specification)
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
   public void parseInquiry()
   {
      runTest(INQUIRY);
   }

   @Test
   public void parseModeSelect6()
   {
      runTest(MODE_SELECT_6);
   }

   @Test
   public void parseModeSelect10()
   {
      runTest(MODE_SELECT_10);
   }

   @Test
   public void parseModeSense6()
   {
      runTest(MODE_SENSE_6);
   }

   @Test
   public void parseModeSense10()
   {
      runTest(MODE_SENSE_10);
   }

   @Test
   public void parseRead6()
   {
      runTest(READ_6);
   }

   @Test
   public void parseRead10()
   {
      runTest(READ_10);
   }

   @Test
   public void parseRead12()
   {
      runTest(READ_12);
   }

   @Test
   public void parseRead16()
   {
      runTest(READ_16);
   }

   @Test
   public void parseReadCapacity10()
   {
      runTest(READ_CAPACITY_10);
   }

   @Test
   public void parseReadCapacity16()
   {
      runTest(READ_CAPACITY_16);
   }

   @Test
   public void parseRemoveDiagnosticResults()
   {
      runTest(REMOVE_DIAGNOSTIC_RESULTS);
   }

   @Test
   public void parseReportLuns()
   {
      runTest(REPORT_LUNS);
   }

   @Test
   public void parseReportSupportedTaskManagementFunctions()
   {
      runTest(REPORT_SUPPORTED_TASK_MANAGEMENT_FUNCTIONS);
   }

   @Test
   public void parseRequestSense()
   {
      runTest(REQUEST_SENSE);
   }

   @Test
   public void parseSendDiagnostic()
   {
      runTest(SEND_DIAGNOSTIC);
   }

   @Test
   public void parseTestUnitReady()
   {
      runTest(TEST_UNIT_READY);
   }

   @Test
   public void parseWrite6()
   {
      runTest(WRITE_6);
   }

   @Test
   public void parseWrite10()
   {
      runTest(WRITE_10);
   }

   @Test
   public void parseWrite12()
   {
      runTest(WRITE_12);
   }

   @Test
   public void parseWrite16()
   {
      runTest(WRITE_16);
   }
}
