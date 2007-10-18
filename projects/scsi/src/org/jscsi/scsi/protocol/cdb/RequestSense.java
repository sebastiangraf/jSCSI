package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class RequestSense extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x03;
   
   private int allocationLength; // Limited to UBYTE_MAX
   private boolean descriptorFormat;
   
   static
   {
      CommandDescriptorBlockFactory.register(OPERATION_CODE, RequestSense.class);
   }
   
   public RequestSense()
   {
      super();
   }

   public RequestSense(
         long allocationLength,
         boolean useDescriptorFormat,
         boolean linked,
         boolean normalACA )
   {
      super(linked, normalACA);
      if ( allocationLength > 256 )
      {
         throw new IllegalArgumentException("Allocation length out of bounds for command type");
      }
      this.allocationLength = (int)allocationLength;
      this.descriptorFormat = useDescriptorFormat;
   }
   
   public RequestSense(long allocationLength, boolean useDescriptorFormat)
   {
      this(allocationLength, useDescriptorFormat, false, false);
   }
   
   
   public void decode(ByteBuffer input)
         throws BufferUnderflowException, IllegalArgumentException
   {
      byte[] cdb = new byte[this.size()];
      input.get(cdb);
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(cdb));
      
      try
      {
         int operationCode = in.readUnsignedByte();
         int format = in.readUnsignedByte() & 0x01;
         this.descriptorFormat = (format == 1);
         in.readShort();
         this.allocationLength = in.readUnsignedByte();
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
         if ( this.descriptorFormat )
         {
            out.writeByte(1);
         }
         else
         {
            out.writeByte(0);
         }
         out.writeShort(0);
         out.writeByte(this.allocationLength);
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
      return this.allocationLength;
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
