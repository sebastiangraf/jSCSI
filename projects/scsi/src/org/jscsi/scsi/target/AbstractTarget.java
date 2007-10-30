package org.jscsi.scsi.target;

import java.util.List;

import org.apache.log4j.Logger;
import org.jscsi.scsi.authentication.AuthenticationHandler;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.tasks.TaskRouter;
import org.jscsi.scsi.transport.TargetTransportPort;

public abstract class AbstractTarget implements Target
{
   private static Logger _logger = Logger.getLogger(AbstractTarget.class);
   
   
   ////////////////////////////////////////////////////////////////////////////////////////////////
   // private data members
   
   
   private String _targetName;
   private TaskRouter _taskRouter;
   private List<AuthenticationHandler> _authHandlers;

   
   ////////////////////////////////////////////////////////////////////////////////////////////////
   // primary methods
   
   
   public void enqueue( TargetTransportPort port, Command command )
   {
      this._taskRouter.enqueue(port, command);
   }


   ////////////////////////////////////////////////////////////////////////////////////////////////
   // getters/setters
   
   /**
    * The Target Device Name of this target.
    */
   public String getTargetName()
   {
      return _targetName;
   }


   /**
    * A list of authentication handlers for this target. The authentication methods must be
    * presented to the initiator in the order returned by this list.
    */
   public List<AuthenticationHandler> getAuthHandlers()
   {
      return _authHandlers;
   }

   /**
    * A list of authentication handlers for this target. The authentication methods must be
    * presented to the initiator in the order returned by this list.
    */
   public void setAuthHandlers(List<AuthenticationHandler> handlers)
   {
      _authHandlers = handlers;
   }
}
