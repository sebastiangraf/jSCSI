package org.jscsi.scsi.tasks;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.transport.TargetTransportPort;

public interface TaskFactory
{
   Task getInstance(TargetTransportPort port, Command command) throws IllegalRequestException;
}
