
package org.jscsi.scsi.protocol.mode;

import java.nio.ByteBuffer;

public abstract class ExtendedDeviceTypeSpecific extends ModePage
{
   public static final byte PAGE_CODE = 0x16;

   private int PAGE_LENGTH;

   public ExtendedDeviceTypeSpecific(int subPageCode, int pageLength)
   {
      super(PAGE_CODE, subPageCode);
      PAGE_LENGTH = pageLength;
   }

   @Override
   abstract protected void decodeModeParameters(int dataLength, ByteBuffer input);

   @Override
   abstract protected void encodeModeParameters(ByteBuffer output);

   @Override
   protected int getPageLength()
   {
      return PAGE_LENGTH;
   }
}
