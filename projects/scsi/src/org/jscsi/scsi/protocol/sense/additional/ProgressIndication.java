
package org.jscsi.scsi.protocol.sense.additional;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;


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

   public void decode(DataInputStream input) throws BufferUnderflowException, IOException
   {
      // TODO Auto-generated method stub
      
   }

   public void encode(DataOutputStream output) throws BufferOverflowException
   {
      // TODO Auto-generated method stub
      
   }
   
   


}


