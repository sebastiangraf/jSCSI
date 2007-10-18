
package org.jscsi.scsi.protocol.sense.exceptions;

import org.jscsi.scsi.protocol.sense.KCQ;

// TODO: Describe class or interface
public class CapacityDataHasChangedException extends ParametersChangedException
{

   public CapacityDataHasChangedException()
   {
      super( KCQ.CAPACITY_DATA_HAS_CHANGED );
   }

}


