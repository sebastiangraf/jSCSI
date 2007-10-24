
package org.jscsi.scsi.tasks.file;

import org.jscsi.scsi.protocol.Command;
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
   public FileTask(TargetTransportPort targetPort, Command command, FileDevice device)
   {
      super(targetPort, command);
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
}
