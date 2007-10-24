
package org.jscsi.scsi.tasks;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.transport.TargetTransportPort;

// TODO: Describe class or interface
public interface TaskFactory
{

   Task getInstance(
         TargetTransportPort port,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry) throws IllegalRequestException;

}
