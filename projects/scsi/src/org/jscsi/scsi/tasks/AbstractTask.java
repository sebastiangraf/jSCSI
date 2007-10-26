package org.jscsi.scsi.tasks;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.transport.TargetTransportPort;

// TODO: Describe class or interface
public abstract class AbstractTask implements Task
{
   // Data   
   private TargetTransportPort _targetPort;
   private Command _command;
   private ModePageRegistry _modePageRegistry;
   private InquiryDataRegistry _inquiryDataRegistry;

   protected AbstractTask()
   {
   }

   // Constructors
   protected AbstractTask(
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      this._targetPort = targetPort;
      this._command = command;
      this._modePageRegistry = modePageRegistry;
      this._inquiryDataRegistry = inquiryDataRegistry;
   }

   // Methods common to all GridTasks
   public Command getCommand()
   {
      return this._command;
   }

   public TargetTransportPort getTargetTransportPort()
   {
      return this._targetPort;
   }

   public ModePageRegistry getModePageRegistry()
   {
      return this._modePageRegistry;
   }

   public InquiryDataRegistry getInquiryDataRegistry()
   {
      return this._inquiryDataRegistry;
   }

   // protected setters
   protected void setTargetTransportPort(TargetTransportPort targetPort)
   {
      this._targetPort = targetPort;
   }

   protected void setCommand(Command command)
   {
      this._command = command;
   }

   protected void setModePageRegistry(ModePageRegistry modePageRegistry)
   {
      this._modePageRegistry = modePageRegistry;
   }
   
   protected void setInquiryDataRegistry(InquiryDataRegistry inquiryDataRegistry)
   {
      this._inquiryDataRegistry = inquiryDataRegistry;
   }
}
