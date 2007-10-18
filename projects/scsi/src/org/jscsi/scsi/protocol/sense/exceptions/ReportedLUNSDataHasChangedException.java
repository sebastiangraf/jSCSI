
package org.jscsi.scsi.protocol.sense.exceptions;

import org.jscsi.scsi.protocol.sense.KCQ;

// TODO: Describe class or interface
public class ReportedLUNSDataHasChangedException extends UnitAttentionException
{

   public ReportedLUNSDataHasChangedException()
   {
      super( KCQ.REPORTED_LUNS_DATA_HAS_CHANGED );
   }

}


