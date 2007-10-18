
package org.jscsi.scsi.protocol.sense.exceptions;
import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.additional.FieldPointer;
import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;

// TODO: Describe class or interface
public abstract class IllegalRequestException extends SenseException
{
   
   public IllegalRequestException(
         KCQ kcq,
         boolean current )
   {
      super( kcq, current );
   }
   
   
   protected abstract FieldPointer getFieldPointer();
   

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
      return this.getFieldPointer();
   }

}


