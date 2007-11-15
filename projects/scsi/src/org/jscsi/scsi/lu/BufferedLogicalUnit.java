package org.jscsi.scsi.lu;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.inquiry.StaticInquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.mode.StaticModePageRegistry;
import org.jscsi.scsi.tasks.buffered.BufferedTaskFactory;
import org.jscsi.scsi.tasks.management.DefaultTaskManager;
import org.jscsi.scsi.tasks.management.DefaultTaskSet;
import org.jscsi.scsi.tasks.management.TaskManager;
import org.jscsi.scsi.tasks.management.TaskSet;

// TODO: Describe class or interface
public class BufferedLogicalUnit extends AbstractLogicalUnit
{   
   private static Logger _logger = Logger.getLogger(TaskSet.class);

   private final int NUM_TASK_THREADS = 10;
   private final int SET_QUEUE_DEPTH = 10;
   
   private final TaskSet taskSet = new DefaultTaskSet(SET_QUEUE_DEPTH);
   private final TaskManager taskManager = new DefaultTaskManager(NUM_TASK_THREADS, this.taskSet);
   private final InquiryDataRegistry inquiryRegistry = new StaticInquiryDataRegistry();
   private final ModePageRegistry modePageRegistry = new StaticModePageRegistry();

   public BufferedLogicalUnit(ByteBuffer store, int blockSize)
   {
      super();
      this.setTaskSet(taskSet);
      this.setTaskManager(taskManager);
      
      
      this.setTaskFactory(new BufferedTaskFactory(
            store,
            blockSize,
            this.modePageRegistry,
            this.inquiryRegistry ));
   }
}
