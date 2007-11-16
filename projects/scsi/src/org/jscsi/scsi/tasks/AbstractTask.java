
package org.jscsi.scsi.tasks;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.ParameterCDB;
import org.jscsi.scsi.protocol.cdb.TransferCDB;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.protocol.sense.exceptions.SynchronousDataTransferErrorException;
import org.jscsi.scsi.transport.TargetTransportPort;

// TODO: Describe class or interface
public abstract class AbstractTask implements Task
{
   private TargetTransportPort targetTransportPort;
   private Command command;
   private ModePageRegistry modePageRegistry;
   private InquiryDataRegistry inquiryDataRegistry;
   private String name = "DefaultTaskName";

   private Thread thread = null;

   /**
    * Abort variable specifies whether task can be aborted or is currently aborted.
    * <p>
    * <ul>
    * <li>If abort is true, task is already aborted or can no longer be aborted.</li>
    * <li>If abort is false, task is not aborted and can be aborted.</li>
    * </ul>
    * <p>
    * The {@link #abort()} method will fail if the this is true. The {@link #run()} method will not
    * enter the {@link #writeResponse(Status, ByteBuffer)} phase. However, abort is not polled.
    * Instead, we check {@link Thread#isInterrupted()}.
    */
   private final AtomicBoolean abort = new AtomicBoolean(false);

   /////////////////////////////////////////////////////////////////////////////
   // abstract methods

   protected abstract void execute() throws InterruptedException, SenseException;

   /////////////////////////////////////////////////////////////////////////////
   // constructors

   protected AbstractTask()
   {
   }

   protected AbstractTask(
         String name,
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      this.name = name;
      this.targetTransportPort = targetPort;
      this.command = command;
      this.modePageRegistry = modePageRegistry;
      this.inquiryDataRegistry = inquiryDataRegistry;
   }

   /////////////////////////////////////////////////////////////////////////////
   // operations

   protected final Task load(
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      this.command = command;
      this.targetTransportPort = targetPort;
      this.modePageRegistry = modePageRegistry;
      this.inquiryDataRegistry = inquiryDataRegistry;
      return this;
   }

   public final boolean abort()
   {
      if (abort.compareAndSet(false, true))
      {
         // If abort is false the task can be aborted because it is neither
         // already aborted nor in the writeResponse() phase. Abort is now set as true.

         // We interrupt the thread executing the task and terminate any outstanding
         // data. With luck, writeData() and readData() methods will not begin if
         // the thread is interrupted. If interrupt() and terminateDataTransfer()
         // occur between the interrupt check and the transfer call to the transport layer
         // the transport layer will receive a request for an already terminated nexus.
         // The transport port interface guarantees that InterruptedException will always be
         // thrown from the transfer methods in this case.
         this.thread.interrupt();
         this.targetTransportPort.terminateDataTransfer(this.command.getNexus(),
               this.command.getCommandReferenceNumber());

         return true;
      }
      else
      {
         // If abort is true the task is already aborted or is in the writeResponse() phase. We
         // can no longer abort and the abort value remains set as true.
         return false;
      }
   }

   public final void run()
   {
      this.thread = Thread.currentThread();
      try
      {
         this.execute();
      }
      catch (SenseException e)
      {
         // Write response with a CHECK CONDITION status.
         this.targetTransportPort.writeResponse(this.command.getNexus(),
               this.command.getCommandReferenceNumber(), Status.CHECK_CONDITION,
               ByteBuffer.wrap(e.encode()));
      }
      catch (InterruptedException e)
      {
         // Task was aborted, don't do anything
      }
   }

   protected final boolean readData(ByteBuffer output) throws InterruptedException,
         SynchronousDataTransferErrorException
   {
      if (Thread.interrupted())
         throw new InterruptedException();

      return this.targetTransportPort.readData(this.command.getNexus(),
            this.command.getCommandReferenceNumber(), output);
   }

   protected final boolean writeData(ByteBuffer input) throws InterruptedException,
         SynchronousDataTransferErrorException
   {
      if (Thread.interrupted())
         throw new InterruptedException();

      return this.targetTransportPort.writeData(this.command.getNexus(),
            this.command.getCommandReferenceNumber(), input);
   }

   protected final boolean writeData(byte[] input) throws InterruptedException,
         SynchronousDataTransferErrorException
   {
      if (Thread.interrupted())
         throw new InterruptedException();

      // check how much data may be returned
      CDB cdb = this.command.getCommandDescriptorBlock();
      long transferLength = 0;
      if (cdb instanceof TransferCDB)
      {
         transferLength = ((TransferCDB) cdb).getTransferLength();
      }
      else if (cdb instanceof ParameterCDB)
      {
         transferLength = ((ParameterCDB) cdb).getAllocationLength();
      }
      // If the CDB is not a transfer or parameter CDB no data should be transfered

      // We allocate a byte buffer of transfer length and write either all input data
      // or up to the transfer length in input data.
      ByteBuffer data = ByteBuffer.allocate((int) transferLength);
      data.put(input, 0, (int) Math.min(transferLength, input.length));
      data.rewind();

      if (Thread.interrupted())
         throw new InterruptedException();

      return this.targetTransportPort.writeData(this.command.getNexus(),
            this.command.getCommandReferenceNumber(), data);
   }

   protected final void writeResponse(Status status, ByteBuffer senseData)
   {
      if (abort.compareAndSet(false, true))
      {
         // If abort is false the task can enter the writeResponse() phase. Abort is now
         // set as true to indicate that abort() can no longer succeed.
         this.getTargetTransportPort().writeResponse(command.getNexus(),
               command.getCommandReferenceNumber(), status, senseData);
      }
      else
      {
         // If abort is true the task has been aborted and no data shall be written to
         // the target transport port. Abort remains set as true.
         return;
      }
   }

   public final Command getCommand()
   {
      return this.command;
   }

   public final InquiryDataRegistry getInquiryDataRegistry()
   {
      return this.inquiryDataRegistry;
   }

   public final ModePageRegistry getModePageRegistry()
   {
      return this.modePageRegistry;
   }

   public final TargetTransportPort getTargetTransportPort()
   {
      return this.targetTransportPort;
   }

   public final String getName()
   {
      return this.name;
   }

   public void setName(final String name)
   {
      this.name = name;
   }

   @Override
   public String toString()
   {
      return "<Task name: " + this.getName() + ", command: " + this.command + ", target-port: "
            + this.targetTransportPort + ">";
   }
}
