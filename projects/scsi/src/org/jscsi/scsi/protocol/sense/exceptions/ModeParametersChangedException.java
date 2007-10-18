
package org.jscsi.scsi.protocol.sense.exceptions;

import org.jscsi.scsi.protocol.sense.KCQ;

// TODO: Describe class or interface
public class ModeParametersChangedException extends ParametersChangedException
{

   public ModeParametersChangedException()
   {
      super(KCQ.MODE_PARAMETERS_CHANGED);
   }
}


