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

package org.jscsi.scsi.transport;

import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Command;

/**
 * The SCSI Initiator Transport Port provides an interface to the Service Delivery Subsystem
 * services required by SCSI Initiator Devices.
 * <p>
 * Data In and Data Out services are handled internally using the input and output byte buffers
 * specified when a command is sent.
 */
public interface InitiatorTransportPort
{

   /**
    * Sends a command to a Target device. Most commands must have a valid I_T_L_x nexus specified.
    * Commands directed at the target device as a whole (such as REPORT LUNS) must have a valid
    * I_T nexus.
    * <p>
    * The application client must specify input and output byte buffers as required by the sent
    * command. The transport port handles reading from the output buffer and writing to the
    * input buffer. The input buffer must be large enough to handle the expected return data.
    * The output buffer must not be changed before a task complete confirmation is returned.
    * <p>
    * On task completion service response, status, and sense data are returned to the
    * confirmation handler. A same output byte buffer as passed in will also be returned.
    * Confirmations will occur asynchronously as tasks are completed by the target device.
    * 
    * @param command A command with a valid I_T or I_T_L_x nexus.
    * @param input A byte buffer ready to accept incoming data. The current write position must
    *    be the position the application client wishes incoming data to be written to. 
    *    The limit must be equal to or greater than (expected input length + current position).
    *    May be <code>null</code> if the command will not return data. It must be possible to
    *    do random (non-sequential) writes to the byte buffer.
    * @param output A byte buffer containing the desired output data. The current position must
    *    be the position from which the application client wishes to send data from.
    *    The limit must be equal to or greater than (output length + current position). May be
    *    <code>null</code> if the command will not write data. It must be possible to perform
    *    random (non-sequential) reads from the output buffer.
    * @param handler Task complete confirmation messages will be sent to this command handler.
    *    A new buffer will be allocated to store any incoming sense data to. The handler should
    *    be capable of parsing the sense data returned by the specific command.
    */
   void send(Command command, ByteBuffer input, ByteBuffer output, ConfirmationHandler handler);

   /**
    * Cancel a task specified by an I_T_L_Q nexus. The cancellation will not be
    * processed if the nexus object does not specify a task tag.
    * 
    * @param nexus An I_T_L_Q Nexus.
    */
   // TODO: Should this be blocking, or keep the confirmation handler?
   void cancel(Nexus nexus, ConfirmationHandler handler);

}
