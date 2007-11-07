
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
