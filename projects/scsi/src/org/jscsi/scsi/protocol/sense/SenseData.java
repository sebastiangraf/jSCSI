
package org.jscsi.scsi.protocol.sense;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.sense.additional.ActualRetryCount;
import org.jscsi.scsi.protocol.sense.additional.FieldPointer;
import org.jscsi.scsi.protocol.sense.additional.NoSenseKeySpecific;
import org.jscsi.scsi.protocol.sense.additional.ProgressIndication;
import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException.ResponseCode;

// TODO: Describe class or interface
public abstract class SenseData
{
   private SenseException.ResponseCode responseCode;
   
   /*
    * We ignore the following bits (often because we only support SBC-2 type targets:
    * 
    * - FILEMARK
    * - EOM
    * - ILI          (don't currently support READ LONG, WRITE LONG commands
    */
   
   
   private byte[] information;                     // if null VALID set to zero
   private byte[] commandSpecificInformation;
   private SenseKeySpecificField senseKeySpecific;
   
   private KCQ kcq;
   
   
   
   
   public byte getResponseCode()
   {
      return this.responseCode.code();
   }
   
   private boolean isFixed()
   {
      switch(this.responseCode)
      {
         case CURRENT_FIXED:
            return true;
         case DEFERRED_FIXED:
            return true;
         case CURRENT_DESCRIPTOR:
            return false;
         case DEFERRED_DESCRIPTOR:
            return false;
      }
      return false;
   }
   
   public boolean isCurrent()
   {
      switch (this.responseCode)
      {
         case CURRENT_FIXED:
            return true;
         case CURRENT_DESCRIPTOR:
            return true;
         case DEFERRED_FIXED:
            return false;
         case DEFERRED_DESCRIPTOR:
            return false;
      }
      return false;
   }
   
   public boolean isDeferred()
   {
      return ! isCurrent();
   }
   
   
   public KCQ getKCQ()
   {
      return this.kcq;
   }

   public SenseKey getSenseKey()
   {
      return this.kcq.key();
   }
   
   public byte[] getInformation()
   {
      return information;
   }

   public byte[] getCommandSpecificInformation()
   {
      return commandSpecificInformation;
   }

   public SenseKeySpecificField getSenseKeySpecific()
   {
      return senseKeySpecific;
   }

   public abstract void encode( ByteBuffer output ) throws BufferOverflowException;
   
   
   protected abstract void decode(boolean valid, ByteBuffer input) 
         throws BufferUnderflowException, IOException;
   
   
   protected SenseData(ResponseCode responseCode)
   {
      this.responseCode = responseCode;
   }
   
   
   protected static SenseKeySpecificField decodeSenseKeySpecificField(
         SenseKey key,
         DataInputStream input ) throws IOException
   {
      SenseKeySpecificField field;
      switch (key)
      {
         case NO_SENSE:
            field = new ProgressIndication();
         case RECOVERED_ERROR:
            field = new ActualRetryCount();
         case NOT_READY:
            field = new ProgressIndication();
         case MEDIUM_ERROR:
            field = new ActualRetryCount();
         case HARDWARE_ERROR:
            field = new ActualRetryCount();
         case ILLEGAL_REQUEST:
            field = new FieldPointer();
         case COPY_ABORTED:
            field = new NoSenseKeySpecific();  // TODO: decode segment pointer, not supported now
         default:
            field = new NoSenseKeySpecific();
      }
      field.decode(input);
      return field;
   }
   
   
   public static SenseData decode(ByteBuffer input) throws BufferUnderflowException, IOException
   {
      byte[] header = new byte[1];
      input.get(header);
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(header));
      
      int b1 = in.readUnsignedByte();
      ResponseCode code = ResponseCode.valueOf( (byte)(b1 & 0x7F) ); // throws IOException
      boolean valid = ((b1 >>> 7) & 0x01) == 1;
      
      SenseData sense = null;
      
      switch (code)
      {
         case CURRENT_FIXED:
            sense = new FixedSenseData(ResponseCode.CURRENT_FIXED);
         case CURRENT_DESCRIPTOR:
            sense = new DescriptorSenseData(ResponseCode.CURRENT_DESCRIPTOR);
         case DEFERRED_FIXED:
            sense = new FixedSenseData(ResponseCode.DEFERRED_FIXED);
         case DEFERRED_DESCRIPTOR:
            sense = new DescriptorSenseData(ResponseCode.DEFERRED_DESCRIPTOR);
      }
      
      sense.decode(valid, input);
      return sense;
   }
   
   
   
   
}


