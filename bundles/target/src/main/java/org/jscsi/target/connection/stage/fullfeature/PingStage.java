package org.jscsi.target.connection.stage.fullfeature;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.nop.NOPOutParser;
import org.jscsi.target.connection.TargetPduFactory;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.settings.SettingsException;

/**
 * A stage for processing NOP-Out PDUs, which are used by the initiator for
 * pinging the target, making sure that the connection is still up.
 * <p>
 * The {@link #execute(ProtocolDataUnit)} method will process these ping messages and send a NOP-In PDU as
 * ping echo, containing a copy of the NOP-Out PDU's data segment.
 * <p>
 * If either the NOP-OUT PDU's initiator or target transfer tag equals the reserved value of 0xffffffff, then
 * no reply will be sent, since the PDU is only supposed to acknowledge a changed ExpCmdSN, or serve as an
 * echo to a NOP-IN ping sent by the target.
 * 
 * @author Andreas Ergenzinger
 */
public class PingStage extends TargetFullFeatureStage {

    private static final int RESERVED_TAG_VALUE = 0xffffffff;

    public PingStage(TargetFullFeaturePhase targetFullFeaturePhase) {
        super(targetFullFeaturePhase);
    }

    @Override
    public void execute(final ProtocolDataUnit pdu) throws IOException, InterruptedException,
        InternetSCSIException, DigestException, SettingsException {

        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final NOPOutParser parser = (NOPOutParser)bhs.getParser();

        if (parser.getTargetTransferTag() != RESERVED_TAG_VALUE) {
            /*
             * This is an error. The jSCSI Target does not send NOP-In ping
             * messages, which would be the only legal reason for the initiator
             * sending a NOP-Out with the TargetTransferTag equal to 0xffffffff.
             * And even if the jSCSI Target was sending pings, the echo would be
             * processed in a dedicated stage.
             * 
             * Therefore, we treat this as an error. Close the connection.
             */
            throw new InternetSCSIException("NOP-Out PDU TargetTransferTag = "
                + parser.getTargetTransferTag() + " in PingStage");
        }

        // decide whether or not response is necessary
        if (bhs.getInitiatorTaskTag() == RESERVED_TAG_VALUE)
            return;// send no response

        // else
        // prepare response data segment (copy up to initiator's
        // MaxRecvDataSegmentLength)
        final int dataSegmentLength =
            Math.min(pdu.getDataSegment().capacity(), settings.getMaxRecvDataSegmentLength());
        final ByteBuffer responseDataSegment = ByteBuffer.allocate(dataSegmentLength);
        responseDataSegment.put(pdu.getDataSegment().array(),// source array,
            0,// offset within the array of the first byte to be read
            dataSegmentLength);// length

        // send response
        final ProtocolDataUnit responsePdu = TargetPduFactory.createNopInPDU(0,// logicalUnitNumber,
                                                                               // reserved
            bhs.getInitiatorTaskTag(),// initiatorTaskTag
            RESERVED_TAG_VALUE,// targetTransferTag
            responseDataSegment);
        connection.sendPdu(responsePdu);
    }

}
