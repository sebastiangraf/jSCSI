package org.jscsi.scsi.protocol.cdb;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

// TODO: Describe class or interface
public interface CommandDescriptorBlock
{   
   int getOperationCode();

   long getLogicalBlockAddress();
     
   /**
    * The transfer length, usually in blocks. Zero if the command does not require a transfer length
    * or no data is to be transferred.
    * 
    * @return Transfer length in blocks.
    */
   long getTransferLength();
   
   /**
    * 
    * @return
    */
   long getAllocationLength();
   
   boolean isNormalACA();
   
   boolean isLinked();
   
   /**
    * Serializes a CDB to the current position in a byte buffer.
    */
   void encode( ByteBuffer output ) throws BufferOverflowException;
   
   /**
    * Deserializes a CDB from the current position in a byte buffer.
    */
   void decode( ByteBuffer input ) throws BufferUnderflowException, IOException;
   
   /**
    * Returns CDB serialization size in bytes.
    */
   int size();
}
