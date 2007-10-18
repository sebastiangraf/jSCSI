
package org.jscsi.scsi.protocol.cdb;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;


// TODO: Describe class or interface
public abstract class AbstractCommandDescriptorBlock implements CommandDescriptorBlock
{
   
   private boolean linked;
   private boolean normalACA;
   
   protected AbstractCommandDescriptorBlock()
   {
      linked = false;
      normalACA = false;
   }
   
   protected AbstractCommandDescriptorBlock(boolean linked, boolean normalACA)
   {
      this.linked = linked;
      this.normalACA = normalACA;
   }

   protected void setControl(int control)
   {
      // TODO: Implement this
   }
   
   protected int getControl()
   {
      // TODO: Implement this
      return 0;
   }


   public boolean isLinked()
   {
      return this.linked;
   }

   public boolean isNormalACA()
   {
      return this.normalACA;
   }

   public abstract void decode(ByteBuffer input) throws BufferUnderflowException, IOException;
   public abstract void encode(ByteBuffer output) throws BufferOverflowException;
   public abstract long getAllocationLength();
   public abstract long getLogicalBlockAddress();
   public abstract int getOperationCode();
   public abstract long getTransferLength();
   public abstract int size();
   
   


}


