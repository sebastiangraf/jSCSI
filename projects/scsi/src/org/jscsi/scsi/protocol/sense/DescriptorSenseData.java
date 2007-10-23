
package org.jscsi.scsi.protocol.sense;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException.ResponseCode;


// TODO: Describe class or interface
public class DescriptorSenseData extends SenseData
{
   public DescriptorSenseData(ResponseCode code)
   {
      super(code);
   }

   @Override
   protected void decode(boolean valid, ByteBuffer input) throws BufferUnderflowException,
         IOException
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void decodeSenseKeySpecific(SenseKeySpecificField field) throws IOException
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public ByteBuffer encode()
   {
      // TODO Auto-generated method stub
      return null;
   }

   
}


