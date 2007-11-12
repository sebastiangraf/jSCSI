
package org.jscsi.scsi.tasks.buffered;

import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.tasks.AbstractTask;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.transport.TargetTransportPort;

public abstract class BufferedTask extends AbstractTask
{
   protected ByteBuffer file;
   protected int blockLength;

   public BufferedTask()
   {
      super();
   }

   public BufferedTask(
         ByteBuffer file,
         int blockLength,
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      super(targetPort, command, modePageRegistry, inquiryDataRegistry);
      this.file = file;
      this.blockLength = blockLength;
   }

   /**
    * Executes the task operation.
    */
   protected abstract void execute(
         ByteBuffer file,
         int blockLength,
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry) throws InterruptedException, SenseException;

   @Override
   protected final void execute(
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry) throws InterruptedException, SenseException
   {
      this.execute(file, blockLength, targetPort, command, modePageRegistry, inquiryDataRegistry);
   }

   protected final Task load(
         ByteBuffer file,
         int blockLength,
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      this.file = file;
      this.blockLength = blockLength;
      super.load(targetPort, command, modePageRegistry, inquiryDataRegistry);
      return this;
   }

   protected final long getFileCapacity()
   {
      if (file.limit() % blockLength != 0)
         throw new RuntimeException("invalid file length; not mulitple of block size");
      return file.limit() / blockLength;
   }
}
