/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jscsi.initiator.connection.state;

import org.jscsi.initiator.connection.Connection;
import org.jscsi.initiator.connection.TargetCapacityInformations;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.scsi.SCSICommandDescriptorBlockParser;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.parser.scsi.SCSICommandParser.TaskAttributes;

/**
 * <h1>CapacityRequestState</h1>
 * <p/>
 * This state handles a Capacity Request to retrieve the block size and the size
 * of the iSCSI Device.
 * 
 * @author Volker Wildi
 */
public final class CapacityRequestState extends AbstractState {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The sent command has a fixed size of <code>8</code> bytes. */
    private static final int EXPECTED_DATA_TRANSFER_LENGTH = 0x08;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This object contains the informations about the capacity of the connected
     * target.
     */

    private final TargetCapacityInformations capacityInformation;

    private final TaskAttributes taskAttributes;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a new, empty <code>CapacityRequestState</code>
     * instance.
     * 
     * @param initConnection
     *            This is the connection, which is used for the network
     *            transmission.
     * @param initCapacityInformation
     *            Store the informations about that iSCSI Device in this
     *            instance.
     * @param initTaskAttributes
     *            The task attributes, which are used with task.
     */
    public CapacityRequestState(final Connection initConnection,
            final TargetCapacityInformations initCapacityInformation,
            final TaskAttributes initTaskAttributes) {

        super(initConnection);
        capacityInformation = initCapacityInformation;
        taskAttributes = initTaskAttributes;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final void execute() throws InternetSCSIException {

        final ProtocolDataUnit protocolDataUnit = protocolDataUnitFactory
                .create(false,
                        true,
                        OperationCode.SCSI_COMMAND,
                        connection.getSetting(OperationalTextKey.HEADER_DIGEST),
                        connection.getSetting(OperationalTextKey.DATA_DIGEST));
        final SCSICommandParser scsi = (SCSICommandParser) protocolDataUnit
                .getBasicHeaderSegment().getParser();

        scsi.setReadExpectedFlag(true);
        scsi.setWriteExpectedFlag(false);
        scsi.setTaskAttributes(taskAttributes);

        scsi.setExpectedDataTransferLength(EXPECTED_DATA_TRANSFER_LENGTH);

        scsi.setCommandDescriptorBlock(SCSICommandDescriptorBlockParser
                .createReadCapacityMessage());

        connection.send(protocolDataUnit);
        connection.nextState(new CapacityResponseState(connection,
                capacityInformation));
        super.stateFollowing = true;
        // return true;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
