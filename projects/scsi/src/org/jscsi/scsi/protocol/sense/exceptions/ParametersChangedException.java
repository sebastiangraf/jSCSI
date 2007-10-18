
package org.jscsi.scsi.protocol.sense.exceptions;
import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;

// TODO: Describe class or interface
public class ParametersChangedException extends UnitAttentionException
{

   public ParametersChangedException()
   {
      super(KCQ.PARAMETERS_CHANGED);
   }
   
   protected ParametersChangedException(KCQ kcq)
   {
      super(kcq);
   }
   
}


