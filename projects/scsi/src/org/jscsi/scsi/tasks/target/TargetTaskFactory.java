package org.jscsi.scsi.tasks.target;

import org.apache.log4j.Logger;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.transport.TargetTransportPort;

public class TargetTaskFactory implements TaskFactory
{
   private static Logger _logger = Logger.getLogger(TargetTaskFactory.class);
   private ModePageRegistry modePageRegistry;
   
   
   public TargetTaskFactory(ModePageRegistry modePageRegistry)
   {
      this.modePageRegistry = modePageRegistry;
   }



   public Task getInstance( TargetTransportPort port, Command command) 
         throws IllegalRequestException
   {
      // TODO Auto-generated method stub
      return null;
   }


}
