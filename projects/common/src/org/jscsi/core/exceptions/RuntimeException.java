package org.jscsi.core.exceptions;

import org.apache.log4j.Logger;

public class RuntimeException extends java.lang.RuntimeException
{
   private static final long serialVersionUID = -1381674358075394473L;
   
   private static Logger _logger = Logger.getLogger(RuntimeException.class);
   
   public RuntimeException()
   {
      super();
      _logger.error("Exception", this);
   }

   public RuntimeException(String reason, Throwable cause)
   {
      super(reason == null ? "" : reason, cause);
      _logger.error("Exception: " + reason, this);
   }

   public RuntimeException(String reason)
   {
      super(reason);
      _logger.error("Exception: " + reason, this);
   }

   public RuntimeException(Throwable cause)
   {
      super(cause);
      _logger.error("Exception", this);
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

      if (getCause() != null)
      {
         if (getCause().getMessage() != null)
         {
            fullMessage.append(", Caused by: ").append(getCause().getMessage());
         }
      }
      return fullMessage.toString();
   }
}
