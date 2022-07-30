package org.jscsi.target.connection.stage.fullfeature;


import java.io.IOException;
import java.security.DigestException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.scsi.IResponseData;
import org.jscsi.target.scsi.cdb.InquiryCDB;
import org.jscsi.target.scsi.inquiry.PageCode.VitalProductDataPageName;
import org.jscsi.target.scsi.inquiry.StandardInquiryData;
import org.jscsi.target.scsi.inquiry.SupportedVpdPages;
import org.jscsi.target.scsi.inquiry.UnitSerialNumberPage;
import org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific.FieldPointerSenseKeySpecificData;
import org.jscsi.target.settings.SettingsException;
import org.jscsi.target.util.Debug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A stage for processing <code>INQUIRY</code> SCSI commands.
 *
 * @author Andreas Ergenzinger
 */
public class InquiryStage extends TargetFullFeatureStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(InquiryStage.class);

    public InquiryStage (TargetFullFeaturePhase targetFullFeaturePhase) {
        super(targetFullFeaturePhase);
    }

    @Override
    public void execute (ProtocolDataUnit pdu) throws IOException , InterruptedException , InternetSCSIException , DigestException , SettingsException {

        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final SCSICommandParser parser = (SCSICommandParser) bhs.getParser();

        ProtocolDataUnit responsePdu;// the response PDU

        // get command details in CDB
        if (LOGGER.isDebugEnabled()) {// print CDB bytes
            LOGGER.debug("CDB bytes: \n" + Debug.byteBufferToString(parser.getCDB()));
        }

        final InquiryCDB cdb = new InquiryCDB(parser.getCDB());
        final FieldPointerSenseKeySpecificData[] illegalFieldPointers = cdb.getIllegalFieldPointers();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("cdb.getAllocationLength() = " + cdb.getAllocationLength());
            LOGGER.debug("cdb.getEnableVitalProductData() = " + cdb.getEnableVitalProductData());
            LOGGER.debug("cdb.isNormalACA() = " + cdb.isNormalACA());
            LOGGER.debug("cdb.getPageCode() = " + cdb.getPageCode());
            LOGGER.debug("cdb.getPageCode().getVitalProductDataPageName() = " + cdb.getPageCode().getVitalProductDataPageName());
        }

        if (illegalFieldPointers != null) {
            // an illegal request has been made
            LOGGER.error("illegal INQUIRY request");
            LOGGER.error("CDB bytes: \n" + Debug.byteBufferToString(parser.getCDB()));

            responsePdu = createFixedFormatErrorPdu(illegalFieldPointers, bhs.getInitiatorTaskTag(), parser.getExpectedDataTransferLength());

            // send response
            connection.sendPdu(responsePdu);

        } else {
            // PDU is okay
            // carry out command

            IResponseData responseData;

            // "If the EVPD bit is set to zero, ...
            if (!cdb.getEnableVitalProductData()) {
                // ... the device server shall return the standard INQUIRY
                // data."
                responseData = StandardInquiryData.getInstance();
            } else {
                /*
                 * SCSI initiator is requesting either "device identification" or "supported VPD pages" or this else
                 * block would not have been entered. (see {@link InquiryCDB#checkIntegrity(ByteBuffer dataSegment)})
                 */
                final VitalProductDataPageName pageName = cdb.getPageCode().getVitalProductDataPageName();

                switch (pageName) {// is never null
                    case SUPPORTED_VPD_PAGES :
                        responseData = SupportedVpdPages.getInstance();
                        break;
                    case DEVICE_IDENTIFICATION :
                        responseData = session.getTargetServer().getDeviceIdentificationVpdPage();
                        break;
                    case UNIT_SERIAL_NUMBER :
                        responseData = UnitSerialNumberPage.getInstance();
                        break;
                    default :
                        // The initiator must not request unsupported mode pages.
                        throw new InternetSCSIException();
                }
            }

            // send response
            sendResponse(bhs.getInitiatorTaskTag(), parser.getExpectedDataTransferLength(), responseData);

        }

    }

}
