
package org.jscsi.scsi.transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.protocol.sense.SenseData;
import org.jscsi.scsi.protocol.sense.SenseDataFactory;
import org.jscsi.scsi.target.Target;

public abstract class AbstractTargetTransport implements TargetTransportPort
{
   private static Logger _logger = Logger.getLogger(AbstractTargetTransport.class);

   private HashMap<Long,ByteBuffer> readDataMap = new HashMap<Long,ByteBuffer>();
   private HashMap<Long,ByteBuffer> writeDataMap = new HashMap<Long,ByteBuffer>();
   
   public boolean readData(Nexus nexus, long cmdRef, ByteBuffer output)
   throws InterruptedException
   {
      _logger.debug("servicing readData request: nexus: " + nexus + ", cmdRef: " + cmdRef);
      output.put(this.readDataMap.get(cmdRef).array());
      return true;
   }

   public void registerTarget(Target target)
   {
      _logger.debug("servicing registerTarget request");
   }

   public void removeTarget(String targetName) throws Exception
   {
      _logger.debug("servicing removeTarget request");
   }

   public void terminateDataTransfer(Nexus nexus, long commandReferenceNumber)
   {
      _logger.debug("servicing terminateDataTransfer request");
   }

   public boolean writeData(Nexus nexus, long cmdRef, ByteBuffer input)
   throws InterruptedException
   {
      _logger.debug("servicing writeData request: nexus: " + nexus + ", cmdRef: " + cmdRef);

      ByteBuffer newbuf = ByteBuffer.allocate(input.limit() - input.position());
      newbuf.put(input);
      this.writeDataMap.put(cmdRef, newbuf);
      return true;
   }

   public void writeResponse(
         Nexus nexus,
         long commandReferenceNumber,
         Status status,
         ByteBuffer senseData)
   {
      _logger.debug("servicing writeResponse request: nexus: " + nexus + ", cmdRef: " + commandReferenceNumber);
      _logger.debug("response was status: " + status);
      if (status.equals(Status.CHECK_CONDITION))
      {
         SenseData sense = null;
         try
         {
            sense = new SenseDataFactory().decode(senseData);
         }
         catch (IOException e)
         {
            _logger.warn("I/O exception while decoding sense data");
         }
         _logger.error("sense data: " + sense);
      }
   }

   @Override
   public String toString()
   {
      return "<DummyTargetTransportPort>";
   }
}


