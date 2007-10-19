
package org.jscsi.scsi.protocol.sense.exceptions;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.additional.FieldPointer;

// TODO: Describe class or interface
public class CommandSequenceErrorException extends IllegalRequestException
{
   
   public CommandSequenceErrorException()
   {
      super(KCQ.COMMAND_SEQUENCE_ERROR, true);
   }

   @Override
   protected FieldPointer getFieldPointer()
   {
      return null;
   }

}


