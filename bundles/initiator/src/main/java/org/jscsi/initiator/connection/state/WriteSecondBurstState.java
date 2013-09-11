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


import java.util.LinkedList;
import java.util.Queue;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.initiator.connection.Connection;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.data.DataOutParser;
import org.jscsi.parser.datasegment.IDataSegmentIterator;
import org.jscsi.parser.datasegment.IDataSegmentIterator.IDataSegmentChunk;
import org.jscsi.parser.datasegment.OperationalTextKey;


/**
 * <h1>WriteSecondBurstState</h1>
 * <p/>
 * This state handles the second and all following Write Sending States, which sends at most <code>MaxBurstLength</code>
 * bytes in each sequence.
 * 
 * @author Volker Wildi
 */
public final class WriteSecondBurstState extends AbstractState {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The chunk of the data segment to send as next. */
    private final IDataSegmentIterator iterator;

    /**
     * The Target Transfer Tag, which is sent by the iSCSI Target within a Ready2Transfer PDU.
     */
    private final int targetTransferTag;

    /**
     * The desired data transfer length, which the iSCSI Target specified in the last Ready2Transfer message.
     */
    private final int desiredDataTransferLength;

    /** The sequence number of this data package unit. */
    private int dataSequenceNumber;

    /** The start offset of the data to send. */
    private int bufferOffset;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a <code>WriteSecondBurstState</code> instance, which sends the second and all following
     * data sequences.
     * 
     * @param initConnection This is the connection, which is used for the network transmission.
     * @param initIterator The next chunk of the data to send.
     * @param initTargetTransferTag The Target Transfer Tag to use.
     * @param initDesiredDataTransferLength The desired data transfer length, which the iSCSI Target specified in the
     *            last Ready2Transfer message.
     * @param initDataSequenceNumber The Data Sequence Number to use as next.
     * @param initBufferOffset The start offset of the data to send.
     */
    public WriteSecondBurstState (final Connection initConnection, final IDataSegmentIterator initIterator, final int initTargetTransferTag, final int initDesiredDataTransferLength, final int initDataSequenceNumber, final int initBufferOffset) {

        super(initConnection);
        iterator = initIterator;
        targetTransferTag = initTargetTransferTag;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("TTT set to " + targetTransferTag);
        }
        desiredDataTransferLength = initDesiredDataTransferLength;
        // dataSequenceNumber = initDataSequenceNumber;//FIXME always starts at
        // 0
        dataSequenceNumber = 0;
        bufferOffset = initBufferOffset;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final void execute () throws InternetSCSIException {

        final Queue<ProtocolDataUnit> protocolDataUnits = new LinkedList<ProtocolDataUnit>();

        ProtocolDataUnit protocolDataUnit;
        DataOutParser dataOut;
        IDataSegmentChunk dataSegmentChunk;
        boolean finalFlag = false;
        final int maxRecvDataSegmentLength = connection.getSettingAsInt(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH);
        int bytes2Transfer = Math.min(connection.getSettingAsInt(OperationalTextKey.MAX_BURST_LENGTH), desiredDataTransferLength);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("bytes2Transfer: " + bytes2Transfer + " iterator.hasNext(): " + iterator.hasNext());
        }

        while (bytes2Transfer > 0 && iterator.hasNext()) {
            if (bytes2Transfer <= maxRecvDataSegmentLength) {
                dataSegmentChunk = iterator.next(bytes2Transfer);
                finalFlag = true;
            } else {
                dataSegmentChunk = iterator.next(maxRecvDataSegmentLength);
                finalFlag = false;
            }

            protocolDataUnit = protocolDataUnitFactory.create(false, finalFlag, OperationCode.SCSI_DATA_OUT, connection.getSetting(OperationalTextKey.HEADER_DIGEST), connection.getSetting(OperationalTextKey.DATA_DIGEST));
            protocolDataUnit.getBasicHeaderSegment().setInitiatorTaskTag(connection.getSession().getInitiatorTaskTag());

            dataOut = (DataOutParser) protocolDataUnit.getBasicHeaderSegment().getParser();

            dataOut.setTargetTransferTag(targetTransferTag);
            dataOut.setDataSequenceNumber(dataSequenceNumber++);
            dataOut.setBufferOffset(bufferOffset);
            bufferOffset += maxRecvDataSegmentLength;

            protocolDataUnit.setDataSegment(dataSegmentChunk);

            protocolDataUnits.offer(protocolDataUnit);
            bytes2Transfer -= maxRecvDataSegmentLength;
        }

        connection.send(protocolDataUnits);
        connection.nextState(new WriteSecondResponseState(connection, iterator, dataSequenceNumber, bufferOffset));

        // return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean nextStateFollowing () {
        return true;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
