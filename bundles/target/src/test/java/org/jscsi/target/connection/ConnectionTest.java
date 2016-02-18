package org.jscsi.target.connection;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.InitiatorMessageParser;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.ProtocolDataUnitFactory;
import org.jscsi.parser.data.DataInParser;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.parser.scsi.SCSIResponseParser;
import org.jscsi.parser.scsi.SCSIResponseParser.ServiceResponse;
import org.jscsi.parser.scsi.SCSIStatus;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.connection.stage.TargetStage;
import org.jscsi.target.connection.stage.fullfeature.FormatUnitStage;
import org.jscsi.target.connection.stage.fullfeature.InquiryStage;
import org.jscsi.target.connection.stage.fullfeature.ReportLunsStage;
import org.jscsi.target.connection.stage.fullfeature.RequestSenseStage;
import org.jscsi.target.connection.stage.fullfeature.SendDiagnosticStage;
import org.jscsi.target.connection.stage.fullfeature.TestUnitReadyStage;
import org.jscsi.target.connection.stage.fullfeature.TextNegotiationStage;
import org.jscsi.target.connection.stage.fullfeature.UnsupportedOpCodeStage;
import org.jscsi.target.scsi.IResponseData;
import org.jscsi.target.scsi.ScsiResponseDataSegment;
import org.jscsi.target.scsi.cdb.InquiryCDB;
import org.jscsi.target.scsi.cdb.ReportLunsCDB;
import org.jscsi.target.scsi.cdb.RequestSenseCdb;
import org.jscsi.target.scsi.cdb.SendDiagnosticCdb;
import org.jscsi.target.scsi.inquiry.PageCode.VitalProductDataPageName;
import org.jscsi.target.scsi.inquiry.StandardInquiryData;
import org.jscsi.target.scsi.inquiry.SupportedVpdPages;
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
import org.jscsi.target.settings.ConnectionSettingsNegotiator;
import org.jscsi.target.settings.SessionSettingsNegotiator;
import org.jscsi.target.settings.SettingsException;
import org.jscsi.target.settings.TextKeyword;
import org.jscsi.target.settings.TextParameter;
import org.jscsi.target.util.ReadWrite;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;


public class ConnectionTest {
    // /**
    // * This test executes the mode sense stage covering the stage execution
    // * using a the target connection and a stub'ish pdu.
    // */
    // @Test(enabled = false)
    // public void testModeSenseStage() throws Exception {
    // TargetConnection connection = targetServer.getConnection();
    // modeSenseStage = new ModeSenseStage(new TargetFullFeaturePhase(connection));
    //
    // final ProtocolDataUnit pdu =
    // new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None");
    //
    // modeSenseStage.execute(pdu);
    // }
    //
    // /**
    // * This test executes the ping stage covering the stage execution
    // * using a the target connection and a stub'ish pdu.
    // */
    // @Test(enabled = false)
    // public void testPingStage() throws Exception {
    // TargetConnection connection = targetServer.getConnection();
    // pingStage = new PingStage(new TargetFullFeaturePhase(connection));
    //
    // final ProtocolDataUnit pdu =
    // new ProtocolDataUnitFactory().create(false, true, OperationCode.NOP_OUT, "None", "None");
    //
    // pingStage.execute(pdu);
    // }

    ArgumentCaptor<ProtocolDataUnit> captor = ArgumentCaptor.forClass(ProtocolDataUnit.class);
    final Connection connection = mock(Connection.class);

    @Test (dataProvider = "instantiateStages" , enabled = true)
    public void testStages (Class<TargetStage> pTargetStageClass, TargetStage[] pStages, Class<ProtocolDataUnit> pDataUnitClass, ProtocolDataUnit[] pDataUnits, Class<Checker> pCheckerClass, Checker[] pChecker) throws DigestException , IOException , InterruptedException , InternetSCSIException , SettingsException {
        assertEquals(pStages.length, pDataUnits.length);
        assertEquals(pStages.length, pChecker.length);

        List<ProtocolDataUnit> units = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            pStages[i].execute(pDataUnits[i]);
            units.add(pChecker[i].check(pStages[i].getConnection()));

            // Some output that makes watching the pdus easiert
            /*
             * System.out.println("************** STAGE UNIT *************** ");
             * System.out.println(pStages[i].getClass()); System.out.println(); System.out.println(units.get(i));*
             */
        }

        verify(connection, times(8)).sendPdu(captor.capture());

