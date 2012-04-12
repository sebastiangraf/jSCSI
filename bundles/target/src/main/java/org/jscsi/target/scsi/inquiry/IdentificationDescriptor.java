package org.jscsi.target.scsi.inquiry;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.ISerializable;
import org.jscsi.target.util.BitManip;

/**
 * Instances of this class contain information identifying the logical unit,
 * SCSI target device containing the logical unit, or access path (i.e., target
 * port) used by the command and returned parameter data.
 * 
 * @author Andreas Ergenzinger
 */
public final class IdentificationDescriptor implements ISerializable {

    /**
     * The total length of all bytes which are not part of the IDENTIFIER field.
     */
    private static final int HEADER_LENGTH = 4;

    /**
     * The PROTOCOL IDENTIFIER field may indicate the SCSI transport protocol to
     * which the identification descriptor applies. If the ASSOCIATION field
     * contains a value other than 01b (i.e., target port) or 10b (i.e., SCSI
     * target device) or the PIV bit is set to zero, then the PROTOCOL
     * IDENTIFIER field contents are reserved. If the ASSOCIATION field contains
     * a value of 01b or 10b and the PIV bit is set to one, then the PROTOCOL
     * IDENTIFIER field shall contain one of the values defined in SPC-3 to
     * indicate the SCSI transport protocol to which the identification
     * descriptor applies.
     */
    private final ProtocolIdentifier protocolIdentifier;

    /**
     * The CODE SET field indicates the code set used for the IDENTIFIER field.
     */
    private final CodeSet codeSet;

    /**
     * A protocol identifier valid (PIV) bit set to zero indicates the PROTOCOL
     * IDENTIFIER field contents are reserved. If the ASSOCIATION field contains
     * a value of 01b or 10b, then a PIV bit set to one indicates the PROTOCOL
     * IDENTIFIER field contains a valid protocol identifier.
     * <p>
     * If the ASSOCIATION field contains a value other than 01b or 10b, then the PIV bit contents are
     * reserved.
     */
    private final boolean protocolIdentifierValid;

    /**
     * The ASSOCIATION field indicates the entity with which the IDENTIFIER
     * field is associated.
     */
    private final Association association;

    /**
     * The IDENTIFIER TYPE field indicates the format and assignment authority
     * for the identifier.
     */
    private final IdentifierType identifierType;

    /**
     * The IDENTIFIER field contains identifying information about the
     * represented entity.
     */
    private final Identifier identifier;

    public IdentificationDescriptor(final ProtocolIdentifier protocolIdentifier, final CodeSet codeSet,
        final boolean protocolIdentifierValid, final Association association,
        final IdentifierType identifierType, final Identifier identifier) {
        this.protocolIdentifier = protocolIdentifier;
        this.codeSet = codeSet;
        this.protocolIdentifierValid = protocolIdentifierValid;
        this.association = association;
        this.identifierType = identifierType;
        this.identifier = identifier;
    }

    public void serialize(ByteBuffer byteBuffer, int index) {

        // byte 0
        byteBuffer.position(index);
        byte b = (byte)(((protocolIdentifier.getValue() & 15) << 4) | (codeSet.getValue() & 15));
        byteBuffer.put(b);

        // byte 1
        b = (byte)(((association.getValue() & 3) << 4) | (identifierType.getValue() & 15));
        b = BitManip.getByteWithBitSet(b, 7, protocolIdentifierValid);
        byteBuffer.put(b);

        // byte 2 is reserved
        byteBuffer.put((byte)0);

        // byte 3
        byteBuffer.put((byte)identifier.size());// identifier length

        // identifier
        identifier.serialize(byteBuffer, index + HEADER_LENGTH);
    }

    public int size() {
        return identifier.size() + HEADER_LENGTH;
    }
}
