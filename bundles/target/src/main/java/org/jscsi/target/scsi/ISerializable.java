package org.jscsi.target.scsi;

import java.nio.ByteBuffer;

/**
 * This interface specifies methods for serializing an object. The serialized
 * object can be inserted into an existing {@link ByteBuffer} of sufficient size
 * at an arbitrary position.
 * <p>
 * This interface is implemented by the classes representing sense and response data (as well as components
 * thereof) to SCSI requests, which are returned to the iSCSI initiator in the SCSI Response PDU's data
 * segment.
 * <p>
 * Additional care must be taken when implementing objects consisting of several {@link ISerializable}
 * components, not to overwrite the fields of another component. The advantage of this strategy, however, will
 * be higher speed, since unnecessary buffer-to-buffer copying is avoided.
 * 
 * @author Andreas Ergenzinger
 */
public interface ISerializable {

    /**
     * Inserts a serialized representation of the object into the specified {@link ByteBuffer}. The serialized
     * object will occupy the byte positions
     * from <i>index</i> to <i>index + {@link #size()} - 1</i>.
     * 
     * @param byteBuffer
     *            where to insert the serialized object representation
     * @param index
     *            the position of the first byte of the serialized object in the {@link ByteBuffer}
     */
    public void serialize(ByteBuffer byteBuffer, int index);

    /**
     * Returns the size in bytes of the object's serialized representation.
     * 
     * @return the size in bytes of the object's serialized representation
     */
    public int size();

}
