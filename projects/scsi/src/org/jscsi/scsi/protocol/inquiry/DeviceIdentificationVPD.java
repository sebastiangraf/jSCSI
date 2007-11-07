package org.jscsi.scsi.protocol.inquiry;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Encodable;

public class DeviceIdentificationVPD extends StandardInquiryData {

	@Override
	public <T extends Encodable> T decode(ByteBuffer buffer) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
