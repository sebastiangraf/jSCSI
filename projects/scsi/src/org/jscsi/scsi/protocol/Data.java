
package org.jscsi.scsi.protocol;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Represents command arguments, return data, mode pages, and sense data in a serializable form.
 * 
 */
public interface Data
{
   
   /**
    * Serializes data to a byte buffer.
    */
   void encode( ByteBuffer output ) throws BufferOverflowException;
   
   
}


