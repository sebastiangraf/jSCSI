/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

package org.jscsi.scsi.lu;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.tasks.management.TaskServiceResponse;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;

/**
 * A SCSI Logical Unit. LUs must be registered with a target to receive tasks. The LU must implement
 * a task manager of some form. Commands will be enqueued to the LU as they are received by the
 * Target Task Router.
 * <p>
 * Some LU implementations may exhibit the following "vendor specific" behavior not conforming to
 * SAM-2 or SAM-3: The LU may required activation for a specific I_T_L nexus before tasks will be
 * processed. These implementations must return a BUSY status until that I_T_L nexus is allowed.
 * This behavior should only be implemented for Logical Units which depend on iSCSI login data
 * before the unit is ready. If an LU does not require login data it must not exhibit this data; it
 * must rely on the iSCSI transport layer to provide authentication services.
 */
public interface LogicalUnit
{
   /**
    * Used by the Task Router to enqueue a command onto the Logical Unit task queue. The Logical
    * Unit then interprets these commands into Tasks.
    * 
    * @param port
    *           The transport port where the command originated. The Logical Unit will return data
    *           directly to the transport port.
    * @param command
    *           The incoming command.
    * @param output
    *           Any incoming data; <code>null</code> if the command did not require an incoming
    *           data transfer.
    */

   void enqueue(TargetTransportPort port, Command command);

   /**
    * Aborts the tagged task specified by the given I_T_L_Q nexus.
    * The tagged task will be immediately aborted. Abortion of untagged tasks is not possible.
    * 
    * @param nexus An I_T_L_Q nexus identifying a given task.
    * @returns {@value TaskServiceResponse#FUNCTION_COMPLETE} when aborted successfully or
    *    {@value TaskServiceResponse#FUNCTION_REJECTED} when not aborted.
    */
   TaskServiceResponse abortTask(Nexus nexus);

   /**
    * Aborts all tasks that were created by the SCSI initiator port and routed through the SCSI
    * target port indicated by the given I_T_L nexus.
    * 
    * @param nexus An I_T_L nexus identifying a given initiator and target port.
    * @returns {@value TaskServiceResponse#FUNCTION_COMPLETE} when aborted successfully or
    *    {@value TaskServiceResponse#FUNCTION_REJECTED} when not aborted.
    */
   TaskServiceResponse abortTaskSet(Nexus nexus);

   /**
    * Aborts all tasks in the appropriate task set as defined by the <code>TST</code> field
    * in the Control mode page (see SPC-2).
    * 
    * @param nexus An I_T_L nexus identifying a given task set.
    * @returns {@value TaskServiceResponse#FUNCTION_COMPLETE} when cleared successfully or
    *    {@value TaskServiceResponse#FUNCTION_REJECTED} when not cleared.
    */
   TaskServiceResponse clearTaskSet(Nexus nexus);

   /**
    * Resets this Logical Unit as required by the SAM-2 <code>LOGICAL UNIT RESET</code> function.
    * @returns {@value TaskServiceResponse#FUNCTION_COMPLETE} when reset successfully or
    *    {@value TaskServiceResponse#FUNCTION_REJECTED} when not reset.
    */
   TaskServiceResponse reset();

   /**
    * Starts this logical unit. Called by a running target when the logical unit is registered.
    */
   void start();

   /**
    * Stops this logical unit. Called by a target when this logical unit is unregistered or the
    * target is shutting down.
    */
   void stop();

   void nexusLost();
}
