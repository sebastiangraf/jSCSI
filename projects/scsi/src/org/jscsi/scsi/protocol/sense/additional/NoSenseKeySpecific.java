
package org.jscsi.scsi.protocol.sense.additional;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

// TODO: Describe class or interface
public class NoSenseKeySpecific implements SenseKeySpecificField
{
   
   public NoSenseKeySpecific() {}

   public void decode(DataInputStream input) throws BufferUnderflowException, IOException
   {
      // TODO Auto-generated method stub
      
   }

   public void encode(DataOutputStream output) throws BufferOverflowException
   {
      // TODO Auto-generated method stub
      
   }

}


