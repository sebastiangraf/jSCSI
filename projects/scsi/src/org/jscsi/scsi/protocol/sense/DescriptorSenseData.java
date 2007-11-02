
package org.jscsi.scsi.protocol.sense;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException.ResponseCode;


// TODO: Describe class or interface
public class DescriptorSenseData extends SenseData
{
   public DescriptorSenseData()
   {
      super();
   }

   @Override
   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void decodeSenseKeySpecific(SenseKeySpecificField field) throws IOException
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public byte[] encode()
   {
      // TODO Auto-generated method stub
      return null;
   }



   
}


