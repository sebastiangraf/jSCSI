
package org.jscsi.scsi.tasks.file;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.tasks.AbstractTask;
import org.jscsi.scsi.transport.TargetTransportPort;

public abstract class FileTask extends AbstractTask
{
   protected FileDevice _device;

   public FileTask()
   {
      super();
   }

   // Constructors
   public FileTask(
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry,
         FileDevice device)
   {
      super(targetPort, command, modePageRegistry, inquiryDataRegistry);
      this._device = device;
   }

   public FileDevice getFileDevice()
   {
      return this._device;
   }

   void setFileDevice(FileDevice device)
   {
      this._device = device;
   }

   // protected setters to be used by FileTaskFactory
   protected void setTargetTransportPort(TargetTransportPort targetPort)
   {
      super.setTargetTransportPort(targetPort);
   }

   protected void setCommand(Command command)
   {
      super.setCommand(command);
   }

   protected void setModePageRegistry(ModePageRegistry modePageRegistry)
   {
      super.setModePageRegistry(modePageRegistry);
   }

   protected void setInquiryDataRegistry(InquiryDataRegistry inquiryDataRegistry)
   {
      super.setInquiryDataRegistry(inquiryDataRegistry);
   }
}
