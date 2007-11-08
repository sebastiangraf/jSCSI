
package org.jscsi.scsi.protocol.sense;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException.ResponseCode;


// TODO: Describe class or interface
public class DescriptorSenseData extends SenseData
{
   /**
    * Constructs a descriptor format sense data with 
    */
   public DescriptorSenseData()
   {
      super(
            ResponseCode.valueOf(true, true),
            KCQ.NO_ERROR,
            null,
            null,
            null );
   }

   @Override
   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      throw new RuntimeException("not implemented");
   }

   @Override
   public void decodeSenseKeySpecific(SenseKeySpecificField field) throws IOException
   {
      throw new RuntimeException("not implemented");
   }

   @Override
   public byte[] encode()
   {
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(bs);
      
      try
      { 
         out.writeByte(this.getResponseCode() & 0x7F); // RESPONSE CODE (wipe Reserved bit)
         out.writeByte(this.getSenseKey().value());
         out.writeByte(this.getSenseCode());
         out.writeByte(this.getSenseCodeQualifier());
         out.writeInt(0); // 3-byte reserved field, ADDITIONAL SENSE LENGTH = 0 
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode descriptor format sense data");
      }
      
      return bs.toByteArray();
   }



   
}


