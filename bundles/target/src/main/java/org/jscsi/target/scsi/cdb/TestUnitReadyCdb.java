package org.jscsi.target.scsi.cdb;

import java.nio.ByteBuffer;

/**
 * This class represents Command Descriptor Blocks for the <code>TEST UNIT READY</code> SCSI command.
 * 
 * @author Andreas Ergenzinger
 */
public class TestUnitReadyCdb extends CommandDescriptorBlock {

    public TestUnitReadyCdb(ByteBuffer buffer) {
        super(buffer);
        // no additional fields to deserialize
    }
}
