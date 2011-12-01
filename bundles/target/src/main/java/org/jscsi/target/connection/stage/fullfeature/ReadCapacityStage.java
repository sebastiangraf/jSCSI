package org.jscsi.target.connection.stage.fullfeature;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.scsi.SCSIStatus;
import org.jscsi.target.Target;
import org.jscsi.target.connection.TargetPduFactory;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;

/**
 * A stage for processing <code>READ CAPACITY (10)</code> SCSI commands.
 * @author Andreas Ergenzinger
 */
public class ReadCapacityStage extends TargetFullFeatureStage {
	
	public ReadCapacityStage(TargetFullFeaturePhase targetFullFeaturePhase){
		super(targetFullFeaturePhase);
	}
	
	public void execute(ProtocolDataUnit pdu) throws IOException, InterruptedException, InternetSCSIException {
		
		BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
		
		ByteBuffer dataSegment = ByteBuffer.allocate(8);
		dataSegment.putInt((int)Target.storageModule.getSizeInBlocks());
		dataSegment.putInt(Target.storageModule.getBlockSizeInBytes());
		ProtocolDataUnit responsePDU = TargetPduFactory.createDataInPdu (
				true,//finalFlag
				false,//acknowledgeFlag
				false,//residualOverflowFlag
				false,//residualUnderflowFlag
				true,//statusFlag
				SCSIStatus.GOOD,//status
				0L,//logicalUnitNumber
				bhs.getInitiatorTaskTag(),//initiatorTaskTag
				0xffffffff,//targetTransferTag
				0,//dataSequenceNumber
				0,//bufferOffset
				0,//residualCount
				dataSegment);//dataSegment
		
		connection.sendPdu(responsePDU);
	}
	
}
