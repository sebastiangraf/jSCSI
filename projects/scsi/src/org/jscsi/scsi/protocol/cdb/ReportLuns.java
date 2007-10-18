
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ReportLuns extends AbstractCommandDescriptorBlock
{
   private static final int OPERATION_CODE = 0xA0;
   
   private int selectReport;
   private int allocationLength;
   
   static
   {
      CommandDescriptorBlockFactory.register(OPERATION_CODE, ReportLuns.class);
   }
   
   public ReportLuns()
   {
      super();
   }
   
   public ReportLuns(int selectReport, int allocationLength, boolean linked, boolean normalACA)
   {
      super(linked, normalACA);
      
      this.selectReport = selectReport;
      this.allocationLength = allocationLength;
   }
   
   public ReportLuns(int selectReport, int allocationLength)
   {
      this(selectReport, allocationLength, false, false);
   }
   
   public void decode(ByteBuffer input) throws IllegalArgumentException
   {
      byte[] cdb = new byte[this.size()];
      input.get(cdb);
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(cdb));
      
      try
      {
         int operationCode = in.readUnsignedByte();
         in.readByte(); // RESERVED block
         this.selectReport = in.readUnsignedByte();
         in.readByte(); // RESERVED block
         in.readByte(); // RESERVED block
         in.readByte(); // RESERVED block
         this.allocationLength = in.readInt();
         in.readByte(); // RESERVED block
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
         out.writeByte(0);
         out.writeByte(this.selectReport);
         out.writeByte(0);
         out.writeByte(0);
         out.writeByte(0);
         out.writeInt(this.allocationLength);
         out.writeByte(0);
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
      return 12;
   }
}
