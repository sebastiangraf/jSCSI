package org.jscsi.scsi.tasks.file;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.transport.TargetTransportPort;

public abstract class FileTask implements Task
{
   // Data   
   protected TargetTransportPort _targetPort;
   protected Command _command;

   protected FileDevice _device;

   public FileTask()
   {
   }

   // Constructors
   public FileTask(TargetTransportPort targetPort, FileDevice device)
   {
      this._targetPort = targetPort;
      this._device = device;
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

   public FileDevice getFileDevice()
   {
      return this._device;
   }

   // package protected setters to be used by GridTaskFactory
   void setTargetTransportPort(TargetTransportPort targetPort)
   {
      this._targetPort = targetPort;
   }

   void setFileDevice(FileDevice device)
   {
      this._device = device;
   }

   void setCommand(Command command)
   {
      this._command = command;
   }
}
