
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
      // TODO Auto-generated method stub
      return null;
   }

   public void decode(byte[] header, ByteBuffer buffer) throws IOException
   {
      // TODO Auto-generated method stub
      
   }

   public byte[] encode()
   {
      // TODO Auto-generated method stub
      return null;
   }


   
   


}


