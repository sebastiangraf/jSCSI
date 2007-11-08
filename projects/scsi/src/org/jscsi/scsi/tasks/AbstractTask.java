package org.jscsi.scsi.tasks;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.ParameterCDB;
import org.jscsi.scsi.protocol.cdb.TransferCDB;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.transport.TargetTransportPort;

// TODO: Describe class or interface
public abstract class AbstractTask implements Task
{
   
   private TargetTransportPort targetPort;
   private Command command;
   private ModePageRegistry modePageRegistry;
   private InquiryDataRegistry inquiryDataRegistry;
   
   private Thread thread = null;
   
   /**
    * Abort variable specifies whether task can be aborted or is currently aborted.
    * <p>
    * <ul>
    *    <li>If abort is true, task is already aborted or can no longer be aborted.</li>
    *    <li>If abort is false, task is not aborted and can be aborted.</li>
    * </ul>
    * <p>
    * The {@link #abort()} method will fail if the this is true. The {@link #run()} method will
    * not enter the {@link #writeResponse(Status, ByteBuffer)} phase. However, abort is not polled.
    * Instead, we check {@link Thread#isInterrupted()}.
    */
   private final AtomicBoolean abort = new AtomicBoolean(false);
   
   
   protected abstract void execute(
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry) throws InterruptedException, SenseException;
         

   protected AbstractTask()
   {
   }

   protected AbstractTask(
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      this.targetPort = targetPort;
      this.command = command;
      this.modePageRegistry = modePageRegistry;
      this.inquiryDataRegistry = inquiryDataRegistry;
   }

   protected final Task load(
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      this.command = command;
      this.targetPort = targetPort;
      this.modePageRegistry = modePageRegistry;
      this.inquiryDataRegistry = inquiryDataRegistry;
      return this;
   }
   
   
   public final boolean abort()
   {
      if ( abort.compareAndSet(false, true) )
      {
         // If abort is false the task can be aborted because it is neither
         // already aborted nor in the writeResponse() phase. Abort is now set as true.
         
         // We interrupt the 
         this.thread.interrupt();
         this.targetPort.terminateDataTransfer(
               this.command.getNexus(),
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
         this.execute(this.targetPort, this.command, this.modePageRegistry, this.inquiryDataRegistry);
      }
      catch (SenseException e)
      {
         // Write response with a CHECK CONDITION status.
         this.targetPort.writeResponse(
               this.command.getNexus(),
               this.command.getCommandReferenceNumber(),
               Status.CHECK_CONDITION,
               ByteBuffer.wrap(e.encode()));
      }
      catch (InterruptedException e)
      {
         // Task was aborted, don't do anything
      }
   }
   
   
   protected final boolean readData(ByteBuffer output) throws InterruptedException
   {
      if (Thread.interrupted())
         throw new InterruptedException();
      
      return this.targetPort.readData(
            this.command.getNexus(),
            this.command.getCommandReferenceNumber(),
            output );
   }
   
   protected final boolean writeData(ByteBuffer input) throws InterruptedException
   {
      if (Thread.interrupted())
         throw new InterruptedException();
      
      return this.targetPort.writeData(
            this.command.getNexus(),
            this.command.getCommandReferenceNumber(),
            input );
   }
   
   protected final boolean writeData(byte[] input) throws InterruptedException
   {
      if (Thread.interrupted())
         throw new InterruptedException();
      
      // check how much data may be returned
      CDB cdb = this.command.getCommandDescriptorBlock();
      long transferLength = 0;
      if ( cdb instanceof TransferCDB )
      {
         transferLength = ((TransferCDB)cdb).getTransferLength();
      }
      else if ( cdb instanceof ParameterCDB )
      {
         transferLength = ((ParameterCDB)cdb).getAllocationLength();
      }
      // If the CDB is not a transfer or parameter CDB no data should be transfered
      
      // We allocate a byte buffer of transfer length and write either all input data
      // or up to the transfer length in input data.
      ByteBuffer data = ByteBuffer.allocate((int)transferLength);
      data.put(input, 0, (int)Math.max(transferLength, input.length));
      
      return this.targetPort.writeData(
            this.command.getNexus(),
            this.command.getCommandReferenceNumber(),
            data );
   }
   
   
   protected final void writeResponse(Status status, ByteBuffer senseData)
   {
      if ( abort.compareAndSet(false, true) )
      {
         // If abort is false the task can enter the writeResponse() phase. Abort is now
         // set as true to indicate that abort() can no longer succeed.
         this.getTargetTransportPort().writeResponse(
               command.getNexus(),
               command.getCommandReferenceNumber(),
               status,
               senseData );
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


   public final TargetTransportPort getTargetTransportPort()
   {
      return this.targetPort;
   }
   
   
   
}
