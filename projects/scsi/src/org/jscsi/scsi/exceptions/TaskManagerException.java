package org.jscsi.scsi.exceptions;

import org.apache.log4j.Logger;
import org.jscsi.core.exceptions.BaseException;

// TODO: Describe class or interface
public class TaskManagerException extends BaseException
{
   private static final long serialVersionUID = -7610013280318700017L;
 
   private static Logger _logger = Logger.getLogger(TaskManagerException.class);
   
   public TaskManagerException()
   {
      super("This feature is not implemented.");
      _logger.error("Exception: Unimplemented feature", this);
   }

   public TaskManagerException(String reason)
   {
      super(reason);
      _logger.error("Exception: " + reason, this);
   }
   
   public TaskManagerException(Throwable cause)
   {
      super(cause);
      _logger.error("Exception: " + cause.toString(), this);
   }
}
