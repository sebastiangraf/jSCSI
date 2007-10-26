
package org.jscsi.scsi.protocol.sense.additional;
import java.io.IOException;
import java.nio.ByteBuffer;


// TODO: Describe class or interface
public class ActualRetryCount implements SenseKeySpecificField
{
   private int actualRetryCount;  // USHORT_MAX
   
   public ActualRetryCount()
   {
      this.actualRetryCount = -1;
   }
   
   public ActualRetryCount( int actualRetryCount )
   {
      this.actualRetryCount = actualRetryCount;
   }

   public int getActualRetryCount()
   {
      return actualRetryCount;
   }

   public void decode(byte[] header, ByteBuffer buffer) throws IOException
   {
      ActualRetryCount actualRetryAccount = decode(buffer);
      this.actualRetryCount = actualRetryAccount.getActualRetryCount();
   }

   public byte[] encode()
   {
      byte[] encodedData = new byte[3];
      
      encodedData[0] = (byte) 0x80;
      
      encodedData[1] = (byte) ((this.actualRetryCount >> 8) & 0xFF);
      
      encodedData[2] = (byte) (this.actualRetryCount & 0xFF);
      
      return encodedData;
   }

   public ActualRetryCount decode(ByteBuffer buffer) throws IOException
   {
      byte[] encodedData = new byte[3];
      
      buffer.get(encodedData);
      
      int retryCount = encodedData[2]; // 8 LSBs
      retryCount |= (encodedData[1] << 8);
      
      return new ActualRetryCount(retryCount);
   }

}


