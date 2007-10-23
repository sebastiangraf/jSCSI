package org.jscsi.scsi.protocol.sense.exceptions;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.additional.ActualRetryCount;
import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;

// TODO: Describe class or interface
public abstract class MediumErrorException extends SenseException
{
   public MediumErrorException(
         KCQ kcq,
         boolean current )
   {
      super( kcq, current );
   }
   
   protected abstract int getActualRetryCount();

   protected abstract long getLogicalBlockAddress();
   
}


