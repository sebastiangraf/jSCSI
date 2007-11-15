package org.jscsi.scsi.lu;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.Read6;
import org.jscsi.scsi.protocol.cdb.Write6;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.inquiry.StaticInquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.mode.StaticModePageRegistry;
import org.jscsi.scsi.protocol.sense.SenseData;
import org.jscsi.scsi.protocol.sense.SenseDataFactory;
import org.jscsi.scsi.target.Target;
import org.jscsi.scsi.tasks.TaskAttribute;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.tasks.buffered.BufferedTaskFactory;
import org.jscsi.scsi.tasks.management.DefaultTaskManager;
import org.jscsi.scsi.tasks.management.DefaultTaskSet;
import org.jscsi.scsi.tasks.management.TaskManager;
import org.jscsi.scsi.tasks.management.TaskSet;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DefaultLogicalUnitTest extends AbstractLogicalUnit implements TargetTransportPort
{
   private static Logger _logger = Logger.getLogger(DefaultLogicalUnitTest.class);

   private static final int NUM_BLOCKS_TRANSMIT = 16;
   
   private static final int TASK_SET_QUEUE_DEPTH = 16;
   private static final int TASK_MGR_NUM_THREADS = 2;
   private static final int STORE_BLOCK_SIZE = 4096;
   // STORE_CAPACITY is representative of the number of blocks, thus:
   //   8192 * 4096B = 32MB
   private static final int STORE_CAPACITY = 8192;

   private static LogicalUnit lu;

   private static TaskSet taskSet;
   private static TaskManager taskManager;
   private static ModePageRegistry modeRegistry;
   private static InquiryDataRegistry inquiryRegistry;
   private static TaskFactory taskFactory;
   private static ByteBuffer store;
   
   private int cmdRef = 0;
   private Random rnd = new Random();
   private Nexus nexus = new Nexus("initiator", "target", 0, 0);
   private HashMap<Long,ByteBuffer> readDataMap = new HashMap<Long,ByteBuffer>();
   private HashMap<Long,ByteBuffer> writeDataMap = new HashMap<Long,ByteBuffer>();

   @BeforeClass
   public static void setUpBeforeClass() throws Exception
   {
      _logger.debug("initializing test");

      taskSet = new DefaultTaskSet(TASK_SET_QUEUE_DEPTH);
      taskManager = new DefaultTaskManager(TASK_MGR_NUM_THREADS, taskSet);
      modeRegistry = new StaticModePageRegistry();
      inquiryRegistry = new StaticInquiryDataRegistry();

      store = ByteBuffer.allocate(STORE_BLOCK_SIZE * STORE_CAPACITY);
      taskFactory = new BufferedTaskFactory(store, STORE_BLOCK_SIZE, modeRegistry, inquiryRegistry);

      lu = new DefaultLogicalUnitTest(taskSet, taskManager, taskFactory);
      _logger.debug("created logical unit: " + lu);
      
      lu.start();
      _logger.debug("logical unit successfully started");
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception
   {
      lu.stop();
      _logger.debug("exiting test");
   }

   @Before
   public void setUp() throws Exception
   {
   }

   @After
   public void tearDown() throws Exception
   {
   }

   /////////////////////////////////////////////////////////////////////////////
   //

   @Test
   public void TestRead6()
   {
      CDB cdb1 = new Write6(false, true, 0, NUM_BLOCKS_TRANSMIT);
      Command cmd1 = new Command(this.nexus, cdb1, TaskAttribute.SIMPLE, cmdRef, 0);
      this.createReadData(NUM_BLOCKS_TRANSMIT * STORE_BLOCK_SIZE, cmdRef);
      lu.enqueue(this, cmd1);
      cmdRef++;
      
      //try {Thread.sleep(1000);} catch (InterruptedException e){}
      
      CDB cdb2 = new Read6(false, true, 0, NUM_BLOCKS_TRANSMIT);
      Command cmd2 = new Command(this.nexus, cdb2, TaskAttribute.SIMPLE, cmdRef, 0);
      lu.enqueue(this, cmd2);
      
      try {Thread.sleep(1000);} catch (InterruptedException e){}
      
      _logger.debug("readDataMap length: " + readDataMap.size());
      _logger.debug("writeDataMap length: " + writeDataMap.size());
      Assert.assertEquals("inconsistent read/write comparison", Arrays.equals(this.readDataMap.get(cmdRef-1).array(), this.writeDataMap.get(cmdRef).array()));
      
      
   }

   /////////////////////////////////////////////////////////////////////////////
   // constructor(s)

   public DefaultLogicalUnitTest()
   {

   }

   public DefaultLogicalUnitTest(
         TaskSet taskSet,
         TaskManager taskManager,
         TaskFactory taskFactory)
   {
      super(taskSet, taskManager, taskFactory);
   }

   /////////////////////////////////////////////////////////////////////////////
   // TargetTransportPort implementation


   public boolean readData(Nexus nexus, long cmdRef, ByteBuffer output)
         throws InterruptedException
   {
      _logger.debug("servicing readData request: nexus: " + nexus + ", cmdRef: " + cmdRef);
      _logger.debug(" ----- output buffer size: " + output.limit());
      _logger.debug(" ----- readData buffer size: " + this.readDataMap.get(cmdRef));
      output.put(this.readDataMap.get(cmdRef));
      return true;
   }

   public void registerTarget(Target target)
   {
      _logger.debug("servicing registerTarget request");
   }

   public void removeTarget(String targetName) throws Exception
   {
      _logger.debug("servicing removeTarget request");
   }

   public void terminateDataTransfer(Nexus nexus, long commandReferenceNumber)
   {
      _logger.debug("servicing terminateDataTransfer request");
   }

   public boolean writeData(Nexus nexus, long cmdRef, ByteBuffer input)
         throws InterruptedException
   {
      _logger.debug("servicing writeData request: nexus: " + nexus + ", cmdRef: " + cmdRef);
      this.writeDataMap.put(cmdRef, input);
      return true;
   }

   public void writeResponse(
         Nexus nexus,
         long commandReferenceNumber,
         Status status,
         ByteBuffer senseData)
   {
      _logger.debug("servicing writeResponse request: nexus: " + nexus + ", cmdRef: " + commandReferenceNumber);
      _logger.debug("response was status: " + status);
      if (status.equals(Status.CHECK_CONDITION))
      {
         SenseData sense = null;
         try
         {
            sense = new SenseDataFactory().decode(senseData);
         }
         catch (IOException e)
         {
            _logger.warn("I/O exception while decoding sense data");
         }
         _logger.error("sense data: " + sense);
      }
   }
   
   @Override
   public String toString()
   {
      return "<DummyTargetTransportPort>";
   }

   /////////////////////////////////////////////////////////////////////////////
   // utilities
   
   public ByteBuffer createReadData(int size, long cmdRef)
   {
      byte[] data = new byte[size];
      this.rnd.nextBytes(data);
      
      ByteBuffer buffData = ByteBuffer.allocate(size);
      buffData.put(data);
      
      this.readDataMap.put(cmdRef, buffData);
      return buffData;
   }
}
