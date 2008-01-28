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

package org.jscsi.scsi.tasks.target;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.ReportLuns;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.protocol.sense.exceptions.InvalidCommandOperationCodeException;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.transport.TargetTransportPort;

public class TargetTaskFactory implements TaskFactory
{
   private static Logger _logger = Logger.getLogger(TargetTaskFactory.class);

   private static Map<Class<? extends CDB>, Class<? extends TargetTask>> tasks =
         new HashMap<Class<? extends CDB>, Class<? extends TargetTask>>();

   private Set<Long> logicalUnits;

   static
   {
      TargetTaskFactory.tasks.put(ReportLuns.class, ReportLunsTask.class);
   }

   public TargetTaskFactory(Set<Long> logicalUnits)
   {
      this.logicalUnits = logicalUnits;
   }

   public Task getInstance(TargetTransportPort port, Command command)
         throws IllegalRequestException
   {
      switch (command.getCommandDescriptorBlock().getOperationCode())
      {
         case ReportLuns.OPERATION_CODE :
            return new ReportLunsTask(logicalUnits, port, command, null, null);
         default :
            _logger.error("Initiator attempted to execute unsupported command: ("
                  + command.getCommandDescriptorBlock().getOperationCode() + ") "
                  + command.getCommandDescriptorBlock().getClass().getName());
            throw new InvalidCommandOperationCodeException();
      }

   }

   public boolean respondsTo(Class<? extends CDB> cls)
   {
      return TargetTaskFactory.tasks.containsKey(cls);
   }
}
