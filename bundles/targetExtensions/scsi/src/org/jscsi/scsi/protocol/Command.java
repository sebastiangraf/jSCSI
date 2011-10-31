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

package org.jscsi.scsi.protocol;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.CDBFactory;
import org.jscsi.scsi.tasks.TaskAttribute;
import org.jscsi.scsi.transport.Nexus;

/**
 * The Command class contains methods needed by the SCSI Transport Protocol Services
 * and the Target Port Task Router.
 * <p>
 * A command is encoded by the Transport Protocol Services on the initiator and decoded
 * by the Transport Protocol Services on the target. This class provides enough
 * information to accomplish this.
 * <p>
 * The Task Router enqueues the command on the proper Logical Unit where it is converted into
 * a Task; some commands are processed directly by the Task Router. A Task uses the Target
 * Transport Port to directly return any data or results.
 * 
 */
public class Command
{
   private static CDBFactory _factory = new CDBFactory();

   private Nexus nexus;
   private CDB commandDescriptorBlock;
   private TaskAttribute taskAttribute;
   private long commandReferenceNumber;
   private int taskPriority;

   /**
    * Creates a Command object. The Command Descriptor Block is decoded using
    * {@link CommandDescriptorBlockFactory#decode(ByteBuffer).
    * 
    * @param nexus The I_T, I_T_L, or I_T_L_Q nexus of the given command.
    * @param commandDescriptorBlock A byte buffer containing the serialized CDB.
    * @param taskAttribute The command's task attribute.
    * @param commandReferenceNumber The command reference number.
    * @param taskPriority The task priority.
    * @throws BufferUnderflowException If the 
    * @throws IOException
    */
   public Command(
         Nexus nexus,
         ByteBuffer commandDescriptorBlock,
         TaskAttribute taskAttribute,
         long commandReferenceNumber,
         int taskPriority) throws BufferUnderflowException, IOException
   {
      super();
      this.nexus = nexus;
      this.commandDescriptorBlock = _factory.decode(commandDescriptorBlock);
      this.taskAttribute = taskAttribute;
      this.commandReferenceNumber = commandReferenceNumber;
      this.taskPriority = taskPriority;
   }

   public Command(
         Nexus nexus,
         CDB commandDescriptorBlock,
         TaskAttribute taskAttribute,
         long commandReferenceNumber,
         int taskPriority)
   {
      super();
      this.nexus = nexus;
      this.commandDescriptorBlock = commandDescriptorBlock;
      this.taskAttribute = taskAttribute;
      this.commandReferenceNumber = commandReferenceNumber;
      this.taskPriority = taskPriority;
   }

   public Nexus getNexus()
   {
      return nexus;
   }

   public CDB getCommandDescriptorBlock()
   {
      return commandDescriptorBlock;
   }

   public TaskAttribute getTaskAttribute()
   {
      return taskAttribute;
   }

   public long getCommandReferenceNumber()
   {
      return commandReferenceNumber;
   }

   public int getTaskPriority()
   {
      return taskPriority;
   }

   @Override
   public String toString()
   {
      return String.format("<Command: %s, nexus: %s, TaskAttribute: %s, CmdRef: %d>",
            this.commandDescriptorBlock, this.nexus, this.taskAttribute,
            this.commandReferenceNumber);
   }
}
