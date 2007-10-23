
package org.jscsi.scsi.protocol.sense.additional;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;


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

   public void decode(DataInputStream input) throws BufferUnderflowException, IOException
   {
      // TODO Auto-generated method stub
      
   }

   public void encode(DataOutputStream output) throws BufferOverflowException
   {
      // TODO Auto-generated method stub
      
   }

}


