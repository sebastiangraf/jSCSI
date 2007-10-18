
package org.jscsi.scsi.protocol.sense.exceptions;
import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;

// TODO: Describe class or interface
public class InquiryDataHasChangedException extends UnitAttentionException
{
   public InquiryDataHasChangedException()
   {
      super(KCQ.INQUIRY_DATA_HAS_CHANGED);
   }
   
}


