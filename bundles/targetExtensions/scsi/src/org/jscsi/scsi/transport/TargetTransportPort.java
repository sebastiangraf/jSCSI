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

package org.jscsi.scsi.transport;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import org.jscsi.core.scsi.Status;

/**
 * The SCSI Target Transport Port provides an interface to the Service Delivery Subsystem services
 * required by SCSI Target Devices.
 * <p>
 * The target port is responsible for enqueuing incoming commands onto the Task Router of the
 * appropriate SCSI target. As the Task Router does not distinguish between command and data
 * transfer services the transport port is responsible for writing all expected incoming data to a
 * byte buffer and enqueuing that buffer along with the incoming command. The transport port must
 * then process returning status, sense data, and input data buffers which are sent directly from
 * Logical Units.
 */
public interface TargetTransportPort
{
   /**
    * Performs Receive Data-Out operation. If successful all expected data will have been read to
    * the output buffer. If it failed a partial transfer may have occurred.
    * <p>
    * This method is called by task implementations for commands such as WRITE, MODE SENSE, and
    * REPORT LUNS.
    * <p>
    * This method shall normally return false if the nexus or command reference number are
    * invalid. However, if {@link #terminateDataTransfer(Nexus, long)} is called this method
    * shall throw an InterruptedException for any nexus and command reference number which
    * was terminated until those values are reused by a new incoming command.
    * <p>
    * This method shall write zero bytes and return true if the expected transfer length is zero.
    * 
    * @param nexus Generally either an I_T_L nexus or an I_T_L_Q nexus
    * @param commandReferenceNumber The command reference number associated with the nexus.
    * @param output The data output buffer which data will be written to. The transport port
    *    shall put at {@link ByteBuffer#position()} until all incoming data is written. The
    *    operation will fail if a {@link BufferOverflowException} occurs. The caller should
    *    take care to rewind or reset the buffer on completion.
    * @return True if all expected data has been written; False if no data or partial data
    *    has been written.
    */
   boolean readData(Nexus nexus, long commandReferenceNumber, ByteBuffer output)
         throws InterruptedException;

   /**
    * Performs a Send Data-In operation. If successful all expected data will have been written
    * from the input buffer. If it failed a partial transfer may have occurred.
    * <p>
    * This method is called by task implementations for commands such as READ and MODE SELECT.
    * <p>
    * This method shall normally return false if the nexus or command reference number are
    * invalid. However, if {@link #terminateDataTransfer(Nexus, long)} is called this method
    * shall throw an InterruptedException for any nexus and command reference number which
    * was terminated until those values are reused by a new incoming command.
    * <p>
    * This method shall write zero bytes and return true if the expected transfer length is zero.
    * 
    * @param nexus Generally either an I_T_L nexus or an I_T_L_Q nexus.
    * @param commandReferenceNumber The command reference number associated with the nexus.
    * @param input The data input buffer which data will be read from. The transport port shall
    *    transfer all data from {@link ByteBuffer#position()} to {@link ByteBuffer#limit()}. The
    *    expected transfer length is thus (limit - position).
    * @return True if all expected data has been written; False if no data or partial data has
    *    been written.
    */
   boolean writeData(Nexus nexus, long commandReferenceNumber, ByteBuffer input)
         throws InterruptedException;

   /**
    * Instructs the transport layer to terminate data transfer for the indicated nexus. The
    * transport layer shall throw an {@link InterruptedException} from any in-progress
    * {@link #writeData(Nexus, long, ByteBuffer)} or {@link #readData(Nexus, long, ByteBuffer)}
    * operation.
    * <p>
    * Interrupting a thread performing a read or write operation shall have the same effect.
    * <p>
    * This method does nothing if there are no in-progress data transfers for the indicated nexus.
    * <p>
    * Future attempts to transfer data using this nexus and command reference number shall
    * also throw an {@link InterruptedException} until a new incoming command reusing those
    * values appears. At that point transfer attempts must return either <code>true</code> for
    * success or <code>false</code> for failure.
    * 
    * @param nexus Generally either an I_T_L nexus or an I_T_L_Q nexus.
    * @param commandReferenceNumber
    */
   void terminateDataTransfer(Nexus nexus, long commandReferenceNumber);

   /**
    * Enqueues return data to send to the initiator indicated by the given Nexus. Used by both Task
    * Routers and Logical Units, depending on the original command.
    * 
    * @param nexus The nexus of the original SCSI request.
    * @param commandReferenceNumber The command reference number associated with the nexus.
    * @param status The final status of the command.
    * @param senseData Autosense data; <code>null</code> if the status is not 
    *    {@link Status#CHECK_CONDITION}. Transfer from the sense data buffer shall occur in the
    *    same manner as the data buffer in {@link #writeData(Nexus, long, ByteBuffer)}.
    */
   void writeResponse(Nexus nexus, long commandReferenceNumber, Status status, ByteBuffer senseData);
}
