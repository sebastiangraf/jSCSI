package org.jscsi.target.connection.stage.fullfeature;

import java.io.IOException;
import java.security.DigestException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.InitiatorMessageParser;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.scsi.cdb.ScsiOperationCode;
import org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific.FieldPointerSenseKeySpecificData;
import org.jscsi.target.settings.SettingsException;

/**
 * Unlike the other subclasses of {@link TargetFullFeatureStage}, this class is
 * not associated with a single {@link ScsiOperationCode}. All SCSI Command PDUs
 * containing a SCSI OpCode not supported by the jSCSI Target (i.e. without a
 * dedicated FullFeatureStage to process them) shall be passed to the {@link #execute(ProtocolDataUnit)}
 * method of this class, which will dispatch
 * a standard SCSI Response PDU stating that the given {@link ScsiOperationCode} is not supported by this
 * target.
 * 
 * @author Andreas Ergenzinger
 */
public class UnsupportedOpCodeStage extends TargetFullFeatureStage {

    public UnsupportedOpCodeStage(TargetFullFeaturePhase targetFullFeaturePhase) {
        super(targetFullFeaturePhase);
    }

    @Override
    public void execute(ProtocolDataUnit pdu) throws IOException, InterruptedException,
        InternetSCSIException, DigestException, SettingsException {

        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final InitiatorMessageParser parser = (InitiatorMessageParser)bhs.getParser();

        // the SCSI OpCode is not supported, tell the initiator

        final FieldPointerSenseKeySpecificData fp = new FieldPointerSenseKeySpecificData(true,// senseKeySpecificDataValid
            true,// commandData (i.e. invalid field in CDB)
            false,// bitPointerValid
            0,// bitPointer, reserved since invalid
            0);// fieldPointer to the SCSI OpCode field

        final FieldPointerSenseKeySpecificData[] fpArray = new FieldPointerSenseKeySpecificData[] {
            fp
        };

        final ProtocolDataUnit responsePdu = createFixedFormatErrorPdu(fpArray,// senseKeySpecificData
            bhs.getInitiatorTaskTag(),// initiatorTaskTag
            parser.getExpectedStatusSequenceNumber());// expectedDataTransferLength

        // send response
        connection.sendPdu(responsePdu);
    }

}
