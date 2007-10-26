
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class ModeSelect6 extends AbstractCommandDescriptorBlock
{
   public static final int OPERATION_CODE = 0x15;

   private boolean pageFormat;
   private boolean savePages;
   private int parameterListLength;

   public boolean isPageFormat()
   {
      return pageFormat;
   }

   public void setPageFormat(boolean pageFormat)
   {
      this.pageFormat = pageFormat;
   }

   public boolean isSavePages()
   {
      return savePages;
   }

   public void setSavePages(boolean savePages)
   {
      this.savePages = savePages;
   }

   public int getParameterListLength()
   {
      return parameterListLength;
   }

   public void setParameterListLength(int parameterListLength)
   {
      this.parameterListLength = parameterListLength;
   }

   public ModeSelect6()
   {
      super();
   }

   public ModeSelect6(
         boolean pageFormat,
         boolean savePages,
         int parameterListLength,
         boolean linked,
         boolean normalACA)
   {
      super(linked, normalACA);
      this.pageFormat = pageFormat;
      this.savePages = savePages;
      this.parameterListLength = parameterListLength;
   }

   public ModeSelect6(boolean pageFormat, boolean savePages, int parameterListLength)
   {
      this(pageFormat, savePages, parameterListLength, false, false);
   }

   @Override
   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));
      int tmp;


      int operationCode = in.readUnsignedByte();
      tmp = in.readUnsignedByte();
      this.savePages = (tmp & 0x01) != 0;
      this.pageFormat = (tmp >>> 4) != 0;
      tmp = in.readShort();
      this.parameterListLength = in.readUnsignedByte();
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
         out.writeByte(((this.savePages ? 0x01 : 0x00) | (this.pageFormat ? 0x10 : 0x00)));
         out.writeShort(0);
         out.writeByte(this.parameterListLength);
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
      return 0;
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
