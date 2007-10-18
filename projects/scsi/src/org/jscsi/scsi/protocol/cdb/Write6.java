package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class Write6 extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x0A;

   private long logicalBlockAddress; // 21-bit LBA
   private int transferLength; // UBYTE_MAX
   
   static
   {
      CommandDescriptorBlockFactory.register(OPERATION_CODE, Write6.class);
   }
   
   protected Write6()
   {
      super();
   }
   
   public Write6(long logicalBlockAddress, long transferLength, boolean linked, boolean normalACA)
   {
      super(linked, normalACA);
      if ( transferLength > 256 )
      {
         throw new IllegalArgumentException("Transfer length out of bounds for command type");
      }
      if ( logicalBlockAddress > 2097152 )
      {
         throw new IllegalArgumentException("Logical Block Address out of bounds for command type");
      }
      this.logicalBlockAddress = logicalBlockAddress;
      this.transferLength = (int)transferLength;
   }
   
   public Write6(long logicalBlockAddress, long transferLength)
   {
      this(logicalBlockAddress, transferLength, false, false);
   }

   public void decode(ByteBuffer input) throws BufferUnderflowException, IOException
   {
      byte[] cdb = new byte[this.size() - 1];
      input.get(cdb);
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(cdb));
      
      long msb = in.readUnsignedByte() & 0x1F;
      long lss = in.readUnsignedShort();
      this.logicalBlockAddress = (msb >>> 32) | lss;
      
      this.transferLength = in.readUnsignedByte();
      if ( this.transferLength == 0 )
      {
         this.transferLength = 256;
      }
      super.setControl(in.readUnsignedByte());

   }

   @Override
   public void encode(ByteBuffer output) throws BufferOverflowException
   {
      ByteArrayOutputStream cdb = new ByteArrayOutputStream(this.size());
      DataOutputStream out = new DataOutputStream(cdb);
      
      try
      {
         out.writeByte(OPERATION_CODE);
         
         int msb = (int)(this.logicalBlockAddress << 32) & 0x1F;
         int lss = (int)this.logicalBlockAddress & 0xFFFF;
         out.writeByte(msb);
         out.writeShort(lss);
         if ( this.transferLength == 256 )
         {
            out.writeByte(0);
         }
         else
         {
            out.writeByte(this.transferLength);
         }
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
      return this.logicalBlockAddress;
   }

   @Override
   public int getOperationCode()
   {
      return OPERATION_CODE;
   }

   @Override
   public long getTransferLength()
   {
      return this.transferLength;
   }

   @Override
   public int size()
   {
      return 6;
   }
}
