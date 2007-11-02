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

package org.jscsi.scsi.lu.file;

import org.apache.log4j.Logger;
import org.jscsi.scsi.exceptions.TaskSetException;
import org.jscsi.scsi.lu.AbstractLogicalUnit;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.protocol.sense.exceptions.IllegalRequestException;
import org.jscsi.scsi.tasks.Task;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.tasks.management.GeneralTaskManager;
import org.jscsi.scsi.transport.TargetTransportPort;

// TODO: Describe class or interface
public class FileLogicalUnit extends AbstractLogicalUnit
{
   private static Logger _logger = Logger.getLogger(FileLogicalUnit.class);

   private static int NUM_TASK_THREADS = 1;

   public FileLogicalUnit(
         TaskFactory taskFactory,
         ModePageRegistry modePageRegistry,
         InquiryDataRegistry inquiryDataRegistry)
   {
      super(taskFactory, new GeneralTaskManager(NUM_TASK_THREADS), modePageRegistry,
            inquiryDataRegistry);

      Thread thread = new Thread(getTaskManager());
      thread.start();
   }

   public void stopTaskManagerThread()
   {
      getTaskManager().shutdown();
   }

   public void enqueue(TargetTransportPort port, Command command)
   {
      Task task = null;

      try
      {
         task =
               getTaskFactory().getInstance(port, command, getModePageRegistry(),
                     getInquiryDataRegistry());
      }
      catch (IllegalRequestException e)
      {
         // FIXME: handle/throw
      }

      //TODO: check task for null

      try
      {
         getTaskManager().submitTask(task);
      }
      catch (TaskSetException e)
      {
         // FIXME: handle/throw
      }

   }
}
