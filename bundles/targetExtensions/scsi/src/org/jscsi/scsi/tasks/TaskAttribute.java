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

/**
 * Valid task attributes as defined in SAM-3 section 8.6.1.
 */
public enum TaskAttribute
{
   /**
    * If only SIMPLE QUEUE TAG messages are used, the target may execute the
    * commands in any order that is deemed desirable within the constraints of
    * the queue management algorithm specified in the control mode page (see
    * [SAM2, 8.3.3.1]).
    */
   SIMPLE,
   /**
    * If ORDERED QUEUE TAG messages are used, the target shall execute the
    * commands in the order received with respect to other commands received
    * with ORDERED QUEUE TAG messages. All commands received with a SIMPLE
    * QUEUE TAG message prior to a command received with an ORDERED QUEUE TAG
    * message, regardless of initiator, shall be executed before that command
    * with the ORDERED QUEUE TAG message. All commands received with a SIMPLE
    * QUEUE TAG message after a command received with an ORDERED QUEUE TAG
    * message, regardless of initiator, shall be executed after that command
    * with the ORDERED QUEUE TAG message.
    */
   ORDERED,
   /**
    * A command received with a HEAD OF QUEUE TAG message is placed first in
    * the queue, to be executed next. A command received with a HEAD OF QUEUE
    * TAG message shall be executed prior to any queued I/O process.
    * Consecutive commands received with HEAD OF QUEUE TAG messages are
    * executed in a last-in-first-out order.
    */
   HEAD_OF_QUEUE,
   /**
    * If accepted, a task having the ACA attribute shall be entered into the task set in the
    * enabled task state. There shall be no more than one ACA task per task set.
    */
   ACA;

}
