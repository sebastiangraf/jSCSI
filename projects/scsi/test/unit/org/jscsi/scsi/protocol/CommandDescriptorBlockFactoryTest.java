
package org.jscsi.scsi.protocol;

import static org.junit.Assert.fail;

import org.jscsi.scsi.protocol.cdb.CommandDescriptorBlockFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

// TODO: Describe class or interface
public class CommandDescriptorBlockFactoryTest
{

   private static final String DEFAULT_PACKAGE = "org.jscsi.scsi.protocol.cdb";

   private static Serializer serializer;

   private static String INQUIRY =
         "Inquiry,OperationCode=8:0x12,reserved=7:0x0,EVPD=1:std,PageCode=8:std,AllocationLength=16:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String MODE_SELECT_6 =
         "ModeSelect6,OperationCode=8:0x15,reserved=3:0x0,PF=1:std,reserved=3:0x0,SP=1:std,reserved=16:0x0,ParameterListLength=8:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

   private static String MODE_SELECT_10 =
         "ModeSelect10,OperationCode=8:0x55,reserved=3:0x0,PF=1:std,reserved=3:0x0,SP=1:std,reserved=40:0x0,ParameterListLength=16:std,reserved=5:0x0,NormalACA=1:std,reserved=1:0x0,Linked=1:std";

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
      serializer = new CommandDescriptorBlockFactory();
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
