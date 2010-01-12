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

import org.apache.log4j.Logger;
import org.jscsi.scsi.lu.LogicalUnit;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.tasks.TaskRouter;
import org.jscsi.scsi.tasks.management.DefaultTaskRouter;
import org.jscsi.scsi.tasks.management.TaskManagementFunction;
import org.jscsi.scsi.tasks.management.TaskServiceResponse;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;

public class DefaultTarget extends AbstractTarget
{
   private static Logger _logger = Logger.getLogger(DefaultTarget.class);

   private boolean running = false;
   private TaskRouter taskRouter;

   public DefaultTarget(String targetName)
   {
      this.setTargetName(targetName);
      this.taskRouter = new DefaultTaskRouter();
   }

   @Override
   public void enqueue(TargetTransportPort port, Command command)
   {
      this.taskRouter.enqueue(port, command);
   }

   public TaskServiceResponse execute(Nexus nexus, TaskManagementFunction function)
   {
      return this.taskRouter.execute(nexus, function);
   }

   public void nexusLost()
   {
      this.taskRouter.nexusLost();
   }

   public void registerLogicalUnit(long lun, LogicalUnit lu)
   {
      this.taskRouter.registerLogicalUnit(lun, lu);
   }

   public LogicalUnit removeLogicalUnit(long lun)
   {
      return this.taskRouter.removeLogicalUnit(lun);
   }

   public synchronized void start()
   {
      _logger.debug("Starting task router for target " + this.getTargetName());
      this.taskRouter.start();
      this.running = true;
   }

   public synchronized void stop()
   {
      _logger.debug("Stopping task router for target " + this.getTargetName());
      this.taskRouter.stop();
   }

   public boolean isRunning()
   {
      return this.running;
   }
}
