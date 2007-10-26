
package org.jscsi.scsi.protocol.sense.additional;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Encodable;


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

   public ProgressIndication decode(ByteBuffer buffer) throws IOException
   {
      byte[] encodedData = new byte[3];
      
      buffer.get(encodedData);
      
      int progressIndication = encodedData[2]; // 8 LSBs
      progressIndication |= (encodedData[1] << 8);
      
      return new ProgressIndication(progressIndication);
   }

   public void decode(byte[] header, ByteBuffer buffer) throws IOException
   {
      ProgressIndication progressIndication = decode(buffer);
      this.progressIndication = progressIndication.getProgressIndication();
   }

   public byte[] encode()
   {
      byte[] encodedData = new byte[3];
      
      encodedData[0] = (byte) 0x80;
      
      encodedData[1] = (byte) ((this.progressIndication >> 8) & 0xFF);
      
      encodedData[2] = (byte) (this.progressIndication & 0xFF);
      
      return encodedData;
   }


   
   


}


