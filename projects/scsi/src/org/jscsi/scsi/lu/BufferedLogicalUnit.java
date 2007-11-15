
package org.jscsi.scsi.lu;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.inquiry.StaticInquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.StaticModePageRegistry;
import org.jscsi.scsi.tasks.buffered.BufferedTaskFactory;
import org.jscsi.scsi.tasks.management.DefaultTaskManager;
import org.jscsi.scsi.tasks.management.DefaultTaskSet;
import org.jscsi.scsi.tasks.management.TaskSet;

// TODO: Describe class or interface
public class BufferedLogicalUnit extends AbstractLogicalUnit
{
   private static Logger _logger = Logger.getLogger(TaskSet.class);

   private final int NUM_TASK_THREADS = 10;
   private final int SET_QUEUE_DEPTH = 10;

   public BufferedLogicalUnit(ByteBuffer store, int blockSize)
   {
      super();
      TaskSet taskSet = new DefaultTaskSet(SET_QUEUE_DEPTH);
      this.setTaskSet(taskSet);
      this.setTaskManager(new DefaultTaskManager(NUM_TASK_THREADS, taskSet));

      this.setTaskFactory(new BufferedTaskFactory(store, blockSize, new StaticModePageRegistry(),
            new StaticInquiryDataRegistry()));
   }
}
