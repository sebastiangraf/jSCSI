package org.jscsi.target.scsi.sense.senseDataDescriptor;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.sense.information.EightByteInformation;

/**
 * The command-specific information sense data descriptor provides information
 * that depends on the command on which the exception condition occurred.
 * 
 * @author Andreas Ergenzinger
 */
public class CommandSpecificSenseDataDescriptor extends SenseDataDescriptor {

    /**
     * The byte position of the COMMAND-SPECIFIC INFORMATION field.
     */
    private static final int COMMAND_SPECIFIC_INFORMATION_INDEX = 4;

    /**
     * Should contain command-specific information.
     */
    private final EightByteInformation commandSpecificInformation;

    /**
     * The constructor.
     * 
     * @param commandSpecificInformation
     *            {@link EightByteInformation} which should contain
     *            command-specific information.
     */
    public CommandSpecificSenseDataDescriptor(final EightByteInformation commandSpecificInformation) {
        super(SenseDataDescriptorType.COMMAND_SPECIFIC_INFORMATION, 0x0a);// additional
                                                                          // length
        this.commandSpecificInformation = commandSpecificInformation;
    }

    @Override
    protected void serializeSpecificFields(ByteBuffer byteBuffer, int index) {
        commandSpecificInformation.serialize(byteBuffer, index + COMMAND_SPECIFIC_INFORMATION_INDEX);
    }

}
