
package org.jscsi.scsi.protocol.sense.additional;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Encodable;

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
      // TODO Auto-generated method stub
      return null;
   }

   public void decode(byte[] header, ByteBuffer buffer) throws IOException
   {
      // TODO Auto-generated method stub
      
   }

   public byte[] encode()
   {
      // TODO Auto-generated method stub
      return null;
   }


   
   

}


