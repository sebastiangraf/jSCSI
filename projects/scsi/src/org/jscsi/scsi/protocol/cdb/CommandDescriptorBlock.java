
package org.jscsi.scsi.protocol.cdb;

import org.jscsi.scsi.protocol.Encodable;

// TODO: Describe class or interface
public interface CommandDescriptorBlock extends Encodable
{
   int getOperationCode();

   long getLogicalBlockAddress();

   /**
    * The transfer length, usually in blocks. Zero if the command does not require a transfer length
    * or no data is to be transferred.
    * 
    * @return Transfer length in blocks.
    */
   long getTransferLength();

   /**
    * 
    * @return
    */
   long getAllocationLength();

   boolean isLinked();

   public void setLinked(boolean linked);

   boolean isNormalACA();

   public void setNormalACA(boolean normalACA);

   /**
    * Returns CDB serialization size in bytes.
    */
   int size();
}
