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
// Date: Nov 2, 2007
//---------------------

package org.jscsi.scsi.lu.file;

import org.jscsi.scsi.lu.DefaultLogicalUnit;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.inquiry.StaticInquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.tasks.file.FileDevice;
import org.jscsi.scsi.tasks.file.FileTaskFactory;
import org.jscsi.scsi.tasks.management.DefaultTaskManager;

// TODO: Describe class or interface
public class FileLogicalUnit extends DefaultLogicalUnit
{

   private static int NUM_TASK_THREADS = 1;

   private FileDevice device;

   public FileLogicalUnit(FileDevice device)
   {
      this(device, new StaticInquiryDataRegistry());
   }

   public FileLogicalUnit(FileDevice device, InquiryDataRegistry inquiryDataRegistry)
   {
      super(new DefaultTaskManager(NUM_TASK_THREADS), inquiryDataRegistry);
      this.device = device;
   }

   public void setModePageRegistry(ModePageRegistry modePageRegistry)
   {
      setTaskFactory(new FileTaskFactory(device, modePageRegistry, getInquiryDataRegistry()));
   }
}
