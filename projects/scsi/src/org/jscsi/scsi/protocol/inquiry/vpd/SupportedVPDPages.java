package org.jscsi.scsi.protocol.inquiry.vpd;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Encodable;

public class SupportedVPDPages extends VPDPage
{
   @Override
   public void decode(byte[] header, ByteBuffer buffer) throws IOException
   {
      this.decode(buffer);
   }

   @Override
   public byte[] encode() throws BufferOverflowException
   {
      // TODO Auto-generated method stub
      return null;
   }

   public <T extends Encodable> T decode(ByteBuffer buffer) throws IOException
   {
      // TODO Auto-generated method stub
      return null;
   }

}
