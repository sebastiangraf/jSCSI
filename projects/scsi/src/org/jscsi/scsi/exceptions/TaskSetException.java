package org.jscsi.scsi.exceptions;

import org.apache.log4j.Logger;
import org.jscsi.core.exceptions.BaseException;

public class TaskSetException extends BaseException
{
   private static final long serialVersionUID = -5977468768654663316L;
 
   private static Logger _logger = Logger.getLogger(TaskSetException.class);

   public TaskSetException()
   {
      super("This feature is not implemented.");
      _logger.error("Exception: Unimplemented feature", this);
   }

   public TaskSetException(String reason)
   {
      super(reason);
      _logger.error("Exception: " + reason, this);
   }
}
