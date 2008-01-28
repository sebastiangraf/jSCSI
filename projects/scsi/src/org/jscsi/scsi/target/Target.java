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
import org.jscsi.scsi.lu.LogicalUnit;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.tasks.management.TaskManagementFunction;
import org.jscsi.scsi.tasks.management.TaskServiceResponse;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;

/**
 * A SCSI Target. The Target class provides a Target Name, a Task Router, and zero or more
 * Authentication Handlers.
 * <p>
 * The Target Name is used by the transport layer for any discovery mechanisms. As a proprietary
 * requirement, the transport layer must be capable of dynamic registration and removal of target
 * devices. Targets shall be referenced by Target Names within the transport layer.
 * <p>
 * The transport layer, after reading in the entire incoming data stream, will enqueue incoming
 * commands and a buffer containing the incoming data to the Target's Task Router. The Task Router
 * processes any commands sent to invalid Logical Units or non-LU commands. Other commands are
 * forwarded to the proper Logical Unit. Return data from Logical Units is sent directly to the
 * transport layer, bypassing the router.
 * <p>
 * Authentication handlers are used during the login phase for iSCSI transport. This authentication
 * architecture is a proprietary extension designed to support Logical Units which require
 * authentication. Authentication handlers are ignored for SCSI transport other than iSCSI.
 * 
 */
public interface Target
{

   /**
    * Enqueues a command to this target. If the command is addressed to an I_T_L_x nexus it
    * will be routed to the proper Logical Unit and executed. If addressed to an I_T nexus the
    * command will be executed by the target's task manager.
    * 
    * @param port The transport port where the command originated. Data will be returned
    *    directly to the transport port.
    * @param command The incoming command.
    */
   void enqueue(TargetTransportPort port, Command command);

   /**
    * Executes a task management function.
    * 
    * @param nexus Where the indicated function will be executed.
    * @param function The task management function to execute.
    * @return The result of the task management function.
    */
   TaskServiceResponse execute(Nexus nexus, TaskManagementFunction function);

   ////////////////////////////////////////////////////////////////////////////////////////////////
   // getters/setters

   /**
    * The Target Device Name of this target.
    */
   public String getTargetName();

   /**
    * A list of authentication handlers for this target. The authentication methods must be
    * presented to the initiator in the order returned by this list.
    */
   public List<AuthenticationHandler> getAuthHandlers();

   /**
    * A list of authentication handlers for this target. The authentication methods must be
    * presented to the initiator in the order returned by this list.
    */
   public void setAuthHandlers(List<AuthenticationHandler> handlers);

   /**
    * Starts this target device. Called by a running target transport port when the target is
    * registered.
    */
   void start();

   /**
    * Stops this target device. Called by a target transport port when this target is unregistered
    * or the transport port is shutting down.
    */
   void stop();

   /**
    * Used by the TargetTransportPort to indicate an I_T Nexus loss event. Indicates to each Logical
    * Unit that an I_T Nexus loss has occurred.
    */
   public void nexusLost();

   /**
    * Register a Logical Unit with the given LUN.
    * 
    * @param number The Logical Unit Number.
    * @param lu The Logical Unit.
    * @throws Exception If the LUN is already assigned.
    */
   void registerLogicalUnit(long lun, LogicalUnit lu);

   /**
    * Remove a Logical Unit from the task router. After removal no further commands will be
    * sent to the LU.
    * 
    * @param number The LUN.
    * @returns The logical unit.
    * @throws Exception If the LUN is not valid.
    */
   LogicalUnit removeLogicalUnit(long lun);

   /**
    * Indicates whether this target device has been start()ed.
    * @return Device status
    */
   boolean isRunning();
}
