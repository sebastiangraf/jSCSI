
package org.jscsi.scsi.protocol.sense.additional;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Encodable;

// TODO: Describe class or interface
public class NoSenseKeySpecific implements SenseKeySpecificField
{
   private static byte[] readData = new byte[3];
   
   public NoSenseKeySpecific() {}

   public NoSenseKeySpecific decode(ByteBuffer buffer) throws IOException
   {
      buffer.get(readData);
      return new NoSenseKeySpecific();
   }

   public void decode(byte[] header, ByteBuffer buffer) throws IOException
   {
      decode(buffer);
   }

   public byte[] encode()
   {
      return new byte[3];
   }

}


