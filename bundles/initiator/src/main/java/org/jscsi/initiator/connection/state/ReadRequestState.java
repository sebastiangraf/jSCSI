/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
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

import java.nio.ByteBuffer;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.initiator.connection.Connection;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.scsi.SCSICommandDescriptorBlockParser;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.parser.scsi.SCSICommandParser.TaskAttributes;

/**
 * <h1>ReadRequestState</h1>
 * <p/>
 * This state handles a Read Request with some unsolicited data.
 * 
 * @author Volker Wildi
 */
public final class ReadRequestState extends AbstractState {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The buffer to used for the message transfer. */
    private final ByteBuffer buffer;

    /** The task attributes of this read operation. */
    private final TaskAttributes taskAttributes;

    /** The expected length in bytes, which should be transfered. */
    private final int expectedDataTransferLength;

    /** The logical block address of the beginning of the read operation. */
    private final int logicalBlockAddress;

    /**
     * The number of blocks (This block size is dependent on the size used on
     * the target side.) to read.
     */
    private final short transferLength;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a <code>ReadRequestState</code> instance, which
     * creates a request to the iSCSI Target.
     * 
     * @param initConnection
     *            This is the connection, which is used for the network
     *            transmission.
     * @param initBuffer
     *            This buffer should be read.
     * @param initTaskAttributes
     *            The task attributes of this task.
     * @param initExpectedDataTransferLength
     *            The expected length in bytes, which should be transfered.
     * @param initLogicalBlockAddress
     *            The logical block address of the first block to read.
     * @param initTransferLength
     *            The number of blocks to read.
     */
    public ReadRequestState(final Connection initConnection,
            final ByteBuffer initBuffer,
            final TaskAttributes initTaskAttributes,
            final int initExpectedDataTransferLength,
            final int initLogicalBlockAddress, final short initTransferLength) {

        super(initConnection);
        buffer = initBuffer;
        taskAttributes = initTaskAttributes;
        expectedDataTransferLength = initExpectedDataTransferLength;
        logicalBlockAddress = initLogicalBlockAddress;
        transferLength = initTransferLength;
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
        scsi.setExpectedDataTransferLength(expectedDataTransferLength);
        scsi.setCommandDescriptorBlock(SCSICommandDescriptorBlockParser
                .createReadMessage(logicalBlockAddress, transferLength));

        connection.send(protocolDataUnit);
        connection.nextState(new ReadResponseState(connection, buffer, 0, 0));
        super.stateFollowing = true;
        // return true;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
