
package org.jscsi.scsi.protocol.sense.exceptions;
import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.additional.FieldPointer;

// TODO: Describe class or interface
public class LogicalUnitNotSupportedException extends IllegalRequestException
{

   public LogicalUnitNotSupportedException()
   {
      super(KCQ.LOGICAL_UNIT_NOT_SUPPORTED, true);
   }
   
   @Override
   protected FieldPointer getFieldPointer()
   {
      return null;
   }

}


