package org.jscsi.scsi.tasks;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.transport.TargetTransportPort;

/**
 * A implementation specific factory which creates tasks capable of executing SCSI commands
 * on a particular logical unit.
 */
public interface TaskFactory
{
   /**
    * Returns a task which can execute the indicated command.
    * 
    * @param port The transport port where the command originated.
    * @param command The incoming command.
    * @return A task object which can execute the indicated command.
    * @throws IllegalRequestException If there is no task which can execute the indicated
    *    command. This means an illegal request has been sent from the initiator.
    */
   Task getInstance(TargetTransportPort port, Command command) throws IllegalRequestException;
}
