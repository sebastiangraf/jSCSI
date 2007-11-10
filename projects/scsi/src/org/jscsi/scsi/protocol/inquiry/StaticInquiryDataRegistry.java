package org.jscsi.scsi.protocol.inquiry;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Encodable;

// TODO: this class must be implemented
public class StaticInquiryDataRegistry extends InquiryDataRegistry
{
   private static Logger _logger = Logger.getLogger(StaticInquiryDataRegistry.class);

   public StaticInquiryDataRegistry()
   {
   }

   @Override
   protected void populateInquiryPages()
   {
      // TODO Auto-generated method stub
      
   }

   @Override
   public <T extends Encodable> T decode(ByteBuffer buffer) throws IOException
   {
      // TODO Auto-generated method stub
      return null;
   }
}
