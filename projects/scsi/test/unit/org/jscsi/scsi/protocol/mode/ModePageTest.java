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
// Date: Nov 15, 2007
//---------------------

package org.jscsi.scsi.protocol.mode;

import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Serializer;
import org.jscsi.scsi.protocol.SerializerTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

// TODO: Describe class or interface
public class ModePageTest
{
   private static Logger _logger = Logger.getLogger(ModePageTest.class);

   private static final String DEFAULT_PACKAGE = "org.jscsi.scsi.protocol.mode";
   
   private static Serializer serializer;
   
   private static String CONTROL_A = "Control," +
      "ParametersSavable=1:std," +
      "SubPageFormat=1:0b0," +
      "PageCode=6:0x0A," +
      "PageLength=8:0x0A," +
      "TST=3:std," +
      "TMF_ONLY=1:std," +
      "reserved=1:0x00," +
      "D_SENSE=1:std," +
      "GLTSD=1:std," +
      "RLEC=1:std," +
      "QueueAlgorithmModifier=4:0x00," +
      "reserved=1:0x00," +
      "QERR=2:0x00," +
      "reserved=1:0x00," +
      "reserved=1:0x00," +
      "RAC=1:0x00," +
      "UA_INTLCK_CTRL=2:0x00," +
      "SWP=1:0x00," +
      "reserved=3:0x00," +
      "ATO=1:0x00," +
      "TAS=1:0x00," +
      "reserved=3:0x00," +
      "AutoloadMode=3:0x00," +
      "reserved=16:0x00," +
      "BusyTimeoutPeriod=16:0x00," +
      "ExtendedSelfTestCompletionTime=16:0x00";
   private static String CONTROL_B = "Control," +
   "ParametersSavable=1:0x00," +
   "SubPageFormat=1:0b0," +
   "PageCode=6:0x0A," +
   "PageLength=8:0x0A," +
   "TST=3:0x00," +
   "TMF_ONLY=1:0x00," +
   "reserved=1:0x00," +
   "D_SENSE=1:0x00," +
   "GLTSD=1:0x00," +
   "RLEC=1:0x00," +
   "QueueAlgorithmModifier=4:std," +
   "reserved=1:0x00," +
   "QERR=2:std," +
   "reserved=1:0x00," +
   "reserved=1:0x00," +
   "RAC=1:std," +
   "UA_INTLCK_CTRL=2:std," +
   "SWP=1:std," +
   "reserved=3:0x00," +
   "ATO=1:0x00," +
   "TAS=1:0x00," +
   "reserved=3:0x00," +
   "AutoloadMode=3:0x00," +
   "reserved=16:0x00," +
   "BusyTimeoutPeriod=16:0x00," +
   "ExtendedSelfTestCompletionTime=16:0x00";
   private static String CONTROL_C = "Control," +
   "ParametersSavable=1:0x00," +
   "SubPageFormat=1:0b0," +
   "PageCode=6:0x0A," +
   "PageLength=8:0x0A," +
   "TST=3:0x00," +
   "TMF_ONLY=1:0x00," +
   "reserved=1:0x00," +
   "D_SENSE=1:0x00," +
   "GLTSD=1:0x00," +
   "RLEC=1:0x00," +
   "QueueAlgorithmModifier=4:std," +
   "reserved=1:0x00," +
   "QERR=2:std," +
   "reserved=1:0x00," +
   "reserved=1:0x00," +
   "RAC=1:0x00," +
   "UA_INTLCK_CTRL=2:0x00," +
   "SWP=1:0x00," +
   "reserved=3:0x00," +
   "ATO=1:std," +
   "TAS=1:std," +
   "reserved=3:0x00," +
   "AutoloadMode=3:std," +
   "reserved=16:0x00," +
   "BusyTimeoutPeriod=16:std," +
   "ExtendedSelfTestCompletionTime=16:std";

   private static String CONTROL_EXTENSION = "ControlExtension," +
      "ParametersSavable=1:std," +
      "SubPageFormat=1:0b01," +
      "PageCode=6:0x0A," +
      "SubPageCode=8:0x01," +
      "PageLength=16:0x1C," +
      "reserved=5:0x00," +
      "TCMOS=1:std," +
      "SCSIP=1:std," +
      "IALUAE=1:std," +
      "reserved=4:0x00," +
      "InitialPriority=4:0x00," +
      "reserved=32:0x00," +
      "reserved=32:0x00," +
      "reserved=32:0x00," +
      "reserved=32:0x00," +
      "reserved=32:0x00," +
      "reserved=32:0x00," +
      "reserved=16:0x00";
   
   private static String INFORMATIONAL_EXCEPTIONS_CONTROL_A = "InformationalExceptionsControl," +
      "ParametersSavable=1:std," +
      "SubPageFormat=1:0b00," +
      "PageCode=6:0x1C," +
      "PageLength=8:0x0A," +
      "PERF=1:std," +
      "reserved=1:0x00," +
      "EBF=1:std," +
      "EWASC=1:std," +
      "DEXCPT=1:std," +
      "TEST=1:std," +
      "reserved=1:0x00," +
      "LOGERR=1:std," +
      "reserved=4:0x00," +
      "MRIE=4:std," +
      "IntervalTimer=32:0x00," +
      "ReportCount=32:0x00";   
   private static String INFORMATIONAL_EXCEPTIONS_CONTROL_B = "InformationalExceptionsControl," +
      "ParametersSavable=1:0x00," +
      "SubPageFormat=1:0b00," +
      "PageCode=6:0x1C," +
      "PageLength=8:0x0A," +
      "PERF=1:0x00," +
      "reserved=1:0x00," +
      "EBF=1:0x00," +
      "EWASC=1:0x00," +
      "DEXCPT=1:0x00," +
      "TEST=1:0x00," +
      "reserved=1:0x00," +
      "LOGERR=1:0x00," +
      "reserved=4:0x00," +
      "MRIE=4:0x00," +
      "IntervalTimer=32:std," +
      "ReportCount=32:std";
   
