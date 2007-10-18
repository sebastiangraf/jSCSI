
package org.jscsi.scsi.protocol.cdb;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class Read32 extends AbstractCommandDescriptorBlock
{
   @Override
   public void decode(ByteBuffer input) throws BufferUnderflowException, IOException
   {
      // TODO Auto-generated method stub
   }

   @Override
   public void encode(ByteBuffer output) throws BufferOverflowException
   {
      // TODO Auto-generated method stub
   }

   @Override
   public long getAllocationLength()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public long getLogicalBlockAddress()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public int getOperationCode()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   @Override
   public long getTransferLength()
   {
      return 0;
   }

   @Override
   public int size()
   {
      return 32;
   }
}
