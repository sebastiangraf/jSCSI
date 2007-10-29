
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class ReadCapacity10 extends AbstractTransferCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x25;

   private boolean PMI;

   public ReadCapacity10()
   {
      super(OPERATION_CODE);
   }

   public ReadCapacity10(boolean pmi, boolean linked, boolean normalACA, int logicalBlockAddress)
   {
      super(OPERATION_CODE, linked, normalACA, logicalBlockAddress, 0);

      this.PMI = pmi;
   }

   public ReadCapacity10(boolean pmi, int logicalBlockAddress)
   {
      this(pmi, false, false, logicalBlockAddress);
   }

   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      in.readByte();
      setLogicalBlockAddress(in.readInt());
      in.readShort();
      this.PMI = (in.readUnsignedByte() & 1) != 0;
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
         out.writeInt((int) getLogicalBlockAddress());
         out.writeShort(0);
         out.writeByte(this.PMI ? 1 : 0);
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
      return 10;
   }

   public boolean isPMI()
   {
      return this.PMI;
   }

   public void setPMI(boolean pmi)
   {
      this.PMI = pmi;
   }
}
