package org.jscsi.target.scsi.cdb;

import java.nio.ByteBuffer;

/**
 * This abstract class represents Command Descriptor Blocks for <code>READ</code> SCSI commands.
 * 
 * @see Read6Cdb
 * @see Read10Cdb
 * @author Andreas Ergenzinger
 */
public abstract class ReadCdb extends ReadOrWriteCdb {

    public ReadCdb(ByteBuffer buffer) {
        super(buffer);
    }

}
