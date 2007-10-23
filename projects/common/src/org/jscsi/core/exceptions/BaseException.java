package org.jscsi.core.exceptions;

import org.apache.log4j.Logger;

public class BaseException extends java.lang.Exception
{
   private static final long serialVersionUID = -8513902632562226623L;
   
   private static Logger _logger = Logger.getLogger(BaseException.class);

   public BaseException()
   {
      super();
      if (_logger.isTraceEnabled())
         _logger.trace("Exception", this);
   }

   public BaseException(String reason, Throwable cause)
   {
      super(reason == null ? "" : reason, cause);
      if (_logger.isTraceEnabled())
         _logger.trace("Exception: " + reason, this);
   }

   public BaseException(String reason)
   {
      super(reason);
      if (_logger.isTraceEnabled())
         _logger.trace("Exception: " + reason, this);
   }

   public BaseException(Throwable cause)
   {
      super(cause);
      if (_logger.isTraceEnabled())
         _logger.trace("Exception", this);
   }

   public String getMessage()
   {
      return super.getMessage();
   }

   public String toString()
   {
      StringBuffer fullMessage = new StringBuffer();

      if (getMessage() != null)
      {
         fullMessage.append(getMessage());
      }
      else
      {
         fullMessage.append("(No message provided)");
      }

      if (getCause() != null && getCause().getMessage() != null)
      {
         fullMessage.append(", Caused by: ").append(getCause().getMessage());
      }
      return fullMessage.toString();
   }
}
