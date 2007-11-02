package org.jscsi.scsi.protocol.sense;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Encodable;
import org.jscsi.scsi.protocol.Serializer;
import org.jscsi.scsi.protocol.sense.additional.ActualRetryCount;
import org.jscsi.scsi.protocol.sense.additional.FieldPointer;
import org.jscsi.scsi.protocol.sense.additional.NoSenseKeySpecific;
import org.jscsi.scsi.protocol.sense.additional.ProgressIndication;
import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException;
import org.jscsi.scsi.protocol.sense.exceptions.SenseException.ResponseCode;

public abstract class SenseData implements Encodable
{
   private SenseException.ResponseCode responseCode;
   
   /*
    * We ignore the following bits (often because we only support SBC-2 type targets:
    * 
    * - FILEMARK
    * - EOM
    * - ILI          (don't currently support READ LONG, WRITE LONG commands
    */
   
   private byte[] information;
   private byte[] commandSpecificInformation;
   private SenseKeySpecificField senseKeySpecific;
   
   private KCQ kcq;
   
   public SenseData(
         ResponseCode responseCode,
         KCQ kcq,
         byte[] information,
         byte[] commandSpecificInformation,
         SenseKeySpecificField senseKeySpecific )
   {
      super();
      this.responseCode = responseCode;
      this.information = information;
      this.commandSpecificInformation = commandSpecificInformation;
      this.senseKeySpecific = senseKeySpecific;
      this.kcq = kcq;
   }

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
   
   public boolean isValid()
   {
      return this.information != null;
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
   
   /**
    * Workaround used by testing framework. Should remove once framework can interpret SenseKey
    * object.
    * @deprecated
    */
   public int getSenseKeyValue()
   {
      return this.kcq.key().value();
   }
   
   public int getSenseCode()
   {
      return this.kcq.code();
   }
   
   public int getSenseCodeQualifier()
   {
      return this.kcq.qualifier();
   }
   
   public byte[] getInformation()
   {
      return this.information == null ? new byte[] {0, 0, 0, 0} : this.information;
   }

   public byte[] getCommandSpecificInformation()
   {
      return commandSpecificInformation;
   }

   public SenseKeySpecificField getSenseKeySpecific()
   {
      return senseKeySpecific;
   }
   
   public abstract void decodeSenseKeySpecific( SenseKeySpecificField field ) throws IOException;

   public abstract byte[] encode();
   
   
   public abstract void decode(byte[] header, ByteBuffer input) throws IOException;
   
   
   protected SenseData() {}
   
   
   protected static SenseKeySpecificField decodeSenseKeySpecificField(
         SenseKey key,
         ByteBuffer input ) throws IOException
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

   protected void setResponseCode(SenseException.ResponseCode responseCode)
   {
      this.responseCode = responseCode;
   }

   protected void setInformation(byte[] information)
   {
      this.information = information;
   }

   protected void setCommandSpecificInformation(byte[] commandSpecificInformation)
   {
      this.commandSpecificInformation = commandSpecificInformation;
   }

   protected void setKcq(KCQ kcq)
   {
      this.kcq = kcq;
   }

}
