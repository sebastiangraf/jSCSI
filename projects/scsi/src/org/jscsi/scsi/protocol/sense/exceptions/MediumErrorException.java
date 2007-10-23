package org.jscsi.scsi.protocol.sense.exceptions;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.additional.ActualRetryCount;
import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;

// TODO: Describe class or interface
public abstract class MediumErrorException extends SenseException
{
   public MediumErrorException(
         KCQ kcq,
         boolean current )
   {
      super( kcq, current );
   }
   
   protected abstract int getActualRetryCount();

   protected abstract long getLogicalBlockAddress();
   
   @Override
   protected byte[] getInformation()
   {
      /*
       * INFORMATION field is populated with LBA for commands which access the medium. This
       * method should be overwritten by an empty method for those errors not returning an LBA.
       * Note that addresses less than UINT_MAX will be encoded as 4 bytes, otherwise 8 bytes
       * will be returned.
       */
      
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream( bs );
      
      long lba = this.getLogicalBlockAddress();
      
      if ( lba > 0xFFFFFFFFL )
      {
         // greater than UINT_MAX, encode as 8-byte value
         out.writeLong( lba );
         out.close();
      }
      else if ( lba > 0x7FFFFFFL )
      {
         // will be a negative signed int
         int l = (int)(-(lba - 0x8000000L));
         out.writeInt(l);
      }
      else
      {
         out.writeInt((int)lba);
      }
      
      try
      {
         out.writeLong(0);
         out.writeLong(lba);
         out.close();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to create serialize exception parameter", e);
      }
      
      this.logicalBlockAddress = bs.toByteArray();
      assert this.logicalBlockAddress.length == 8 : "Invalid length for error information field";
      
      this.actualRetryCount = new ActualRetryCount(actualRetryCount);
   }
   

}


