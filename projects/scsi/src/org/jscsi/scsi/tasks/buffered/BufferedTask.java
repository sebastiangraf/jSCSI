package org.jscsi.scsi.tasks.buffered;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.tasks.AbstractTask;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.transport.TargetTransportPort;

public abstract class BufferedTask extends AbstractTask
{
   private static Logger _logger = Logger.getLogger(BufferedTask.class);

   protected int blockSize;
   protected ByteBuffer store;

   
   ////////////////////////////////////////////////////////////////////////////
   // constructor(s)
   
   
   public BufferedTask()
   {
      super();
   }

   public BufferedTask(ByteBuffer store,
                     int blockSize,
                     TargetTransportPort targetPort,
                     Command command,
                     ModePageRegistry modePageRegistry,
                     InquiryDataRegistry inquiryDataRegistry)
   {
      super(targetPort, command, modePageRegistry, inquiryDataRegistry);
      this.store = store;
      this.blockSize = blockSize;
   }

   
   /////////////////////////////////////////////////////////////////////////////
   // abstract members
   
   
   /**
    * Executes the task operation.
    */
   protected abstract void execute(ByteBuffer file,
                                   int blockLength,
                                   TargetTransportPort targetPort,
                                   Command command,
                                   ModePageRegistry modePageRegistry,
                                   InquiryDataRegistry inquiryDataRegistry)
   throws InterruptedException, SenseException;

   @Override
   protected final void execute(TargetTransportPort targetPort,
                                Command command,
                                ModePageRegistry modePageRegistry,
                                InquiryDataRegistry inquiryDataRegistry)
   throws InterruptedException, SenseException
   {
      this.execute(this.store, this.blockSize, targetPort, command, modePageRegistry, inquiryDataRegistry);
   }

   protected final Task load(ByteBuffer store,
                             int blockLength,
                             TargetTransportPort targetPort,
                             Command command,
                             ModePageRegistry modePageRegistry,
                             InquiryDataRegistry inquiryDataRegistry)
   {
      this.store = store;
      this.blockSize = blockLength;
      super.load(targetPort, command, modePageRegistry, inquiryDataRegistry);
      return this;
   }

   protected long getDeviceCapacity()
   {
      if (this.store.limit() % blockSize != 0)
         throw new RuntimeException("invalid file length; not mulitple of block size");
      return this.store.limit() / this.blockSize;
   }
}
