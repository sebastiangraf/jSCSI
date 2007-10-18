
package org.jscsi.scsi.protocol.sense.exceptions;

// TODO: Describe class or interface
public class ParameterValueInvalidException extends InvalidFieldInParameterListException
{

   public ParameterValueInvalidException(boolean current, byte bitPointer, int fieldPointer)
   {
      super(current, bitPointer, fieldPointer);
   }

   public ParameterValueInvalidException(boolean current, int fieldPointer)
   {
      super(current, fieldPointer);
   }

}


