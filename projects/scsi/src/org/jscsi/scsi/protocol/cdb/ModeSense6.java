
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ModeSense6 extends AbstractCommandDescriptorBlock
{
   private static final int OPERATION_CODE = 0x1A;
      
   public static final int PC_CURRENT_VALUES = 0x00;
   public static final int PC_CHANGEABLE_VALUES = 0x01;
   public static final int PC_DEFAULT_VALUES = 0x03;
   public static final int PC_SAVED_VALUES = 0x04;
   
   private boolean dbd;
   private int pageControl;
   private int pageCode;
   private int subPageCode;
   private long allocationLength;
   
   static
   {
      CommandDescriptorBlockFactory.register(OPERATION_CODE, ModeSense6.class);
   }
   
   public ModeSense6()
   {
      super();
   }
   
   public ModeSense6(boolean dbd, int pageControl, int pageCode, int subPageCode, long allocationLength, boolean linked, boolean normalACA)
   {
      super(linked, normalACA);
      
      if ( allocationLength > 65536 )
      {
         throw new IllegalArgumentException("Allocation length out of bounds for command type");
      }

      this.dbd = dbd;
      this.pageControl = pageControl;
      this.pageCode = pageCode;
      this.subPageCode = subPageCode;
      this.allocationLength = (int)allocationLength;
   }
   
   public ModeSense6(boolean dbd, int pageControl, int pageCode, int subPageCode, long allocationLength)
   {
      this(dbd, pageControl, pageCode, subPageCode, allocationLength, false, false);
   }
   
   public void decode(ByteBuffer input) throws IllegalArgumentException
   {
      byte[] cdb = new byte[this.size()];
      input.get(cdb);
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(cdb));
      int tmp;
      
      try
      {
         int operationCode = in.readUnsignedByte();
         this.dbd = (in.readUnsignedByte() & 0x04) != 0;
         tmp = in.readUnsignedByte();
         this.pageCode = tmp & 0x3F;
         this.pageControl = tmp >>> 6;
         this.subPageCode = in.readUnsignedByte();
         this.allocationLength = (long)in.readUnsignedShort();

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
         out.writeByte(this.dbd ? 0x04 : 0x00);
         out.writeByte(this.pageControl | this.pageCode);
         out.writeByte(this.subPageCode);
         out.writeByte((int)this.allocationLength);
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
