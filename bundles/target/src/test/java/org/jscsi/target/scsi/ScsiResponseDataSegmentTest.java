package org.jscsi.target.scsi;

import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.ProtocolDataUnitFactory;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.target.scsi.cdb.RequestSenseCdb;
import org.jscsi.target.scsi.sense.AdditionalSenseBytes;
import org.jscsi.target.scsi.sense.AdditionalSenseCodeAndQualifier;
import org.jscsi.target.scsi.sense.DescriptorFormatSenseData;
import org.jscsi.target.scsi.sense.ErrorType;
import org.jscsi.target.scsi.sense.FixedFormatSenseData;
import org.jscsi.target.scsi.sense.SenseData;
import org.jscsi.target.scsi.sense.SenseKey;
import org.jscsi.target.scsi.sense.information.FourByteInformation;
import org.jscsi.target.scsi.sense.senseDataDescriptor.SenseDataDescriptor;
import org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific.FieldPointerSenseKeySpecificData;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class tests the behavior of a scsi response data segment.
 * 
 * @author Andreas Rain
 * 
 */
public class ScsiResponseDataSegmentTest {

    /**
     * None-empty data segment
     */

    static ScsiResponseDataSegment nonEmpty;

    @BeforeClass
    public void beforeClass() {

        /**
         * The following part simulates a none-empty ScsiResponseDataSegment.
         * It's copied for the most part from the RequestSenseStage
         */

        final ProtocolDataUnit pdu =
            new ProtocolDataUnitFactory().create(false, true, OperationCode.LOGIN_REQUEST, "None", "None");

        final SCSICommandParser parser = new SCSICommandParser(pdu);
        parser.setExpectedDataTransferLength(8);

        // get command details in CDB
        final RequestSenseCdb cdb = new RequestSenseCdb(parser.getCDB());

        SenseData senseData;

        if (cdb.getDescriptorFormat()) {
            // descriptor format sense data has been requested

            senseData = new DescriptorFormatSenseData(ErrorType.CURRENT,// errorType
                SenseKey.ILLEGAL_REQUEST,// sense key
                AdditionalSenseCodeAndQualifier.INVALID_FIELD_IN_CDB,// additional
                                                                     // sense
                                                                     // code
                                                                     // and
                                                                     // qualifier
                new SenseDataDescriptor[0]);// sense data descriptors

        } else {
            // fixed format sense data has been requested

            senseData = new FixedFormatSenseData(false,// valid
                ErrorType.CURRENT,// error type
                false,// file mark
                false,// end of medium
                false,// incorrect length indicator
                SenseKey.ILLEGAL_REQUEST,// sense key
                new FourByteInformation(),// information
                new FourByteInformation(),// command specific
                                          // information
                AdditionalSenseCodeAndQualifier.INVALID_FIELD_IN_CDB,// additional
                                                                     // sense
                                                                     // code
                                                                     // and
                                                                     // qualifier
                (byte)0,// field replaceable unit code
                new FieldPointerSenseKeySpecificData(true,// senseKeySpecificDataValid
                    true,// commandData (i.e. invalid field in CDB)
                    true,// bitPointerValid
                    3,// bitPointer
                    8),// fieldPointer,// sense key specific data, only
                       // report first problem
                new AdditionalSenseBytes());// additional sense bytes
        }

        nonEmpty = new ScsiResponseDataSegment(senseData, parser.getExpectedDataTransferLength());
    }

    @Test
    public void testNoneEmpty() {
        nonEmpty.serialize();
    }

    @Test
    public void testEmpty() {
        ScsiResponseDataSegment.EMPTY_DATA_SEGMENT.serialize();
    }

}
