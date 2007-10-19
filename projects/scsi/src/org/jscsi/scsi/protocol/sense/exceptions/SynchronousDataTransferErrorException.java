
package org.jscsi.scsi.protocol.sense.exceptions;
import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;

// TODO: Describe class or interface
public class SynchronousDataTransferErrorException extends SenseException
{
   
   public SynchronousDataTransferErrorException()
   {
      super(KCQ.SYNCHRONOUS_DATA_TRANSFER_ERROR, true);
   }

   @Override
   protected byte[] getCommandSpecificInformation()
   {
      return null;
   }

   @Override
   protected byte[] getInformation()
   {
      return null;
   }

   @Override
   protected SenseKeySpecificField getSenseKeySpecific()
   {
      return null;
   }

}


