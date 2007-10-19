
package org.jscsi.scsi.protocol.sense.exceptions;
import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.additional.FieldPointer;

// TODO: Describe class or interface
public class InvalidCommandOperationCodeException extends IllegalRequestException
{
   
   public InvalidCommandOperationCodeException()
   {
      super(KCQ.INVALID_COMMAND_OPERATION_CODE, true);
   }
   
   @Override
   protected FieldPointer getFieldPointer()
   {      
      return new FieldPointer(true, (byte)-1, 0);
   }

  

}


