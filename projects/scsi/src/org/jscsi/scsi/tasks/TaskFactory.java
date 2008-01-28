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

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.transport.TargetTransportPort;

/**
 * A implementation specific factory which creates tasks capable of executing SCSI commands
 * on a particular logical unit.
 */
public interface TaskFactory
{
   /**
    * Returns a task which can execute the indicated command.
    * 
    * @param port The transport port where the command originated.
    * @param command The incoming command.
    * @return A task object which can execute the indicated command.
    * @throws IllegalRequestException If there is no task which can execute the indicated
    *    command. This means an illegal request has been sent from the initiator.
    */
   Task getInstance(TargetTransportPort port, Command command) throws IllegalRequestException;

   /**
    * Returns a boolean indicating whether this factory responds to a particular Task class.
    * 
    * @param cls The command class we're inspecting.
    * @return boolean True if this factory supports the Task class, false otherwise.
    */
   boolean respondsTo(Class<? extends CDB> cls);
}
