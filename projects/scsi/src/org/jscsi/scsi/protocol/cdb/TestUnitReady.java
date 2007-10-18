
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

// TODO: Describe class or interface
public class TestUnitReady extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x00;

   static
   {
      CommandDescriptorBlockFactory.register(OPERATION_CODE, TestUnitReady.class);
   }
      
   public TestUnitReady(boolean linked, boolean normalACA)
   {
      super(linked, normalACA);
   }
   
   public TestUnitReady()
   {
   }
   
   public void decode(ByteBuffer input) throws IllegalArgumentException
   {
      byte[] cdb = new byte[this.size()];
      input.get(cdb);
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(cdb));
      
      try
      {
         int operationCode = in.readUnsignedByte();
         in.readInt();
         super.setControl(in.readUnsignedByte());
         
         if ( operationCode != OPERATION_CODE )
         {
            throw new IllegalArgumentException(
                  "Invalid operation code: " + Integer.toHexString(operationCode));
         }
      }
      catch (IOException e)
      {
         throw new IllegalArgumentException("Error reading input data.");
      }
   }
   
   @Override
   public void encode(ByteBuffer output)
   {
      ByteArrayOutputStream cdb = new ByteArrayOutputStream(this.size());
      DataOutputStream out = new DataOutputStream(cdb);
      
      try
      {
         out.writeByte(OPERATION_CODE);
         out.writeInt(0);
         out.writeByte(super.getControl());
         
         output.put(cdb.toByteArray());
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }

   }

   @Override
   public long getAllocationLength()
   {
      return 0;
   }

   @Override
   public long getLogicalBlockAddress()
   {
      return 0;
   }

   @Override
   public int getOperationCode()
   {
      return OPERATION_CODE;
   }

   @Override
   public long getTransferLength()
   {
      return 0;
   }

   @Override
   public int size()
   {
      return 6;
   }
}
