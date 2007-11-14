
package org.jscsi.scsi.protocol.sense.additional;
import java.io.IOException;
import java.nio.ByteBuffer;


// TODO: Describe class or interface
public class ProgressIndication implements SenseKeySpecificField
{
   
   private int progressIndication; // USHORT_MAX
   
   public ProgressIndication()
   {
      this.progressIndication = -1;
   }
   
   public ProgressIndication( int progressIndication )
   {
      this.progressIndication = progressIndication;
   }

   public int getProgressIndication()
   {
      return progressIndication;
   }

   public void decode(byte[] header, ByteBuffer buffer) throws IOException
   {
      decode(buffer);
   }

   public byte[] encode()
   {
      byte[] encodedData = new byte[3];

      encodedData[0] = (byte) 0x80;
      encodedData[1] = (byte) ((this.progressIndication >>> 8) & 0xFF);
      encodedData[2] = (byte) (this.progressIndication & 0xFF);

      return encodedData;
   }

   @SuppressWarnings("unchecked")
   public ProgressIndication decode(ByteBuffer buffer) throws IOException
   {
      byte[] encodedData = new byte[3];
      buffer.get(encodedData);

      this.progressIndication = (encodedData[2] & 0xFF); // 8 LSBs
      this.progressIndication |= ((encodedData[1] & 0xFF) << 8);

      return this;
   }
}


