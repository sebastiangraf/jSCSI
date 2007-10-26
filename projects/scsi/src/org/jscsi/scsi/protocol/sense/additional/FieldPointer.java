
package org.jscsi.scsi.protocol.sense.additional;

import java.io.IOException;
import java.nio.ByteBuffer;

// TODO: Describe class or interface
public class FieldPointer implements SenseKeySpecificField
{
   
   private boolean commandData;
   private byte bitPointer = -1; // MAX VALUE 0x07 (3-bit), for negative value BPV = 0
   private int fieldPointer;   // USHORT_MAX
   
   public FieldPointer(
         boolean commandData,
         byte bitPointer,
         int fieldPointer )
   {
      this.commandData = commandData;
      this.bitPointer = bitPointer;
      this.fieldPointer = fieldPointer;
   }
   
   public FieldPointer()
   {
      this.commandData = false;
      this.bitPointer = -1;
      this.fieldPointer = -1;
   }

   public boolean isCommandData()
   {
      return commandData;
   }

   public byte getBitPointer()
   {
      return bitPointer;
   }

   public int getFieldPointer()
   {
      return fieldPointer;
   }

   public FieldPointer decode(ByteBuffer buffer) throws IOException
   {
      byte[] encodedData = new byte[3];
      
      buffer.get(encodedData);
      
      boolean commandData = (encodedData[0] >> 6) == 1;
      
      byte bitPointer = (byte) (encodedData[0] & 0x03);
      
      int fieldPointer = encodedData[2]; // 8 LSBs
      fieldPointer |= (encodedData[1] << 8);
      
      return new FieldPointer(commandData, bitPointer, fieldPointer);
   }

   public void decode(byte[] header, ByteBuffer buffer) throws IOException
   {
      FieldPointer fieldPointer = decode(buffer);
      this.commandData = fieldPointer.isCommandData();
      this.bitPointer = fieldPointer.getBitPointer();
      this.fieldPointer = fieldPointer.getFieldPointer();
   }

   public byte[] encode()
   {
      byte[] encodedData = new byte[3];
      
      encodedData[0] = (byte) (1<<7);
      encodedData[0] |= (byte) (commandData ? (1<<6) : 0);
      encodedData[0] |= (byte) ((this.bitPointer != 0) ? (1<<3) : 0);
      encodedData[0] |= (byte) (this.bitPointer & 0x03);
      
      encodedData[1] = (byte) ((this.fieldPointer >> 8) & 0xFF);
      encodedData[2] = (byte) (this.fieldPointer & 0xFF);
      
      return encodedData;
   }

}


