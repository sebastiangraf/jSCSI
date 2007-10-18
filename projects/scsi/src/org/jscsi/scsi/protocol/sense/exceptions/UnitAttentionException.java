
package org.jscsi.scsi.protocol.sense.exceptions;
import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;

// TODO: Describe class or interface
public abstract class UnitAttentionException extends SenseException
{
   
   public UnitAttentionException(KCQ kcq)
   {
      super(kcq, true); // UA condition exceptions are always current
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


