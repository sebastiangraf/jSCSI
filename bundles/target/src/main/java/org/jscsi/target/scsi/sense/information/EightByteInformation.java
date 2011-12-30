package org.jscsi.target.scsi.sense.information;

import java.nio.ByteBuffer;

/**
 * {@link Information} with a field length of 8 bytes.
 * 
 * @author Andreas Ergenzinger
 */
public class EightByteInformation extends Information {

    private static final int SIZE = 8;

    public void serialize(ByteBuffer byteBuffer, int index) {
        // do nothing
    }

    public int size() {
        return SIZE;
    }

}
