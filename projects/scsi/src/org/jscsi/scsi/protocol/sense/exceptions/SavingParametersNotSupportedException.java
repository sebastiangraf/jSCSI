
package org.jscsi.scsi.protocol.sense.exceptions;
import org.jscsi.scsi.protocol.sense.KCQ;

// TODO: Describe class or interface
public class SavingParametersNotSupportedException extends InvalidFieldInCDBException
{
   private static final long serialVersionUID = -3686281446274870771L;

   public SavingParametersNotSupportedException(boolean current, byte bitPointer, int fieldPointer)
   {
      super(KCQ.SAVING_PARAMETERS_NOT_SUPPORTED, current, bitPointer, fieldPointer);
   }

   public SavingParametersNotSupportedException(boolean current, int fieldPointer)
   {
      super(KCQ.SAVING_PARAMETERS_NOT_SUPPORTED, current, (byte)-1, fieldPointer);
   }
   

}


