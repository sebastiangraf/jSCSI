
package org.jscsi.scsi.protocol.cdb;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public abstract class AbstractCommandDescriptorBlock implements CommandDescriptorBlock
{

   private boolean linked;
   private boolean normalACA;

   protected AbstractCommandDescriptorBlock()
   {
   }

   protected AbstractCommandDescriptorBlock(boolean linked, boolean normalACA)
   {
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

   

   public abstract void decode(byte[] header, ByteBuffer buffer) throws IOException;

   public abstract byte[] encode();

   public abstract long getAllocationLength();

   public abstract long getLogicalBlockAddress();

   public abstract int getOperationCode();

   public abstract long getTransferLength();

   public abstract int size();

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
}
