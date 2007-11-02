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
// @author: mmotwani
//
// Date: Nov 1, 2007
//---------------------

package org.jscsi.scsi.lu;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.tasks.management.TaskManager;

// TODO: Describe class or interface
public abstract class AbstractLogicalUnit implements LogicalUnit
{
   private static Logger _logger = Logger.getLogger(AbstractLogicalUnit.class);

   private TaskFactory taskFactory;
   private TaskManager taskManager;
   private ModePageRegistry modePageRegistry;
   private InquiryDataRegistry inquiryDataRegistry;

   public AbstractLogicalUnit(
         TaskFactory taskFactory,
         TaskManager taskManager,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      this.taskFactory = taskFactory;
      this.taskManager = taskManager;
      this.modePageRegistry = modePageRegistry;
      this.inquiryDataRegistry = inquiryDataRegistry;
   }

   public void startTaskManagerThread()
   {
      Thread thread = new Thread(this.taskManager);
      thread.start();
   }

   public void stopTaskManagerThread()
   {
      this.taskManager.shutdown();
   }

   public TaskFactory getTaskFactory()
   {
      return this.taskFactory;
   }

   public void setTaskFactory(TaskFactory taskFactory)
   {
      this.taskFactory = taskFactory;
   }

   public TaskManager getTaskManager()
   {
      return this.taskManager;
   }

   public void setTaskManager(TaskManager taskManager)
   {
      this.taskManager = taskManager;
   }

   public ModePageRegistry getModePageRegistry()
   {
      return this.modePageRegistry;
   }

   public void setModePageRegistry(ModePageRegistry modePageRegistry)
   {
      this.modePageRegistry = modePageRegistry;
   }

   public InquiryDataRegistry getInquiryDataRegistry()
   {
      return this.inquiryDataRegistry;
   }

   public void setInquiryDataRegistry(InquiryDataRegistry inquiryDataRegistry)
   {
      this.inquiryDataRegistry = inquiryDataRegistry;
   }
}
