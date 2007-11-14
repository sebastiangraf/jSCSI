
package org.jscsi.scsi.protocol.sense.additional;

import java.io.IOException;
import java.nio.ByteBuffer;

// TODO: Describe class or interface
public class FieldPointer implements SenseKeySpecificField
{

   private boolean CD;
   private boolean BPV;
   private byte bitPointer = -1; // MAX VALUE 0x07 (3-bit), for negative value BPV = 0
   private int fieldPointer; // USHORT_MAX

   public FieldPointer(boolean commandData, byte bitPointer, int fieldPointer)
   {
      this.CD = commandData;
      this.bitPointer = bitPointer;
      this.fieldPointer = fieldPointer;
   }

   public FieldPointer()
   {
      this.CD = false;
      this.bitPointer = -1;
      this.fieldPointer = -1;
   }

   @SuppressWarnings("unchecked")
   public FieldPointer decode(ByteBuffer buffer) throws IOException
   {
      byte[] encodedData = new byte[3];

      buffer.get(encodedData);

      this.CD = ((encodedData[0] >>> 6) & 0x01) == 1;

      this.BPV = ((encodedData[0] >>> 3) & 0x01) == 1;

      this.bitPointer = (byte) (encodedData[0] & 0x07);

      this.fieldPointer = (encodedData[2] & 0xFF); // 8 LSBs
      this.fieldPointer |= ((encodedData[1] & 0xFF) << 8);

      return this;
   }

   public void decode(byte[] header, ByteBuffer buffer) throws IOException
   {
      decode(buffer);
   }

   public byte[] encode()
   {
      byte[] encodedData = new byte[3];

      encodedData[0] = (byte) (1 << 7);
      encodedData[0] |= (byte) (this.CD ? (1 << 6) : 0);
      encodedData[0] |= (byte) (this.BPV ? (1 << 3) : 0);
      encodedData[0] |= (byte) (this.bitPointer & 0x07);

      encodedData[1] = (byte) ((this.fieldPointer >>> 8) & 0xFF);
      encodedData[2] = (byte) (this.fieldPointer & 0xFF);

      return encodedData;
   }

   public boolean isCD()
   {
      return this.CD;
   }

   public void setCD(boolean cd)
   {
      this.CD = cd;
   }

   public boolean isBPV()
   {
      return this.BPV;
   }

   public void setBPV(boolean bpv)
   {
      this.BPV = bpv;
   }

   public byte getBitPointer()
   {
      return this.bitPointer;
   }

   public void setBitPointer(byte bitPointer)
   {
      this.bitPointer = bitPointer;
   }

   public int getFieldPointer()
   {
      return this.fieldPointer;
   }

   public void setFieldPointer(int fieldPointer)
   {
      this.fieldPointer = fieldPointer;
   }

}
