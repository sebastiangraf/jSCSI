
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class ModeSelect10 extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x55;

   private boolean PF;
   private boolean SP;
   private int parameterListLength;

   protected ModeSelect10()
   {
      super(OPERATION_CODE);
   }

   public ModeSelect10(
         boolean pageFormat,
         boolean savePages,
         int parameterListLength,
         boolean linked,
         boolean normalACA)
   {
      super(OPERATION_CODE, linked, normalACA);
      this.PF = pageFormat;
      this.SP = savePages;
      this.parameterListLength = parameterListLength;
   }

   public ModeSelect10(boolean pageFormat, boolean savePages, int parameterListLength)
   {
      this(pageFormat, savePages, parameterListLength, false, false);
   }

   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));
      int tmp;

      int operationCode = in.readUnsignedByte();
      tmp = in.readUnsignedByte();
      this.SP = (tmp & 0x01) != 0;
      this.PF = (tmp >>> 4) != 0;
      tmp = in.readInt();
      tmp = in.readByte();
      this.parameterListLength = in.readUnsignedShort();
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
         out.writeByte(((this.SP ? 0x01 : 0x00) | (this.PF ? 0x10 : 0x00)));
         out.writeInt(0);
         out.writeByte(0);
         out.writeShort(this.parameterListLength);
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

   public boolean isPF()
   {
      return this.PF;
   }

   public void setPF(boolean pf)
   {
      this.PF = pf;
   }

   public boolean isSP()
   {
      return this.SP;
   }

   public void setSP(boolean sp)
   {
      this.SP = sp;
   }

   public int getParameterListLength()
   {
      return this.parameterListLength;
   }

   public void setParameterListLength(int parameterListLength)
   {
      this.parameterListLength = parameterListLength;
   }
}
