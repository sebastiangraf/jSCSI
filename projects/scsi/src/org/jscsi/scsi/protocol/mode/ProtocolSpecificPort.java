
package org.jscsi.scsi.protocol.mode;

public abstract class ProtocolSpecificPort extends ModePage
{
   public static final byte PAGE_CODE = 0x18;

   public ProtocolSpecificPort(int subPageCode, int pageLength)
   {
      super(PAGE_CODE, subPageCode, pageLength);
   }
}
