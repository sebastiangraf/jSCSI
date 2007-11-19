//
// Cleversafe open-source code header - Version 1.1 - December 1, 2006
//
// Cleversafe Dispersed Storage(TM) is software for secure, private and
// reliable storage of the world's data using information dispersal.
//
// Copyright (C) 2005-2007 Cleversafe, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
// USA.
//
// Contact Information: Cleversafe, 10 W. 35th Street, 16th Floor #84,
// Chicago IL 60616
// email licensing@cleversafe.org
//
// END-OF-HEADER
//-----------------------
// @author: jquigley
//
// Date: Nov 19, 2007
//---------------------

package org.jscsi.scsi.tasks.target;

import java.util.Set;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.tasks.AbstractTask;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.transport.TargetTransportPort;

public abstract class TargetTask extends AbstractTask
{
   private static Logger _logger = Logger.getLogger(TargetTask.class);

   private Set<Long> logicalUnits;
   
   public TargetTask()
   {
      super();
   }
   
   public TargetTask(
         String name,
         Set<Long> logicalUnits,
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      super(name, targetPort, command, modePageRegistry, inquiryDataRegistry);
      this.logicalUnits = logicalUnits;
   }

   /**
    * Executes the task operation.
    */
   protected abstract void execute() throws InterruptedException, SenseException;

   protected final Task load(
         String name,
         Set<Long> logicalUnits,
         TargetTransportPort targetPort,
         Command command,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      super.load(name, targetPort, command, modePageRegistry, inquiryDataRegistry);
      return this;
   }
   
   
   protected final Set<Long> getLogicalUnits()
   {
      return this.logicalUnits;
   }
   
   
   
}


