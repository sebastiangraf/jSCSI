
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Inquiry extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x12;

   private boolean evpd;
   private int pageCode;
   private int allocationLength;

   static
   {
      
   }
   
   public Inquiry()
   {
      super();
   }
   
   public Inquiry(boolean evpd, byte pageCode, short allocationLength, boolean linked, boolean normalACA)
   {
      super(linked, normalACA);

      this.evpd = evpd;
      this.pageCode = pageCode;
      this.allocationLength = allocationLength;
   }
   
   public Inquiry(boolean evpd, byte pageCode, short allocationLength)
   {
      this(evpd, pageCode, allocationLength, false, false);
   }
   
   public void decode(ByteBuffer input) throws IllegalArgumentException
   {
      byte[] cdb = new byte[this.size()];
      input.get(cdb);
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(cdb));
      
      try
      {
         int operationCode = in.readUnsignedByte();
         this.evpd = (in.readUnsignedByte() & 0x01) == 0x01;
         this.pageCode = in.readUnsignedByte();
         this.allocationLength = in.readShort();
         super.setControl(in.readUnsignedByte());

         if ( operationCode != OPERATION_CODE )
         {
            throw new IllegalArgumentException("Invalid operation code: " + Integer.toHexString(operationCode));
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
         out.writeByte(this.evpd ? 0x01 : 0x00);
         out.writeByte(this.pageCode);
         out.writeShort(this.allocationLength);
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
