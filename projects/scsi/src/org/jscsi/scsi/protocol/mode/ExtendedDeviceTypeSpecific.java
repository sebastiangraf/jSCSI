
package org.jscsi.scsi.protocol.mode;


public abstract class ExtendedDeviceTypeSpecific extends ModePage
{
   public static final byte PAGE_CODE = 0x16;

   public ExtendedDeviceTypeSpecific(int subPageCode, int pageLength)
   {
      super(PAGE_CODE, subPageCode, pageLength);
   }
}
