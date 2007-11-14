
package org.jscsi.scsi.lu;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.inquiry.StaticInquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.mode.StaticModePageRegistry;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.tasks.buffered.BufferedTaskFactory;
import org.jscsi.scsi.tasks.management.DefaultTaskManager;
import org.jscsi.scsi.tasks.management.DefaultTaskSet;
import org.jscsi.scsi.tasks.management.TaskManager;
import org.jscsi.scsi.tasks.management.TaskSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DefaultLogicalUnitTest extends AbstractLogicalUnit
{
   private static Logger _logger = Logger.getLogger(DefaultLogicalUnitTest.class);

   private static final int TASK_SET_QUEUE_DEPTH = 1;
   private static final int TASK_MGR_NUM_THREADS = 1;
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

      lu = new DefaultLogicalUnitTest(taskSet, taskManager, modeRegistry, inquiryRegistry);
      _logger.debug("created logical unit: " + lu);
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

   /////////////////////////////////////////////////////////////////////////////
   //

   @Test
   public void Test()
   {

   }

   /////////////////////////////////////////////////////////////////////////////
   // constructor(s)

   public DefaultLogicalUnitTest()
   {

   }

   public DefaultLogicalUnitTest(
         TaskSet taskSet,
         TaskManager taskManager,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      super(taskSet, taskManager, taskFactory);
   }

   /////////////////////////////////////////////////////////////////////////////
   // LogicalUnit implementation

}
