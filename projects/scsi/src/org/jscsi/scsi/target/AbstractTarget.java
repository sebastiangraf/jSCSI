package org.jscsi.scsi.target;

import java.util.List;

import org.jscsi.scsi.authentication.AuthenticationHandler;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.transport.TargetTransportPort;

public abstract class AbstractTarget implements Target
{
   ////////////////////////////////////////////////////////////////////////////////////////////////
   // private data members
   
   
   private String _targetName;
   private List<AuthenticationHandler> _authHandlers;

   
   ////////////////////////////////////////////////////////////////////////////////////////////////
   // abstract methods
   
   
   public abstract void enqueue( TargetTransportPort port, Command command );


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
    * The Target Device Name of this target.
    */
   public void setTargetName(String targetName)
   {
      this._targetName = targetName;
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
