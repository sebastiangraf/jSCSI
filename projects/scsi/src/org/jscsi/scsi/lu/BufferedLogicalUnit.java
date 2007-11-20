
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

   public BufferedLogicalUnit(ByteBuffer store, int blockSize, int taskThreads, int queueDepth)
   {
      super();
      TaskSet taskSet = new DefaultTaskSet(queueDepth);
      this.setTaskSet(taskSet);
      this.setTaskManager(new DefaultTaskManager(taskThreads, taskSet));

      this.setTaskFactory(new BufferedTaskFactory(store, blockSize, new StaticModePageRegistry(),
            new StaticInquiryDataRegistry()));
   }
}
