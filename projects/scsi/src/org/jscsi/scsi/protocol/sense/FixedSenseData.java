
package org.jscsi.scsi.protocol.sense;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.sense.additional.NoSenseKeySpecific;
import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException.ResponseCode;
import org.jscsi.scsi.protocol.util.ByteBufferInputStream;


// TODO: Describe class or interface
public class FixedSenseData extends SenseData
{
   
   private static final int FIXED_SENSE_DATA_LENGTH = 18;
   private static final int PRE_SIZE_LENGTH = 7; // 8 bytes - 1 pre-parsed byte

   private byte[] senseKeySpecificBuffer = null;
   
   public FixedSenseData() { super(); }

   
   public FixedSenseData(
         boolean current,
         KCQ kcq,
         byte[] information,
         byte[] commandSpecificInformation,
         SenseKeySpecificField senseKeySpecific)
   {
      super(
            ResponseCode.valueOf(current, false), 
            kcq, 
            information, 
            commandSpecificInformation, 
            senseKeySpecific );
   }

   @Override
   public void decodeSenseKeySpecific(SenseKeySpecificField field) 
         throws BufferUnderflowException, IOException
   {
      field.decode(ByteBuffer.wrap(this.senseKeySpecificBuffer));
   }



   @Override
   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      assert header != null && header.length == 1 : "input header is invalid";
      boolean valid = (header[0] & 0x80) != 0;
      this.setResponseCode(ResponseCode.valueOf((byte)(header[0] & 0x7F)));
      
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));
      
      // read in first segment of fixed format sense data
      in.readByte();
      int key = in.readUnsignedByte() & 0x0F; // TODO: FILEMARK, EOM, and ILI are unsupported
      byte[] info = new byte[4];
      in.read(info);
      int length = in.readUnsignedByte() - 10; // length of next segment, minus required read-in
      length = length < 0 ? 0 : length;
      
      // read in the next segment of the fixed format sense data
      byte[] cmdi = new byte[4];
      in.read(cmdi);
      int code = in.readUnsignedByte();
      int qualifier = in.readUnsignedByte();
      in.readByte();
      this.senseKeySpecificBuffer = new byte[3];
      in.read(this.senseKeySpecificBuffer);
      // the rest of the additional sense bytes are ignored
      // (vendor specific bytes not supported)
      in.skip(length);
      
      KCQ kcq = KCQ.valueOf(key, code, qualifier); // throws IOException on invalid values
      
      // Set appropriate fields
      
      this.setKcq(kcq);
      this.setInformation( valid ? info : null );
      this.setCommandSpecificInformation(cmdi);
      
      // sense key specific buffer already set, will be decoded when exception is constructed.
   }



   @Override
   public byte[] encode()
   {
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream( bs );
      
      try
      {
         byte[] info = this.getInformation();
         byte[] cmdi = this.getCommandSpecificInformation();
         KCQ kcq = this.getKCQ();
         SenseKeySpecificField field = this.getSenseKeySpecific();
         if ( field == null )
         {
            field = new NoSenseKeySpecific();
         }

         // returned response code is byte with max value 0x7F (7-bit).
         int response = this.getResponseCode();
         
         // We mark VALID as 0 if info is null or over max size (4-byte).
         if ( info != null && info.length == 4 )
         {
            response |= 0x80;
         }
         else if ( info.length < 4 )
         {
            throw new RuntimeException(
                  "Returned sense information has invalid length: " + info.length );
         }
         else
         {
            info = new byte[4]; // Ignore invalid or null value, will write all zeros to field
         }
         
         if ( cmdi.length != 4 )
         {
            cmdi = new byte[4]; // Ignore invalid command specific information lengths.
         }
         
         out.writeByte(response);  // VALID and RESPONSE CODE
         out.writeByte(0);
         out.writeByte(kcq.key().value()); // TODO: FILEMARK, EOM, and ILI not current supported
         out.write(info);
         out.writeByte(10);    // no "Additional sense bytes" will be written, last byte is #17
         out.write(cmdi);
         out.writeByte(kcq.code());
         out.writeByte(kcq.qualifier());
         out.writeByte(0);
         out.write(field.encode());
         
         assert bs.toByteArray().length == FIXED_SENSE_DATA_LENGTH : "Invalid encoded sense data"; 
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode fixed format sense data.");
      }
      
      return bs.toByteArray();
   }
   
   
   


}


