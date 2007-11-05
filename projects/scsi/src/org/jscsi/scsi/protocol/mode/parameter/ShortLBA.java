
package org.jscsi.scsi.protocol.mode.parameter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.BufferUnderflowException;

import org.jscsi.scsi.protocol.mode.ModePage;

public class ShortLBA extends ModePage
{
   protected ShortLBA()
   {
      // FIXME: what's the page length? currently passing 0.
      super((byte) -1, 0);
      // TODO Auto-generated constructor stub
   }

   @Override
   protected void decodeModeParameters(int dataLength, DataInputStream inputStream)
         throws BufferUnderflowException, IllegalArgumentException
   {
      // TODO Auto-generated method stub

   }

   @Override
   protected void encodeModeParameters(DataOutputStream output)
   {
      // TODO Auto-generated method stub

   }
}
