
package org.jscsi.scsi.protocol.mode;


public abstract class ProtocolSpecificLogicalUnit extends ModePage
{
   public static final byte PAGE_CODE = 0x18;

   public ProtocolSpecificLogicalUnit(int subPageCode, int pageLength)
   {
      super(PAGE_CODE, subPageCode, pageLength);
   }
}
