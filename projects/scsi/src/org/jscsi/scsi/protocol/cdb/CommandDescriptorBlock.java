
package org.jscsi.scsi.protocol.cdb;

import org.jscsi.scsi.protocol.Encodable;

// TODO: Describe class or interface
public interface CommandDescriptorBlock extends Encodable
{
   int getOperationCode();

   boolean isLinked();

   public void setLinked(boolean linked);

   boolean isNormalACA();

   public void setNormalACA(boolean normalACA);

   /**
    * Returns CDB serialization size in bytes.
    */
   int size();
}