        // Some output that makes watching the pdus easiert
        /*
         * for (int i = 0; i < 8; i++) { if (captor.getAllValues().get(i).getBasicHeaderSegment().getParser() instanceof
         * DataInParser) continue; System.out.println("************** TEST UNIT *************** "); if (i > 0)
         * System.out.println(pStages[i].getClass() + "(" + (pStages[i - 1].getClass()) + ")"); else
         * System.out.println(pStages[i].getClass()); System.out.println();
         * System.out.println(captor.getAllValues().get(i)); }
         */

        for (int i = 0; i < units.size() - 1; i++) {
            if (captor.getAllValues().get(0).getBasicHeaderSegment().getParser() instanceof DataInParser == false)
                assertEquals(units.remove(0), captor.getAllValues().remove(0));
            else
                captor.getAllValues().remove(0);
        }

    }

    @DataProvider (name = "instantiateStages")
    public Object[][] provideStages () {
        // special pdu for textnegotation
        final ProtocolDataUnit textNegotationUnit = new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_TM_REQUEST, "None", "None");
        textNegotationUnit.setDataSegment(ByteBuffer.wrap("hello world".getBytes()));

        // setting up the connection properly
        SessionSettingsNegotiator sessionSettingsNegotiator = new SessionSettingsNegotiator();
        ConnectionSettingsNegotiator connectionSettingsNegotiator = new ConnectionSettingsNegotiator(sessionSettingsNegotiator);
        final TargetSession session = mock(TargetSession.class);

        when(connection.getSettings()).thenReturn(connectionSettingsNegotiator.getSettings());

        // setting up the phases
        TargetFullFeaturePhase phase = new TargetFullFeaturePhase(connection);
        Object[][] returnVal = { { TargetStage.class, new TargetStage[] { new TestUnitReadyStage(phase), new SendDiagnosticStage(phase), new ReportLunsStage(phase), new InquiryStage(phase), new RequestSenseStage(phase), new TextNegotiationStage(phase), new UnsupportedOpCodeStage(phase), new FormatUnitStage(phase) }, ProtocolDataUnit.class, new ProtocolDataUnit[] {
                // TextUnitReadyStage
        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None"),
                // SendDiagnosticStage
        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None"),
                // ReportLunsStage
        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None"),
                // InquiryStage
        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None"),
                // RequestSenseStage
        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None"),
                // TextNegotiationStage
        textNegotationUnit,
                // UnsupportedOpCodeStage
        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_TM_REQUEST, "None", "None"),
                // FormatUnitStage
        new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None") }, Checker.class, new Checker[] {
                // TestUnitReadyStage checker
        new Checker() {

            @Override
            public ProtocolDataUnit check (final Connection pConnection) throws InterruptedException , IOException , InternetSCSIException {
                ProtocolDataUnit responsePdu;
                final BasicHeaderSegment bhs = new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None").getBasicHeaderSegment();

                // Taken from the stage
                responsePdu = TargetPduFactory.createSCSIResponsePdu(false,// bidirectionalReadResidualOverflow
                        false,// bidirectionalReadResidualUnderflow
                        false,// residualOverflow
                        false,// residualUnderflow,
                        SCSIResponseParser.ServiceResponse.COMMAND_COMPLETED_AT_TARGET,// response,
                        SCSIStatus.GOOD,// status,
                        bhs.getInitiatorTaskTag(),// initiatorTaskTag,
                        0,// snackTag
                        0,// expectedDataSequenceNumber
                        0,// bidirectionalReadResidualCount
                        0,// residualCount
                        ScsiResponseDataSegment.EMPTY_DATA_SEGMENT);// data
                                                                    // segment

                return responsePdu;
            }
        }, // SendDiagnosticStage checker
        new Checker() {

            @Override
            public ProtocolDataUnit check (final Connection pConnection) throws InterruptedException , IOException , InternetSCSIException {
                ProtocolDataUnit responsePdu;
                final BasicHeaderSegment bhs = new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None").getBasicHeaderSegment();
                final SCSICommandParser parser = (SCSICommandParser) bhs.getParser();
                final SendDiagnosticCdb cdb = new SendDiagnosticCdb(parser.getCDB());

                // create the whole sense data
                FixedFormatSenseData senseData = new FixedFormatSenseData(false,// valid
                ErrorType.CURRENT,// error type
                false,// file mark
                false,// end of medium
                false,// incorrect length indicator
                SenseKey.ILLEGAL_REQUEST,// sense key
                new FourByteInformation(),// information
                new FourByteInformation(),// command specific information
                AdditionalSenseCodeAndQualifier.INVALID_FIELD_IN_CDB,// additional sense
                                                                     // code and
                // qualifier
                (byte) 0,// field replaceable unit code
                cdb.getIllegalFieldPointers()[0],// sense key specific data, only report
                // first problem
                new AdditionalSenseBytes());// additional sense bytes

                // keep only the part of the sense data that will be sent
                final ScsiResponseDataSegment dataSegment = new ScsiResponseDataSegment(senseData, parser.getExpectedDataTransferLength());
                final int senseDataSize = senseData.size();

                // calculate residuals and flags
                final int residualCount = Math.abs(parser.getExpectedDataTransferLength() - senseDataSize);
                final boolean residualOverflow = parser.getExpectedDataTransferLength() < senseDataSize;
                final boolean residualUnderflow = parser.getExpectedDataTransferLength() > senseDataSize;

                // create and return PDU
                responsePdu = TargetPduFactory.createSCSIResponsePdu(false,// bidirectionalReadResidualOverflow
                        false,// bidirectionalReadResidualUnderflow
                        residualOverflow,// residualOverflow
                        residualUnderflow,// residualUnderflow,
                        SCSIResponseParser.ServiceResponse.COMMAND_COMPLETED_AT_TARGET,// response,
                        SCSIStatus.CHECK_CONDITION,// status,
                        bhs.getInitiatorTaskTag(),// initiatorTaskTag,
                        0,// snackTag
                        0,// expectedDataSequenceNumber
                        0,// bidirectionalReadResidualCount
                        residualCount,// residualCount
                        dataSegment);// data segment
                return responsePdu;
            }
        }, // ReportLunsStage checker
        new Checker() {

            @Override
            public ProtocolDataUnit check (final Connection pConnection) throws InterruptedException , IOException , InternetSCSIException {
                ProtocolDataUnit responsePdu;
                final BasicHeaderSegment bhs = new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None").getBasicHeaderSegment();
                final SCSICommandParser parser = (SCSICommandParser) bhs.getParser();
                final ReportLunsCDB cdb = new ReportLunsCDB(parser.getCDB());
                final FieldPointerSenseKeySpecificData[] illegalFieldPointers = cdb.getIllegalFieldPointers();

                FixedFormatSenseData senseData = new FixedFormatSenseData(false,// valid
                ErrorType.CURRENT,// error type
                false,// file mark
                false,// end of medium
                false,// incorrect length indicator
                SenseKey.ILLEGAL_REQUEST,// sense key
                new FourByteInformation(),// information
                new FourByteInformation(),// command specific information
                AdditionalSenseCodeAndQualifier.INVALID_FIELD_IN_CDB,// additional sense
                                                                     // code and
                // qualifier
                (byte) 0,// field replaceable unit code
                illegalFieldPointers[0],// sense key specific data, only report
                                        // first problem
                new AdditionalSenseBytes());// additional sense bytes

                // keep only the part of the sense data that will be sent
                final ScsiResponseDataSegment dataSegment = new ScsiResponseDataSegment(senseData, parser.getExpectedDataTransferLength());
                final int senseDataSize = senseData.size();
                // calculate residuals and flags
                final int residualCount = Math.abs(parser.getExpectedDataTransferLength() - senseDataSize);
                final boolean residualOverflow = parser.getExpectedDataTransferLength() < senseDataSize;
                final boolean residualUnderflow = parser.getExpectedDataTransferLength() > senseDataSize;
                // create and return PDU
                responsePdu = TargetPduFactory.createSCSIResponsePdu(false,// bidirectionalReadResidualOverflow
                        false,// bidirectionalReadResidualUnderflow
                        residualOverflow,// residualOverflow
                        residualUnderflow,// residualUnderflow,
                        SCSIResponseParser.ServiceResponse.COMMAND_COMPLETED_AT_TARGET,// response,
                        SCSIStatus.CHECK_CONDITION,// status,
                        bhs.getInitiatorTaskTag(),// initiatorTaskTag,
                        0,// snackTag
                        0,// expectedDataSequenceNumber
                        0,// bidirectionalReadResidualCount
                        residualCount,// residualCount
                        dataSegment);// data segment

                // send response
                return responsePdu;
            }
        }, // InquiryStage checker
        new Checker() {

            @Override
            public ProtocolDataUnit check (final Connection pConnection) throws InterruptedException , IOException , InternetSCSIException {
                ProtocolDataUnit responsePdu;
                final BasicHeaderSegment bhs = new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None").getBasicHeaderSegment();
                final SCSICommandParser parser = (SCSICommandParser) bhs.getParser();
                final InquiryCDB cdb = new InquiryCDB(parser.getCDB());
                IResponseData responseData;

                if (!cdb.getEnableVitalProductData()) {
                    responseData = StandardInquiryData.getInstance();
                } else {
                    final VitalProductDataPageName pageName = cdb.getPageCode().getVitalProductDataPageName();

                    switch (pageName) {// is never null
                        case SUPPORTED_VPD_PAGES :
                            responseData = SupportedVpdPages.getInstance();
                            break;
                        case DEVICE_IDENTIFICATION :
                            responseData = session.getTargetServer().getDeviceIdentificationVpdPage();
                            break;
                        default :
                            throw new InternetSCSIException();
                    }
                }

                // The part from the targetfullfeaturephase

                final ByteBuffer fullBuffer = ByteBuffer.allocate(responseData.size());
                responseData.serialize(fullBuffer, 0);

                ByteBuffer trimmedBuffer;
                if (fullBuffer.capacity() <= parser.getExpectedDataTransferLength()) {
                    trimmedBuffer = fullBuffer;
                } else {
                    trimmedBuffer = ByteBuffer.allocate(parser.getExpectedDataTransferLength());
                    trimmedBuffer.put(fullBuffer.array(),// source array
                            0,// offset in source
                            parser.getExpectedDataTransferLength());// length
                }

                final boolean residualOverflow = parser.getExpectedDataTransferLength() < fullBuffer.capacity();
                final boolean residualUnderflow = parser.getExpectedDataTransferLength() > fullBuffer.capacity();
                final int residualCount = Math.abs(parser.getExpectedDataTransferLength() - fullBuffer.capacity());

                responsePdu = TargetPduFactory.createSCSIResponsePdu(false,// bidirectionalReadResidualOverflow
                        false,// bidirectionalReadResidualUnderflow
                        residualOverflow,// residualOverflow
                        residualUnderflow,// residualUnderflow
                        ServiceResponse.COMMAND_COMPLETED_AT_TARGET,// response
                        SCSIStatus.GOOD,// status
                        bhs.getInitiatorTaskTag(), 0,// snackTag, reserved
                        0,// expectedDataSequenceNumber
                        0,// bidirectionalReadResidualCount
                        residualCount,// residualCount
                        ScsiResponseDataSegment.EMPTY_DATA_SEGMENT);// scsiResponseDataSegment

                return responsePdu;
            }
        }, // RequestSenseStage checker
        new Checker() {

            @Override
            public ProtocolDataUnit check (final Connection pConnection) throws InterruptedException , IOException , InternetSCSIException {
                ProtocolDataUnit responsePdu;
                final BasicHeaderSegment bhs = new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None").getBasicHeaderSegment();
                final SCSICommandParser parser = (SCSICommandParser) bhs.getParser();
                final RequestSenseCdb cdb = new RequestSenseCdb(parser.getCDB());
                SenseData senseData;
                final SenseKey senseKey = SenseKey.NO_SENSE;
                final AdditionalSenseCodeAndQualifier additionalSense = AdditionalSenseCodeAndQualifier.NO_ADDITIONAL_SENSE_INFORMATION;

                if (cdb.getDescriptorFormat()) {
                    senseData = new DescriptorFormatSenseData(ErrorType.CURRENT,// errorType
                    senseKey,// sense key
                    additionalSense,// additional sense code and qualifier
                    new SenseDataDescriptor[0]);// sense data descriptors
                } else {
                    senseData = new FixedFormatSenseData(false,// valid
                    ErrorType.CURRENT,// error type
                    false,// file mark
                    false,// end of medium
                    false,// incorrect length indicator
                    senseKey,// sense key
                    new FourByteInformation(),// information
                    new FourByteInformation(),// command specific
                                              // information
                    additionalSense,// additional sense code and qualifier
                    (byte) 0,// field replaceable unit code
                    null,// sense key specific data, only report first
                         // problem
                    null);// additional sense bytes
                }

                responsePdu = TargetPduFactory.createSCSIResponsePdu(false,// bidirectionalReadResidualOverflow
                        false,// bidirectionalReadResidualUnderflow
                        false,// residualOverflow
                        false,// residualUnderflow,
                        SCSIResponseParser.ServiceResponse.COMMAND_COMPLETED_AT_TARGET,// response,
                        SCSIStatus.GOOD,// status,
                        bhs.getInitiatorTaskTag(),// initiatorTaskTag,
                        0,// snackTag
                        0,// expectedDataSequenceNumber
                        0,// bidirectionalReadResidualCount
                        0,// residualCount
                        new ScsiResponseDataSegment(senseData, parser.getExpectedDataTransferLength()));

                return responsePdu;
            }
        }, // TextNegotiationStage checker
        new Checker() {

            @Override
            public ProtocolDataUnit check (final Connection pConnection) throws InterruptedException , IOException , InternetSCSIException , SettingsException {
                ProtocolDataUnit responsePdu;
                final BasicHeaderSegment bhs = textNegotationUnit.getBasicHeaderSegment();

                final int initiatorTaskTag = bhs.getInitiatorTaskTag();

                final String textRequest = new String(textNegotationUnit.getDataSegment().array());

                ByteBuffer replyDataSegment = null;// for later

                // tokenize key-value pairs
                final List<String> requestKeyValuePairs = TextParameter.tokenizeKeyValuePairs(textRequest);

                final List<String> responseKeyValuePairs = new Vector<>();

                // process SendTargets command
                if (requestKeyValuePairs != null) {
                    String sendTargetsValue = null;

                    if (requestKeyValuePairs.size() == 1) sendTargetsValue = TextParameter.getSuffix(requestKeyValuePairs.get(0),// string
                            TextKeyword.SEND_TARGETS + TextKeyword.EQUALS);// prefix

                    if (sendTargetsValue != null) {
                        final boolean normal = session.isNormalSession();
                        final boolean sendTargetName = // see upper table
                        !normal && sendTargetsValue.equals(TextKeyword.ALL);
                        final boolean sendTargetAddress = // see upper table
                        (!normal && sendTargetsValue.equals(TextKeyword.ALL)) || (session.getTargetServer().isValidTargetName(sendTargetsValue)) || (normal && sendTargetsValue.length() == 0);

                        // add TargetName
                        if (sendTargetName) {
                            for (String curTargetName : session.getTargetServer().getTargetNames()) {
                                responseKeyValuePairs.add(TextParameter.toKeyValuePair(TextKeyword.TARGET_NAME, curTargetName));
                                // add TargetAddress
                                if (sendTargetAddress) responseKeyValuePairs.add(TextParameter.toKeyValuePair(TextKeyword.TARGET_ADDRESS, session.getTargetServer().getConfig().getTargetAddress() + // domain
                                TextKeyword.COLON + // :
                                session.getTargetServer().getConfig().getPort() + // port
                                TextKeyword.COMMA + // ,
                                session.getTargetServer().getConfig().getTargetPortalGroupTag())); // groupTag)
                            }
                        } else {
                            // We're here if they sent us a target name and are asking for the
                            // address (I think)
                            if (sendTargetAddress) responseKeyValuePairs.add(TextParameter.toKeyValuePair(TextKeyword.TARGET_ADDRESS, session.getTargetServer().getConfig().getTargetAddress() + // domain
                            TextKeyword.COLON + // :
                            session.getTargetServer().getConfig().getPort() + // port
                            TextKeyword.COMMA + // ,
                            session.getTargetServer().getConfig().getTargetPortalGroupTag())); // groupTag)
                        }

                    }
                    // concatenate and serialize reply
                    final String replyString = TextParameter.concatenateKeyValuePairs(responseKeyValuePairs);

                    replyDataSegment = ReadWrite.stringToTextDataSegments(replyString, connection.getSettings().getMaxRecvDataSegmentLength())[0];
                }

                responsePdu = TargetPduFactory.createTextResponsePdu(true,// finalFlag
                        false,// continueFlag
                        0,// logicalUnitNumber
                        initiatorTaskTag, 0xffffffff,// targetTransferTag
                        replyDataSegment);// dataSegment

                return responsePdu;
            }
        }, // UnsupportedOpCodeStage checker
        new Checker() {

            @Override
            public ProtocolDataUnit check (final Connection pConnection) throws InterruptedException , IOException , InternetSCSIException {
                ProtocolDataUnit responsePdu;
                final BasicHeaderSegment bhs = new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_TM_REQUEST, "None", "None").getBasicHeaderSegment();
                final InitiatorMessageParser parser = (InitiatorMessageParser) bhs.getParser();

                final FieldPointerSenseKeySpecificData fp = new FieldPointerSenseKeySpecificData(true,// senseKeySpecificDataValid
                true,// commandData (i.e. invalid field in CDB)
                false,// bitPointerValid
                0,// bitPointer, reserved since invalid
                0);// fieldPointer to the SCSI OpCode field

                final FieldPointerSenseKeySpecificData[] fpArray = new FieldPointerSenseKeySpecificData[] { fp };

                // create the whole sense data
                FixedFormatSenseData senseData = new FixedFormatSenseData(false,// valid
                ErrorType.CURRENT,// error type
                false,// file mark
                false,// end of medium
                false,// incorrect length indicator
                SenseKey.ILLEGAL_REQUEST,// sense key
                new FourByteInformation(),// information
                new FourByteInformation(),// command specific information
                AdditionalSenseCodeAndQualifier.INVALID_FIELD_IN_CDB,// additional sense
                                                                     // code and
                // qualifier
                (byte) 0,// field replaceable unit code
                fpArray[0],// sense key specific data, only report
                           // first problem
                new AdditionalSenseBytes());// additional sense bytes

                // keep only the part of the sense data that will be sent
                final ScsiResponseDataSegment dataSegment = new ScsiResponseDataSegment(senseData, parser.getExpectedStatusSequenceNumber());
                final int senseDataSize = senseData.size();

                // calculate residuals and flags
                final int residualCount = Math.abs(parser.getExpectedStatusSequenceNumber() - senseDataSize);
                final boolean residualOverflow = parser.getExpectedStatusSequenceNumber() < senseDataSize;
                final boolean residualUnderflow = parser.getExpectedStatusSequenceNumber() > senseDataSize;

                // create and return PDU
                responsePdu = TargetPduFactory.createSCSIResponsePdu(false,// bidirectionalReadResidualOverflow
                        false,// bidirectionalReadResidualUnderflow
                        residualOverflow,// residualOverflow
                        residualUnderflow,// residualUnderflow,
                        SCSIResponseParser.ServiceResponse.COMMAND_COMPLETED_AT_TARGET,// response,
                        SCSIStatus.CHECK_CONDITION,// status,
                        bhs.getInitiatorTaskTag(),// initiatorTaskTag,
                        0,// snackTag
                        0,// expectedDataSequenceNumber
                        0,// bidirectionalReadResidualCount
                        residualCount,// residualCount
                        dataSegment);// data segment

                return responsePdu;
            }
        }, // FormatUnitStage checker
        new Checker() {

            @Override
            public ProtocolDataUnit check (final Connection pConnection) throws InterruptedException , IOException , InternetSCSIException {
                ProtocolDataUnit responsePdu;
                final BasicHeaderSegment bhs = new ProtocolDataUnitFactory().create(false, true, OperationCode.SCSI_COMMAND, "None", "None").getBasicHeaderSegment();
                final SCSICommandParser parser = (SCSICommandParser) bhs.getParser();

                // Taken from the stage
                final int residualCount = Math.abs(parser.getExpectedDataTransferLength() - 0);
                final boolean residualOverflow = parser.getExpectedDataTransferLength() < 0;
                final boolean residualUnderflow = parser.getExpectedDataTransferLength() > 0;

                responsePdu = TargetPduFactory.createSCSIResponsePdu(false,// bidirectionalReadResidualOverflow
                        false,// bidirectionalReadResidualUnderflow
                        residualOverflow,// residualOverflow,
                        residualUnderflow,// residualUnderflow,
                        SCSIResponseParser.ServiceResponse.COMMAND_COMPLETED_AT_TARGET,// response
                        SCSIStatus.GOOD,// status
                        bhs.getInitiatorTaskTag(),// initiatorTaskTag
                        0,// snackTag
                        0,// expectedDataSequenceNumber
                        0,// bidirectionalReadResidualCount
                        residualCount,// residualCount
                        ScsiResponseDataSegment.EMPTY_DATA_SEGMENT);// data segment

                return responsePdu;
            }
        } } } };
        return returnVal;
    }

    static interface Checker {
        ProtocolDataUnit check (final Connection pConnection) throws InterruptedException , IOException , InternetSCSIException , SettingsException;
    }

}
