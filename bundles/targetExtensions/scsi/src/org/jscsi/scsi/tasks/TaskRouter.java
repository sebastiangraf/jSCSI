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


package org.jscsi.scsi.tasks;

import org.jscsi.scsi.lu.LogicalUnit;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.tasks.management.TaskManagementFunction;
import org.jscsi.scsi.tasks.management.TaskServiceResponse;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;

/**
 * Within a SCSI Target, the Task Router sits between the Target Ports and Logical Units.
 * The Task Router has two primary responsibilities.
 * <p>
 * First, the Task Router must maintain a list of Logical Units and enqueue incoming commands
 * to the proper Logical Unit as specified by the I_T_L_x Nexus on a given command. If an invalid
 * Logical Unit is specified, the router must process the command as specified in SAM-2 or SAM-3.
 * <p>
 * Second, for I_T Nexus commands ("non-LU commands") the router must process the command as
 * specified in SPC-2, SPC-3, or other relevant standard.
 * <p>
 * Router implementations must be capable of dynamic registration and removal of Logical Units.
 * When removed, no further commands sent to a particular Logical Unit shall be forwarded to that
 * LU.
 * <p>
 * The Task Router is not responsible for processing data transport services. Commands which
 * require incoming data must be presented to the router with a byte buffer containing all
 * expected data. Logical Units enqueue return data directly to the originating Target Transport
 * Port.
 */
public interface TaskRouter
{

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
    * @returns The Logical Unit.
    * @throws Exception If the LUN is not valid.
    */
   LogicalUnit removeLogicalUnit(long lun);

   /**
    * Used by the Target Transport Port to enqueue incoming commands to the task router. The
    * router is then responsible for forwarding those commands to Logical Units or returning
    * an error condition if the appropriate LU cannot be found.
    * 
    * @param port The transport port where the command originated. This is forwarded to the LU
    *    which then returns data directly to the transport port.
    * @param command The incoming command.
    * @param output Any incoming data; <code>null</code> if the command did not require an incoming
    *    data transfer.
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

   /**
    * Starts this task router. Called by a starting target.
    */
   void start();

   /**
    * Stops this task router. Called by a stopping target.
    */
   void stop();

   /**
    * Used by the Target Transport Port to indicate an I_T nexus loss event.
    */
   void nexusLost();

}
