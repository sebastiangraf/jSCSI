package org.jscsi.scsi.protocol.inquiry.vpd;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

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

   }



   @Override
   public byte[] encode() throws BufferOverflowException
   {

      return null;
   }


   

}
