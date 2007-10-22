
package org.jscsi.scsi.protocol.mode.parameter;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.mode.ModePage;

public class LongLBA extends ModePage
{
   protected LongLBA()
   {
      super((byte) -1);
      // TODO Auto-generated constructor stub
   }

   @Override
   protected void decodeModeParameters(int dataLength, ByteBuffer input)
         throws BufferUnderflowException, IllegalArgumentException
   {
      // TODO Auto-generated method stub

   }

   @Override
   protected void encodeModeParameters(ByteBuffer output)
   {
      // TODO Auto-generated method stub

   }

   @Override
   protected int getPageLength()
   {
      // TODO Auto-generated method stub
      return 0;
   }
}
