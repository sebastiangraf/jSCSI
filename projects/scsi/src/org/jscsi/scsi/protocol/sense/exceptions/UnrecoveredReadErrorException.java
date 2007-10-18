
package org.jscsi.scsi.protocol.sense.exceptions;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.jscsi.scsi.protocol.sense.KCQ;
import org.jscsi.scsi.protocol.sense.additional.ActualRetryCount;
import org.jscsi.scsi.protocol.sense.additional.SenseKeySpecificField;

// TODO: Describe class or interface
public class UnrecoveredReadErrorException extends MediumErrorException
{

   private long LBA;
   private byte[] logicalBlockAddress;
   private ActualRetryCount actualRetryCount;
   
   public UnrecoveredReadErrorException(
         boolean current,
         long logicalBlockAddress,
         int actualRetryCount )
   {
      super(KCQ.UNRECOVERED_READ_ERROR, current);
      assert actualRetryCount < 0xFFFF : "actual retry count field out of range";
      
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream( bs );
      
      this.LBA = logicalBlockAddress;
      
      try
      {
         out.writeLong(0);
         out.writeLong(logicalBlockAddress);
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
   
   
   @Override
   protected int getActualRetryCount()
   {
      return this.actualRetryCount.getActualRetryCount();
   }

   @Override
   protected long getLogicalBlockAddress()
   {
      return this.LBA;
   }


   @Override
   protected byte[] getCommandSpecificInformation()
   {
      return null;
   }

   @Override
   protected byte[] getInformation()
   {
      return this.logicalBlockAddress;
   }


   @Override
   protected SenseKeySpecificField getSenseKeySpecific()
   {
      return this.actualRetryCount;
   }
   
   

}


