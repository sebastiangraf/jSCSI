
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
   
   private static String INQUIRY_SPEC =
      "Inquiry,OperationCode=8:0x12,reserved=7:0x0,EVPD=1:std,PageCode=8:std,AllocationLength=16:std,reserved=5:random,NormalACA=1:std,reserved=1:0x00,Linked=1:std";
   private static String MODE_SELECT_6 =
      "ModeSelect6,OperationCode=8:0x15,reserved=3:0x0,PageFormat=1:std,reserved=3:0x0,SavePages=1:std,reserved=16:0x0,ParameterListLength=8:std,reserved=5:random,NormalACA=1:std,reserved=1:0x00,Linked=1:std";
   private static String MODE_SELECT_10 =
      "ModeSelect10,OperationCode=8:0x55,reserved=3:0x0,PageFormat=1:std,reserved=3:0x0,SavePages=1:std,reserved=40:0x0,ParameterListLength=16:std,reserved=5:random,NormalACA=1:std,reserved=1:0x00,Linked=1:std";
   private static String MODE_SENSE_6 =
      "ModeSense6,OperationCode=8:0x1A,reserved=4:0x0,Dbd=1:std,reserved=3:0x0,PageControl=2:std,PageCode=6:std,SubPageCode=8:std,AllocationLength=8:std,reserved=5:random,NormalACA=1:std,reserved=1:0x00,Linked=1:std";
   private static String MODE_SENSE_10 =
      "ModeSense10,OperationCode=8:0x5A,reserved=3:0x0,LLBAA=1:std,Dbd=1:std,reserved=3:0x0,PageControl=2:std,PageCode=6:std,SubPageCode=8:std,reserved=24:0x0,AllocationLength=16:std,reserved=5:random,NormalACA=1:std,reserved=1:0x00,Linked=1:std";
   
      
      
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
   public void parseInquiry()
   {
      runTest(INQUIRY_SPEC);
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
   
   

}


