
package org.jscsi.scsi.protocol.sense.additional;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Encodable;


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
      // TODO Auto-generated method stub
      
   }

   public byte[] encode()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public ActualRetryCount decode(ByteBuffer buffer) throws IOException
   {
      // TODO Auto-generated method stub
      return null;
   }



}


