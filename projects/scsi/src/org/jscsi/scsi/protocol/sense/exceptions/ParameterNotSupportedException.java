
package org.jscsi.scsi.protocol.sense.exceptions;

// TODO: Describe class or interface
public class ParameterNotSupportedException extends InvalidFieldInParameterListException
{

   public ParameterNotSupportedException(boolean current, byte bitPointer, int fieldPointer)
   {
      super(current, bitPointer, fieldPointer);
   }

   public ParameterNotSupportedException(boolean current, int fieldPointer)
   {
      super(current, fieldPointer);
   }

}


