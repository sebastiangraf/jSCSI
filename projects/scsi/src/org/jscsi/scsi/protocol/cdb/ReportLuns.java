
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class ReportLuns extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0xA0;

   private int selectReport;
   private int allocationLength;

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

   @Override
   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      in.readByte(); // RESERVED block
      this.selectReport = in.readUnsignedByte();
      in.readByte(); // RESERVED block
      in.readByte(); // RESERVED block
      in.readByte(); // RESERVED block
      this.allocationLength = in.readInt();
      in.readByte(); // RESERVED block
      super.setControl(in.readUnsignedByte());

      if (operationCode != OPERATION_CODE)
      {
         throw new IOException("Invalid operation code: "
               + Integer.toHexString(operationCode));
      }
   }

   @Override
   public byte[] encode()
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

         return cdb.toByteArray();
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

   public int getSelectReport()
   {
      return selectReport;
   }

   public void setSelectReport(int selectReport)
   {
      this.selectReport = selectReport;
   }

   public void setAllocationLength(int allocationLength)
   {
      this.allocationLength = allocationLength;
   }
}
