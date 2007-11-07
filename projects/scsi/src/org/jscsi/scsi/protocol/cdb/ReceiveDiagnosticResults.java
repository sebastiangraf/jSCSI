
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class ReceiveDiagnosticResults extends AbstractParameterCDB
{
   public static final int OPERATION_CODE = 0x1C;

   private boolean PCV;
   private int pageCode;

   protected ReceiveDiagnosticResults()
   {
      super(OPERATION_CODE);
   }

   public ReceiveDiagnosticResults(
         boolean pcv,
         int pageCode,
         boolean linked,
         boolean normalACA,
         int allocationLength)
   {
      super(OPERATION_CODE, linked, normalACA, allocationLength, 0);

      this.PCV = pcv;
      this.pageCode = pageCode;
   }

   public ReceiveDiagnosticResults(boolean pcv, int pageCode, int allocationLength)
   {
      this(pcv, pageCode, false, false, allocationLength);
   }

   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      this.PCV = (in.readUnsignedByte() & 1) == 1;
      this.pageCode = in.readUnsignedByte();
      setAllocationLength(in.readUnsignedShort());
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
         out.writeByte((this.PCV == true) ? 1 : 0);
         out.writeByte(this.pageCode);
         out.writeShort((int) getAllocationLength());
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
      return 6;
   }

   public boolean isPCV()
   {
      return this.PCV;
   }

   public void setPCV(boolean pcv)
   {
      this.PCV = pcv;
   }

   public int getPageCode()
   {
      return this.pageCode;
   }

   public void setPageCode(int pageCode)
   {
      this.pageCode = pageCode;
   }
}
