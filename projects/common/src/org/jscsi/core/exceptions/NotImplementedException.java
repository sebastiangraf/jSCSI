package org.jscsi.core.exceptions;

import org.apache.log4j.Logger;

public class NotImplementedException extends RuntimeException
{
   private static final long serialVersionUID = -6494714494565370205L;
 
   private static Logger _logger = Logger.getLogger(NotImplementedException.class);
   
   public NotImplementedException()
   {
      super("This feature is not implemented.");
      _logger.error("Exception: Unimplemented feature", this);
   }

   public NotImplementedException(String reason)
   {
      super(reason);
      _logger.error("Exception: " + reason, this);
   }
}
