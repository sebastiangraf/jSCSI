
package org.jscsi.scsi.protocol.mode;


public abstract class ExtendedModePage extends ModePage
{
   public static final byte PAGE_CODE = 0x15;

   public ExtendedModePage(int subPageCode, int pageLength)
   {
      super(PAGE_CODE, subPageCode, pageLength);
   }
}
