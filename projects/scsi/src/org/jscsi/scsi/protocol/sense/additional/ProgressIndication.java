
package org.jscsi.scsi.protocol.sense.additional;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
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
   
   


}


