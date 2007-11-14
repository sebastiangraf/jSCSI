
package org.jscsi.scsi.protocol.sense.additional;
import java.io.IOException;
import java.nio.ByteBuffer;

// TODO: Describe class or interface
public class NoSenseKeySpecific implements SenseKeySpecificField
{
   private static byte[] readData = new byte[3];
   
   public NoSenseKeySpecific() {}

   @SuppressWarnings("unchecked")
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


