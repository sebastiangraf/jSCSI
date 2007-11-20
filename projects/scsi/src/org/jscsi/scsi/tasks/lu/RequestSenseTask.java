
package org.jscsi.scsi.tasks.lu;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.RequestSense;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.DescriptorSenseData;
import org.jscsi.scsi.protocol.sense.FixedSenseData;
import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.transport.TargetTransportPort;

public class RequestSenseTask extends LUTask
{
   private static Logger _logger = Logger.getLogger(RequestSenseTask.class);

   public RequestSenseTask()
   {
      super("RequestSenseTask");
   }
   
   public RequestSenseTask(
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      super("RequestSenseTask", targetPort, command, modePageRegistry, inquiryDataRegistry);
      // TODO Auto-generated constructor stub
   }



   @Override
   protected void execute() throws InterruptedException, SenseException
   {
      _logger.debug("executing logical unit 'request sense' task");

      RequestSense cdb = (RequestSense) getCommand().getCommandDescriptorBlock();

      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bs);

      try
      {
         if (cdb.isDESC())
         {
            out.write((new DescriptorSenseData()).encode());
         }
         else
         {
            out.write((new FixedSenseData(true, KCQ.NO_ERROR, null, null, null)).encode());
         }
      }
      catch (IOException e)
      {
         _logger.error("unable to encode sense data: " + e);
         throw new RuntimeException("unable to encode sense data", e);
      }

      this.writeData(bs.toByteArray());
      this.writeResponse(Status.GOOD, null);
   }
}