   private static String CACHING_A = "Caching," +
   "ParametersSavable=1:0x00," +
   "SubPageFormat=1:0b00," +
   "PageCode=6:0x08," +
   "PageLength=8:0x12," +
   "IC=1:std," +
   "ABPF=1:std," +
   "CAP=1:std," +
   "DISC=1:std," +
   "SIZE=1:std," +
   "WCE=1:std," +
   "MF=1:std," +
   "RCD=1:std," +
   "DemandReadRetentionPriority=4:0x00," +
   "WriteRetentionPriority=4:0x00," +
   "DisablePrefetchTransferLength=16:0x00," +
   "MinimumPrefetch=16:0x00," +
   "MaximumPrefetch=16:0x00," +
   "MaximumPrefetchCeiling=16:0x00," +
   "FSW=1:0x00," +
   "LBCSS=1:0x00," +
   "DRA=1:0x00," +
   "reserved=4:0x00," +
   "NV_DIS=1:0x00," +
   "NumberOfCacheSegments=8:0x00," +
   "CacheSegmentSize=16:0x00," +
   "reserved=32:0x00";
   private static String CACHING_B = "Caching," +
   "ParametersSavable=1:0x00," +
   "SubPageFormat=1:0b00," +
   "PageCode=6:0x08," +
   "PageLength=8:0x12," +
   "IC=1:0x00," +
   "ABPF=1:0x00," +
   "CAP=1:0x00," +
   "DISC=1:0x00," +
   "SIZE=1:0x00," +
   "WCE=1:0x00," +
   "MF=1:0x00," +
   "RCD=1:0x00," +
   "DemandReadRetentionPriority=4:std," +
   "WriteRetentionPriority=4:std," +
   "DisablePrefetchTransferLength=16:std," +
   "MinimumPrefetch=16:std," +
   "MaximumPrefetch=16:std," +
   "MaximumPrefetchCeiling=16:std," +
   "FSW=1:0x00," +
   "LBCSS=1:0x00," +
   "DRA=1:0x00," +
   "reserved=4:0x00," +
   "NV_DIS=1:0x00," +
   "NumberOfCacheSegments=8:0x00," +
   "CacheSegmentSize=16:0x00," +
   "reserved=32:0x00";
   private static String CACHING_C = "Caching," +
   "ParametersSavable=1:0x00," +
   "SubPageFormat=1:0b00," +
   "PageCode=6:0x08," +
   "PageLength=8:0x12," +
   "IC=1:0x00," +
   "ABPF=1:0x00," +
   "CAP=1:0x00," +
   "DISC=1:0x00," +
   "SIZE=1:0x00," +
   "WCE=1:0x00," +
   "MF=1:0x00," +
   "RCD=1:0x00," +
   "DemandReadRetentionPriority=4:0x00," +
   "WriteRetentionPriority=4:0x00," +
   "DisablePrefetchTransferLength=16:0x00," +
   "MinimumPrefetch=16:0x00," +
   "MaximumPrefetch=16:0x00," +
   "MaximumPrefetchCeiling=16:0x00," +
   "FSW=1:std," +
   "LBCSS=1:std," +
   "DRA=1:std," +
   "reserved=4:std," +
   "NV_DIS=1:std," +
   "NumberOfCacheSegments=8:std," +
   "CacheSegmentSize=16:std," +
   "reserved=32:0x00";
   
   private static String READ_WRITE_ERROR_RECOVERY = "ReadWriteErrorRecovery," +
      "ParametersSavable=1:std," +
      "SubPageFormat=1:0b0," +
      "PageCode=6:0x01," +
      "PageLength=8:0x0A," +
      "AWRE=1:std," +
      "ARRE=1:std," +
      "TB=1:std," +
      "RC=1:std," +
      "EER=1:std," +
      "PER=1:std," +
      "DTE=1:std," +
      "DCR=1:std," +
      "ReadRetryCount=8:std," +
      "reserved=32:0x00," +
      "WriteRetryCount=8:std," +
      "reserved=8:0x00," +
      "RecoveryTimeLimit=16:std";
   
   
   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      serializer = new StaticModePageRegistry();
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
   public void parseControl()
   {
      runTest(CONTROL_A);
      runTest(CONTROL_B);
      runTest(CONTROL_C);
   }
   
   @Test
   public void parseControlExtension()
   {
      runTest(CONTROL_EXTENSION);
   }
   
   @Test
   public void parseInformationalExceptionsControl()
   {
      runTest(INFORMATIONAL_EXCEPTIONS_CONTROL_A);
      runTest(INFORMATIONAL_EXCEPTIONS_CONTROL_B);
   }
   
   @Test
   public void parseCaching()
   {
      runTest(CACHING_A);
      runTest(CACHING_B);
      runTest(CACHING_C);
   }
   
   @Test
   public void parseReadWriteErrorRecovery()
   {
      runTest(READ_WRITE_ERROR_RECOVERY);
   }

}


