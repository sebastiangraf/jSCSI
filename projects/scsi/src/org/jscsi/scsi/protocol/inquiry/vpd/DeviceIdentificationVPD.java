package org.jscsi.scsi.protocol.inquiry.vpd;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jscsi.scsi.protocol.Encodable;

public class DeviceIdentificationVPD extends VPDPage
{
   private static Logger _logger = Logger.getLogger(DeviceIdentificationVPD.class);
   
   public static final int PAGE_CODE = 0x83;

   public DeviceIdentificationVPD()
   {
   }
   
   @Override
   public void decode(byte[] header, ByteBuffer buffer) throws IOException
   {
      this.decode(buffer);
   }



   @Override
   public byte[] encode() throws BufferOverflowException
   {

      return null;
   }



   public <T extends Encodable> T decode(ByteBuffer buffer) throws IOException
   {

      return null;
   }
   

}
