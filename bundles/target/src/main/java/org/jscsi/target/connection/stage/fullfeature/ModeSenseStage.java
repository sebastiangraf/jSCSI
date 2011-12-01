package org.jscsi.target.connection.stage.fullfeature;

import java.io.IOException;
import java.security.DigestException;

import org.apache.log4j.Logger;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.target.Target;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.scsi.cdb.ModeSense6Cdb;
import org.jscsi.target.scsi.modeSense.CachingModePage;
import org.jscsi.target.scsi.modeSense.HeaderType;
import org.jscsi.target.scsi.modeSense.InformationExceptionsControlModePage;
import org.jscsi.target.scsi.modeSense.ModePage;
import org.jscsi.target.scsi.modeSense.ModePageCode;
import org.jscsi.target.scsi.modeSense.ModeParameterList;
import org.jscsi.target.scsi.modeSense.ModeParameterListBuilder;
import org.jscsi.target.scsi.modeSense.ShortLogicalBlockDescriptor;
import org.jscsi.target.settings.SettingsException;

/**
 * A stage for processing <code>MODE SENSE (6)</code> SCSI commands.
 * @author Andreas Ergenzinger
 */
public final class ModeSenseStage extends TargetFullFeatureStage {
	
	private static final Logger LOGGER = Logger.getLogger(ModeSenseStage.class);
	
	public ModeSenseStage(TargetFullFeaturePhase targetFullFeaturePhase) {
		super(targetFullFeaturePhase);
	}

	@Override
	public void execute(final ProtocolDataUnit pdu) throws IOException,
			InterruptedException, InternetSCSIException, DigestException,
			SettingsException {
		
		final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
		final SCSICommandParser parser = (SCSICommandParser)bhs.getParser();
		final ModeSense6Cdb cdb = new ModeSense6Cdb(parser.getCDB());
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(cdb.getDisableBlockDescriptors());
			LOGGER.debug(cdb.getPageControl());
			LOGGER.debug(cdb.getPageCode());
			LOGGER.debug(cdb.getSubpageCode());
			LOGGER.debug("cdb.getAllocationLength() = " + cdb.getAllocationLength());
			LOGGER.debug(cdb.getModePage());
		}
		
		//final PageControl pageControl = cdb.getPageControl();//see 8 lines below
		final ModePageCode modePageCode = cdb.getModePage();
		
		//ModeParameterList and ModeParameterListBuilder common to
		//all supported ModePage requests
		
		ModePage[] modePages = null;
		if (modePageCode == ModePageCode.INFORMATIONAL_EXCEPTIONS_CONTROL_MODE_PAGE) {
			//TODO this should to be made dynamic wrt. cdb.getPageControl();
			
			modePages = new ModePage[]{getInformationExceptionsControlModePage()};
			
		} else if (modePageCode == ModePageCode.CACHING_MODE_PAGE) {
			
			modePages = new ModePage[]{getCachingModePage()};
			
		} else if (modePageCode == ModePageCode.RETURN_ALL_MODE_PAGES_ONLY) {
			
			modePages = new ModePage[]{
					getInformationExceptionsControlModePage(),
					getCachingModePage()};
			
		}//else modeParameterList stays null
		
		//create and send response PDU
		if (modePages != null) {
			
			//create ModeParameterList
			final ModeParameterListBuilder builder = new ModeParameterListBuilder(
					HeaderType.MODE_PARAMETER_HEADER_6);
			builder.setLogicalBlockDescriptors(
					new ShortLogicalBlockDescriptor(
							Target.storageModule.getSizeInBlocks(),//numberOfLogicalBlocks
							Target.storageModule.getBlockSizeInBytes()));//logicalBlockLength
			builder.setModePages(modePages);
			ModeParameterList modeParameterList = ModeParameterList.build(builder);
			
			//send response
			sendResponse(
					bhs.getInitiatorTaskTag(),//initiatorTaskTag,
					parser.getExpectedDataTransferLength(),//expectedDataTransferLength,
					modeParameterList);//responseData
			
		} else {
			/*
			 * The initiator has requested a mode sense page which the
			 * jSCSI Target cannot provide.
			 * 
			 * This could be answered with an Illegal field in CDB message
			 * but, there is no good way to identify the exact field at
			 * fault.
			 */
			throw new InternetSCSIException();
		}
		
	}
	
	private static final InformationExceptionsControlModePage
	getInformationExceptionsControlModePage() {
		return new InformationExceptionsControlModePage(
				false,//parametersSaveable
				false,//performance
				false,//enableBackgroundFunction
				false,//enableWarning
				true,//disableExceptionControl
				false,//test
				false,//logErrors
				0x0,//methodOfReportingInformationalExceptionConditions
				0,//intervalTimer
				0);//reportCount
	}
	
	private static final CachingModePage getCachingModePage() {
		return new CachingModePage(
				false,//parametersSaveable
				false,//initiatorControl
				true,//abortPrefetch
				false,//cachingAnalysisPermitted
				false,//discontinuity
				true,//sizeEnable
				false,//writebackCacheEnable
				false,//multiplicationFactor
				true,//readCacheDisable
				0x0,//demandReadRetentionPriority
				0x0,//writeRetentionPriority
				0,//disablePrefetchTransferLength
				0,//minimumPrefetch
				65535,//maximumPrefetch
				65535,//maximumPrefetchCeiling
				true,//forceSequentialWrite
				false,//logicalBlockCacheSegmentSize
				false,//disableReadAhead
				false,//nonVolatileCacheDisabled
				20,//numberOfCacheSegments
				0);//cacheSegmentSize 
	}
}
