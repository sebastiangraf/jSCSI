
package org.jscsi.scsi.protocol.cdb;

public abstract class AbstractCommandDescriptorBlock implements CommandDescriptorBlock
{
   private int operationCode;
   private boolean linked;
   private boolean normalACA;

   protected AbstractCommandDescriptorBlock(int operationCode)
   {
      this.operationCode = operationCode;
   }

   protected AbstractCommandDescriptorBlock(int operationCode, boolean linked, boolean normalACA)
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
