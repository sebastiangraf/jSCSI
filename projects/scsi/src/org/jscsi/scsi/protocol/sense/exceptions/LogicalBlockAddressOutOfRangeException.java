
package org.jscsi.scsi.protocol.sense.exceptions;
import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.additional.FieldPointer;
import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;

// TODO: Describe class or interface
public class LogicalBlockAddressOutOfRangeException extends IllegalRequestException
{
   private FieldPointer fieldPointer;
   
   /**
    * Indicates an LBA value was out of range.
    * @param current True if the error is current; False if the error is deferred.
    * @param command True if the error occurred in a CDB; False if the error was in parameter data.
    * @param bitPointer The first bit where an error occurs; bit value indicated in MSB
    *    (left-most) form.
    * @param fieldPointer The first byte where an error occurs.
    */
   public LogicalBlockAddressOutOfRangeException(
         boolean current,
         boolean command,
         byte bitPointer,
         int fieldPointer )
   {
      super(KCQ.LOGICAL_BLOCK_ADDRESS_OUT_OF_RANGE, current);
      assert bitPointer <= 0x07 : "bit pointer value out of range";
      assert fieldPointer <= 65536 : "field pointer value out of range";
      
      this.fieldPointer = new FieldPointer(command, bitPointer, fieldPointer);
   }
   
   /**
    * Indicates an LBA value was out of range.
    * @param current True if the error is current; False if the error is deferred.
    * @param command True if the error occurred in a CDB; False if the error was in parameter data.
    * @param fieldPointer The first byte where an error occurs.
    */
   public LogicalBlockAddressOutOfRangeException(
         boolean current,
         boolean command,
         int fieldPointer )
   {
      super(KCQ.LOGICAL_BLOCK_ADDRESS_OUT_OF_RANGE, current);
      assert fieldPointer <= 65536 : "field pointer value out of range";
      
      this.fieldPointer = new FieldPointer(command, (byte)-1, fieldPointer);
      
   }
   
   @Override
   protected FieldPointer getFieldPointer()
   {
      return this.fieldPointer;
   }


}


