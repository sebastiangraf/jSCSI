
package org.jscsi.scsi.protocol.sense.additional;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
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
   
   

}


