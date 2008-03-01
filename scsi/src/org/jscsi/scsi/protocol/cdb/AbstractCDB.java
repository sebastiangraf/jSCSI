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

package org.jscsi.scsi.protocol.cdb;

public abstract class AbstractCDB implements CDB
{
   private int operationCode;
   private boolean linked;
   private boolean normalACA;

   protected AbstractCDB(int operationCode)
   {
      this.operationCode = operationCode;
   }

   protected AbstractCDB(int operationCode, boolean linked, boolean normalACA)
   {
      this.operationCode = operationCode;
      this.linked = linked;
      this.normalACA = normalACA;
   }

   protected void setControl(int control)
   {
      this.normalACA = (control & 0x04) > 0;
      this.linked = (control & 0x01) > 0;
   }

   protected int getControl()
   {
      int control = 0;
      control |= this.linked ? 0x01 : 0x00;
      control |= this.normalACA ? 0x04 : 0x00;
      return control;
   }

   public boolean isLinked()
   {
      return this.linked;
   }

   public boolean isNormalACA()
   {
      return this.normalACA;
   }

   public void setLinked(boolean linked)
   {
      this.linked = linked;
   }

   public void setNormalACA(boolean normalACA)
   {
      this.normalACA = normalACA;
   }

   public int getOperationCode()
   {
      return this.operationCode;
   }
}
