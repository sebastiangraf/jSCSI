
package org.jscsi.scsi.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A factory object capable of selecting the proper class to decode data from an incoming byte
 * buffer.
 */
public interface Serializer
{
   /**
    * Returns the object represented by the incoming byte buffer data.
    * @param <T> An object representing the incoming data.
    * @param buffer A byte buffer set at the beginning of the serialized data.
    * @return An encodable object decoded from the input data.
    * @throws IOException If an object could not be decoded from the incoming data.
    */
   <T extends Encodable> T decode( ByteBuffer buffer ) throws IOException;
   
}


