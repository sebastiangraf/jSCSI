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

import org.jscsi.scsi.protocol.Encodable;

/**
 * A SCSI command descriptor block (CDB). Many CDB objects are divided into "transfer" and
 * "parameter" commands.
 * <p>
 * Transfer commands like WRITE and READ contain a logical block address and a transfer length.
 * The transfer direction is indicated by the command type.
 * <p>
 * Parameter commands like MODE SENSE, MODE SELECT, INQUIRY, and REPORT LUNS contain an allocation
 * length or parameter length. The allocation length is the amount of space allocated by
 * the initiator for return "input" data. The parameter length is the amount of incoming "output"
 * data. The direction of transfer is indicated by which length field is non-zero. For example,
 * a command with non-zero parameter length is sending data from the initiator to the target and
 * the allocation length field will be zero; a command with non-zero allocation length is receiving
 * data from the target to the initiator and the parameter length field will be zero. A command
 * with both zero fields transmits no data.
 * <p>
 * Commands which do not require data transfer such as TEST UNIT READY will implement only this
 * interface.
 */


public interface CDB extends Encodable
{
   
   // TODO: make all methods here public

   /**
    * Returns the operation code associated with this CDB.
    */
   int getOperationCode();

   /**
    * Indicates if this command is part of a linked command chain.
    */
   boolean isLinked();

   /**
    * Sets this command to be part of a linked command chain.
    */
   public void setLinked(boolean linked);

   /**
    * Indicates if a contingent allegiance (CA) or auto contingent allegiance (ACA) condition
    * is established if the command returns with {@link Status#CHECK_CONDITION} status.
    */
   boolean isNormalACA();

   /**
    * Modifies this command to establish an ACA condition if {@link Status#CHECK_CONDITION} status
    * is returned.
    */
   public void setNormalACA(boolean normalACA);

   /**
    * Returns CDB serialization size in bytes.
    */
   int size();
}
