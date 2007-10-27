
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class ReportLuns extends AbstractParameterCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0xA0;

   private int selectReport;

   public ReportLuns()
   {
      super(OPERATION_CODE);
   }

   public ReportLuns(int selectReport, boolean linked, boolean normalACA, int allocationLength)
   {
      super(OPERATION_CODE, linked, normalACA, allocationLength);

      this.selectReport = selectReport;
   }

   public ReportLuns(int selectReport, int allocationLength)
   {
      this(selectReport, false, false, allocationLength);
   }

   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      in.readByte(); // RESERVED block
      this.selectReport = in.readUnsignedByte();
      in.readByte(); // RESERVED block
      in.readByte(); // RESERVED block
      in.readByte(); // RESERVED block
      setAllocationLength(in.readInt());
      in.readByte(); // RESERVED block
      super.setControl(in.readUnsignedByte());

      if (operationCode != OPERATION_CODE)
      {
         throw new IOException("Invalid operation code: " + Integer.toHexString(operationCode));
      }
   }

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
         out.writeInt((int) getAllocationLength());
         out.writeByte(0);
         out.writeByte(super.getControl());

         return cdb.toByteArray();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   public int size()
   {
      return 12;
   }

   public int getSelectReport()
   {
      return this.selectReport;
   }

   public void setSelectReport(int selectReport)
   {
      this.selectReport = selectReport;
   }
}
