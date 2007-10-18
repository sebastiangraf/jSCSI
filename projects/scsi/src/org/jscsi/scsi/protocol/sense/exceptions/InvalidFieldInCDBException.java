
package org.jscsi.scsi.protocol.sense.exceptions;
import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.additional.FieldPointer;

// TODO: Describe class or interface
public class InvalidFieldInCDBException extends IllegalRequestException
{

   FieldPointer fieldPointer;

   public InvalidFieldInCDBException(
         boolean current,
         byte bitPointer,
         int fieldPointer )
   {
      super(KCQ.INVALID_FIELD_IN_CDB, current);
      assert bitPointer <= 0x07 : "bit pointer value out of range";
      assert fieldPointer <= 65536 : "field pointer value out of range";
      
      this.fieldPointer = new FieldPointer(true, bitPointer, fieldPointer);
   }
   
   public InvalidFieldInCDBException( boolean current, int fieldPointer )
   {
      super(KCQ.INVALID_FIELD_IN_CDB, current);
      assert fieldPointer <= 65536 : "field pointer value out of range";
      
      this.fieldPointer = new FieldPointer(true, (byte)-1, fieldPointer);
   }

   @Override
   protected FieldPointer getFieldPointer()
   {
      return this.fieldPointer;
   }     
   
   
   
   
   

}


