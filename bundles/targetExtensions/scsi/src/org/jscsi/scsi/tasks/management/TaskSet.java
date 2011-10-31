/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
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

package org.jscsi.scsi.tasks.management;

import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;

import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.transport.Nexus;

/**
 * A SAM-2 task set. The task set implements the BlockingQueue interface and provides additional
 * methods for removing a single task identified by a task tag or all tasks.
 * <p>
 * Some implementations may provide one task set for all I_T nexuses. In such cases task management
 * commands will affect outstanding commands for all initiators. 
 */
public interface TaskSet extends BlockingQueue<Task>
{

   /**
    * Aborts and removes the task specified by the given I_T_L_Q nexus. The caller obtains the
    * results of {@link Task#abort()}.
    * <p>
    * This method should be used when removing a task outside the normal execution flow. One
    * such usage is in processing the SAM-2 <code>ABORT TASK</code> task management function.
    * 
    * @param nexus An I_T_L_Q nexus identifying a given task.
    * @return The results of {@link Task#abort()}.
    * @throws NoSuchElementException If a task with the given task tag does not exist.
    * @throws InterruptedException If the thread is interrupted during the abort attempt.
    * @throws IllegalArgumentException If an I_T_L_Q nexus is not provided.
    */
   boolean remove(Nexus nexus) throws NoSuchElementException, InterruptedException,
         IllegalArgumentException;

   /**
    * Aborts all in-progress tasks and clears the task set for the given I_T_L nexus. Succeeds even
    * if some tasks fail to abort. 
    * <p>
    * Implementations which maintain a separate task set per I_T nexus will abort only those tasks
    * which match the indicated initiator. Implementations which maintain a shared task set will
    * abort all tasks for all initiators.
    * 
    * @param nexus An I_T_L nexus identifying a given task set.
    * @throws InterruptedException If the thread is interrupted during the abort attempt.
    * @throws IllegalArgumentException If an I_T_L nexus is not provided.
    */
   void clear(Nexus nexus) throws InterruptedException, IllegalArgumentException;

   /**
    * Aborts in-progress tasks and clears the task set of tasks that were created by the SCSI
    * initiator port and routed through the SCSI target port indicated by the given I_T_L nexus.
    * Succeeds even if some tasks fail to abort.
    * <p>
    * Some simple implementations may call {@link #clear(Nexus)} from this method. This will
    * result in less fine-grain task management and should not be used for logical units which
    * aim to properly support multiple simultaneous initiator connections.
    * 
    * @param nexus An I_T_L nexus identifying a source initiator and intermediate port.
    * @throws InterruptedException If the thread is interrupted during the abort attempt.
    * @throws IllegalArgumentException If an I_T_L nexus is not provided.
    */
   void abort(Nexus nexus) throws InterruptedException, IllegalArgumentException;
}
