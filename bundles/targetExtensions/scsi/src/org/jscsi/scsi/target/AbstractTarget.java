//Cleversafe open-source code header - Version 1.1 - December 1, 2006
//
//Cleversafe Dispersed Storage(TM) is software for secure, private and
//reliable storage of the world's data using information dispersal.
//
//Copyright (C) 2005-2007 Cleversafe, Inc.
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
//USA.
//
//Contact Information: 
// Cleversafe, 10 W. 35th Street, 16th Floor #84,
// Chicago IL 60616
// email: licensing@cleversafe.org
//
//END-OF-HEADER
//-----------------------
//@author: John Quigley <jquigley@cleversafe.com>
//@date: January 1, 2008
//---------------------

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

   public abstract void enqueue(TargetTransportPort port, Command command);

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
