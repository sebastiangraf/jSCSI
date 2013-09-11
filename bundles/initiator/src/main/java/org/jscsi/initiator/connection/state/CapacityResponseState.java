/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met: * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution. * Neither the name of the University of Konstanz nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jscsi.initiator.connection.state;


import org.jscsi.exception.InternetSCSIException;
import org.jscsi.initiator.connection.Connection;
import org.jscsi.initiator.connection.TargetCapacityInformations;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.data.DataInParser;
import org.jscsi.parser.scsi.SCSICommandParser.TaskAttributes;
import org.jscsi.parser.scsi.SCSIResponseParser;
import org.jscsi.parser.scsi.SCSIStatus;


/**
 * <h1>CapacityResponseState</h1>
 * <p/>
 * This state handles a Capacity Response to extract the block size and the size of the iSCSI Device.
 * 
 * @author Volker Wildi
 */
public final class CapacityResponseState extends AbstractState {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This object contains the informations about the capacity of the connected target.
     */
    private final TargetCapacityInformations capacityInformation;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a new, empty <code>CapacityResponseState</code> instance.
     * 
     * @param initConnection This is the connection, which is used for the network transmission.
     * @param initCapacityInformation Store the extracted informations in this instance.
     */
    protected CapacityResponseState (final Connection initConnection, final TargetCapacityInformations initCapacityInformation) {

        super(initConnection);
        capacityInformation = initCapacityInformation;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final void execute () throws InternetSCSIException {

        final ProtocolDataUnit protocolDataUnit = connection.receive();

        // first, we extract capacity informations
        if (!(protocolDataUnit.getBasicHeaderSegment().getParser() instanceof DataInParser)) {

            // In newer versions of iscsi targets there the target tells the initiator
            // that the status is cleared using a scsi response. It's defined in the RFC 3720 on page 78 (or
            // at least mentioned, it's
            // actually defined in SAM-2 and). This is why we have to ask for capacity informations once again
            // receiving this
            // Response to our capacity request.
            if (protocolDataUnit.getBasicHeaderSegment().getParser() instanceof SCSIResponseParser) {
                connection.nextState(new CapacityRequestState(connection, capacityInformation, TaskAttributes.SIMPLE));
                super.stateFollowing = true;
                return;
            } else {
                throw new InternetSCSIException(protocolDataUnit.getBasicHeaderSegment().getParser().getClass().getSimpleName() + " is not the expected type of PDU.");
            }
        }

        /**
         * The server responded using the data-in-parser.
         */
        final DataInParser parser = (DataInParser) protocolDataUnit.getBasicHeaderSegment().getParser();

        capacityInformation.deserialize(protocolDataUnit.getDataSegment());

        if (!parser.isStatusFlag() || parser.getStatus() != SCSIStatus.GOOD) {
            // receive SCSI Response PDU and check status (no phase
            // collapse)
            final ProtocolDataUnit scsiPdu = connection.receive();
            if (scsiPdu.getBasicHeaderSegment().getOpCode() == OperationCode.SCSI_RESPONSE) {
                final SCSIResponseParser scsiParser = (SCSIResponseParser) scsiPdu.getBasicHeaderSegment().getParser();
                if (scsiParser.getStatus() == SCSIStatus.GOOD) return;// done
            }
            throw new InternetSCSIException("Error: Task did not finish successfully.");
        }

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
