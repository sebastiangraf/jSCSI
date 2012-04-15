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

import java.nio.ByteBuffer;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.initiator.connection.Connection;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.DataSegmentFactory;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.datasegment.IDataSegment;
import org.jscsi.parser.datasegment.IDataSegmentIterator;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.scsi.SCSICommandDescriptorBlockParser;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.parser.scsi.SCSICommandParser.TaskAttributes;

/**
 * <h1>WriteRequestState</h1>
 * <p/>
 * This state handles a Write Response with unsolicited data.
 * 
 * @author Volker Wildi
 */
public final class WriteRequestState extends AbstractState {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The buffer to used for the message transfer. */
    private final ByteBuffer buffer;

    /** The task attributes of this write operation. */
    private final TaskAttributes taskAttributes;

    /** The expected length in bytes, which should be transfered. */
    private final int expectedDataTransferLength;

    /** The logical block address of the start block for this write operation. */
    private final int logicalBlockAddress;

    /** The start index of the buffer. */
    private final int bufferPosition;

    /**
     * The number of blocks (This block size is dependent on the size used on
     * the target side.) to read.
     */
    private final short transferLength;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a <code>WriteRequestState</code> instance, which
     * creates a request to the iSCSI Target.
     * 
     * @param initConnection
     *            This is the connection, which is used for the network
     *            transmission.
     * @param initBuffer
     *            This buffer should be sent.
     * @param initBufferPosition
     *            The start index of the buffer.
     * @param initTaskAttributes
     *            The task attributes of this task.
     * @param initExpectedDataTransferLength
     *            The expected length in bytes, which should be transfered.
     * @param initLogicalBlockAddress
     *            The logical block address of the first block to write.
     * @param initTransferLength
     *            The number of blocks to write.
     */
    public WriteRequestState(final Connection initConnection,
            final ByteBuffer initBuffer, final int initBufferPosition,
            final TaskAttributes initTaskAttributes,
            final int initExpectedDataTransferLength,
            final int initLogicalBlockAddress, final short initTransferLength) {

        super(initConnection);
        buffer = initBuffer;
        bufferPosition = initBufferPosition;
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

        scsi.setReadExpectedFlag(false);
        scsi.setWriteExpectedFlag(true);
        scsi.setTaskAttributes(taskAttributes);

        scsi.setExpectedDataTransferLength(expectedDataTransferLength);

        final int maxRecvDataSegmentLength = connection
                .getSettingAsInt(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH);
        scsi.setCommandDescriptorBlock(SCSICommandDescriptorBlockParser
                .createWriteMessage(logicalBlockAddress, transferLength));

        final IDataSegment dataSegment = DataSegmentFactory.create(buffer,
                bufferPosition, expectedDataTransferLength,
                DataSegmentFormat.BINARY, maxRecvDataSegmentLength);
        final IDataSegmentIterator iterator = dataSegment.iterator();
        int bufferOffset = 0;

        if (connection.getSettingAsBoolean(OperationalTextKey.IMMEDIATE_DATA)) {
            final int min = Math.min(maxRecvDataSegmentLength, connection
                    .getSettingAsInt(OperationalTextKey.FIRST_BURST_LENGTH));
            protocolDataUnit.setDataSegment(iterator.next(min));
            bufferOffset += min;
        }

        connection.send(protocolDataUnit);

        if (!connection.getSettingAsBoolean(OperationalTextKey.INITIAL_R2T)
                && iterator.hasNext()) {
            connection.nextState(new WriteFirstBurstState(connection, iterator,
                    0xFFFFFFFF, 0, bufferOffset));
        } else {
            connection.nextState(new WriteSecondResponseState(connection,
                    iterator, 0, bufferOffset));
        }
        super.stateFollowing = true;
        // return true;
    }
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
