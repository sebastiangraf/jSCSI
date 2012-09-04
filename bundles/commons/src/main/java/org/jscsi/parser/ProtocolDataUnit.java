/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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
package org.jscsi.parser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;
import java.security.DigestException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.datasegment.AbstractDataSegment;
import org.jscsi.parser.datasegment.IDataSegmentIterator.IDataSegmentChunk;
import org.jscsi.parser.digest.IDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>ProtocolDataUnit</h1>
 * <p>
 * This class encapsulates a Protocol Data Unit (PDU), which is defined in the iSCSI Standard (RFC 3720).
 * 
 * @author Volker Wildi
 */
public final class ProtocolDataUnit {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The initial size of the Additional Header Segment. */
    private static final int AHS_INITIAL_SIZE = 0;

    /** The Log interface. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolDataUnit.class);

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The Basic Header Segment of this PDU. */
    private final BasicHeaderSegment basicHeaderSegment;

    /** The Additional Header Segment 1...n (optional) of this PDU. */
    private final AbstractList<AdditionalHeaderSegment> additionalHeaderSegments;

    /**
     * The (optional) Data Segment contains PDU associated data. Its payload
     * effective length is provided in the BHS field - <code>DataSegmentLength</code>. The Data Segment is
     * also padded to
     * multiple of a <code>4</code> byte words.
     */
    private ByteBuffer dataSegment;

    /**
     * Optional header and data digests protect the integrity of the header and
     * data, respectively. The digests, if present, are located, respectively,
     * after the header and PDU-specific data, and cover respectively the header
     * and the PDU data, each including the padding bytes, if any.
     * <p>
     * <b>The existence and type of digests are negotiated during the Login Phase. </b>
     * <p>
     * The separation of the header and data digests is useful in iSCSI routing applications, in which only
     * the header changes when a message is forwarded. In this case, only the header digest should be
     * recalculated.
     * <p>
     * Digests are not included in data or header length fields.
     * <p>
     * A zero-length Data Segment also implies a zero-length data-digest.
     */

    /** Digest of the header of this PDU. */
    private IDigest headerDigest;

    /** Digest of the data segment of this PDU. */
    private IDigest dataDigest;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty ProtcolDataUnit object.
     * 
     * @param initHeaderDigest
     *            The instance of the digest to use for the Basic Header Segment
     *            protection.
     * @param initDataDigest
     *            The instance of the digest to use for the Data Segment
     *            protection.
     */
    public ProtocolDataUnit(final IDigest initHeaderDigest, final IDigest initDataDigest) {

        basicHeaderSegment = new BasicHeaderSegment();
        headerDigest = initHeaderDigest;

        additionalHeaderSegments = new ArrayList<AdditionalHeaderSegment>(AHS_INITIAL_SIZE);

        dataSegment = ByteBuffer.allocate(0);
        dataDigest = initDataDigest;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Serialize all informations of this PDU object to its byte representation.
     * 
     * @return The byte representation of this PDU.
     * @throws InternetSCSIException
     *             If any violation of the iSCSI-Standard emerge.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public final ByteBuffer serialize() throws InternetSCSIException, IOException {

        basicHeaderSegment.getParser().checkIntegrity();

        final ByteBuffer pdu = ByteBuffer.allocate(calcSize());

        int offset = 0;
        offset += basicHeaderSegment.serialize(pdu, offset);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Serialized Basic Header Segment:\n" + toString());
        }

        offset += serializeAdditionalHeaderSegments(pdu, offset);

        // write header digest
        // TODO: Move CRC calculation in BasicHeaderSegment.serialize?
        if (basicHeaderSegment.getParser().canHaveDigests()) {
            offset += serializeDigest(pdu, headerDigest);
        }

        // serialize data segment
        offset += serializeDataSegment(pdu, offset);

        // write data segment digest
        // TODO: Move CRC calculation in BasicHeaderSegment.serialize?
        if (basicHeaderSegment.getParser().canHaveDigests()) {
            offset += serializeDigest(pdu, dataDigest);
        }

        return (ByteBuffer)pdu.rewind();
    }

    /**
     * Deserializes (parses) a given byte representation of a PDU to an PDU
     * object.
     * 
     * @param pdu
     *            The byte representation of an PDU to parse.
     * @return The number of bytes, which are serialized.
     * @throws InternetSCSIException
     *             If any violation of the iSCSI-Standard emerge.
     * @throws IOException
     *             if an I/O error occurs.
     * @throws DigestException
     *             There is a mismatch of the digest.
     */
    public final int deserialize(final ByteBuffer pdu) throws InternetSCSIException, IOException,
        DigestException {

        int offset = deserializeBasicHeaderSegment(pdu);

        offset += deserializeAdditionalHeaderSegments(pdu, offset);

        offset += deserializeDataSegment(pdu, offset);

        basicHeaderSegment.getParser().checkIntegrity();

        return offset;
    }

    /**
     * Deserializes a given array starting from offset <code>0</code> and store
     * the informations in the BasicHeaderSegment object..
     * 
     * @param bhs
     *            The array to read from.
     * @throws InternetSCSIException
     *             If any violation of the iSCSI-Standard emerge.
     * @throws DigestException
     *             There is a mismatch of the digest.
     */
    private final int deserializeBasicHeaderSegment(final ByteBuffer bhs) throws InternetSCSIException,
        DigestException {

        int len = basicHeaderSegment.deserialize(this, bhs);

        // read header digest and validate
        if (basicHeaderSegment.getParser().canHaveDigests()) {
            len +=
                deserializeDigest(bhs, bhs.position() - BasicHeaderSegment.BHS_FIXED_SIZE,
                    BasicHeaderSegment.BHS_FIXED_SIZE, headerDigest);
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Deserialized Basic Header Segment:\n" + toString());
        }

        return len;
    }

    /**
     * Deserializes a array (starting from offset <code>0</code>) and store the
     * informations to the <code>AdditionalHeaderSegment</code> object.
     * 
     * @param pdu
     *            The array to read from.
     * @return The length of the read bytes.
     * @throws InternetSCSIException
     *             If any violation of the iSCSI-Standard emerge.
     */
    private final int deserializeAdditionalHeaderSegments(final ByteBuffer pdu) throws InternetSCSIException {

        return deserializeAdditionalHeaderSegments(pdu, 0);
    }

    /**
     * Deserializes a array (starting from the given offset) and store the
     * informations to the <code>AdditionalHeaderSegment</code> object.
     * 
     * @param pdu
     *            The <code>ByteBuffer</code> to read from.
     * @param offset
     *            The offset to start from.
     * @return The length of the written bytes.
     * @throws InternetSCSIException
     *             If any violation of the iSCSI-Standard emerge.
     */
    private final int deserializeAdditionalHeaderSegments(final ByteBuffer pdu, final int offset)
        throws InternetSCSIException {

        // parsing Additional Header Segment
        int off = offset;
        int ahsLength = basicHeaderSegment.getTotalAHSLength();
        while (ahsLength != 0) {
            final AdditionalHeaderSegment tmpAHS = new AdditionalHeaderSegment();
            tmpAHS.deserialize(pdu, off);

            additionalHeaderSegments.add(tmpAHS);
            ahsLength -= tmpAHS.getLength();

            off += tmpAHS.getSpecificField().position();
        }

        return off - offset;
    }

    /**
     * Serialize all the contained additional header segments to the destination
     * array starting from the given offset.
     * 
     * @param dst
     *            The destination array to write in.
     * @param offset
     *            The offset to start to write in <code>dst</code>.
     * @return The written length.
     * @throws InternetSCSIException
     *             If any violation of the iSCSI-Standard emerge.
     */
    private final int serializeAdditionalHeaderSegments(final ByteBuffer dst, final int offset)
        throws InternetSCSIException {

        int off = offset;
        for (AdditionalHeaderSegment ahs : additionalHeaderSegments) {
            off += ahs.serialize(dst, off);
        }

        return off - offset;
    }

    /**
     * Serializes the data segment (binary or key-value pairs) to a destination
     * array, staring from offset to write.
     * 
     * @param dst
     *            The array to write in.
     * @param offset
     *            The start offset to start from in <code>dst</code>.
     * @return The written length.
     * @throws InternetSCSIException
     *             If any violation of the iSCSI-Standard emerge.
     */
    public final int serializeDataSegment(final ByteBuffer dst, final int offset)
        throws InternetSCSIException {

        dataSegment.rewind();
        dst.position(offset);
        dst.put(dataSegment);

        return dataSegment.limit();
    }

    /**
     * Deserializes a array (starting from the given offset) and store the
     * informations to the Data Segment.
     * 
     * @param pdu
     *            The array to read from.
     * @param offset
     *            The offset to start from.
     * @return The length of the written bytes.
     * @throws IOException
     *             if an I/O error occurs.
     * @throws InternetSCSIException
     *             If any violation of the iSCSI-Standard emerge.
     * @throws DigestException
     *             There is a mismatch of the digest.
     */
    private final int deserializeDataSegment(final ByteBuffer pdu, final int offset) throws IOException,
        InternetSCSIException, DigestException {

        final int length = basicHeaderSegment.getDataSegmentLength();

        if (dataSegment == null || dataSegment.limit() < length) {
            dataSegment = ByteBuffer.allocate(AbstractDataSegment.getTotalLength(length));
        }
        dataSegment.put(pdu);

        dataSegment.flip();

        // read data segment digest and validate
        if (basicHeaderSegment.getParser().canHaveDigests()) {
            deserializeDigest(pdu, offset, length, dataDigest);
        }

        if (dataSegment == null) {
            return 0;
        } else {
            return dataSegment.limit();
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Writes this <code>ProtocolDataUnit</code> object to the given <code>SocketChannel</code>.
     * 
     * @param sChannel
     *            <code>SocketChannel</code> to write to.
     * @return The number of bytes written, possibly zero.
     * @throws InternetSCSIException
     *             if any violation of the iSCSI-Standard emerge.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public final int write(final SocketChannel sChannel) throws InternetSCSIException, IOException {

        // print debug informations
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(basicHeaderSegment.getParser().getShortInfo());
        }

        final ByteBuffer src = serialize();
        int length = 0;

        while (length < src.limit()) {
            length += sChannel.write(src);
        }

        return length;
    }

    /**
     * Reads from the given <code>SocketChannel</code> all the neccassary bytes
     * to fill this PDU.
     * 
     * @param sChannel
     *            <code>SocketChannel</code> to read from.
     * @return The number of bytes, possibly zero,or <code>-1</code> if the
     *         channel has reached end-of-stream
     * @throws IOException
     *             if an I/O error occurs.
     * @throws InternetSCSIException
     *             if any violation of the iSCSI-Standard emerge.
     * @throws DigestException
     *             if a mismatch of the digest exists.
     */
    public final int read(final SocketChannel sChannel) throws InternetSCSIException, IOException,
        DigestException {

        // read Basic Header Segment first to determine the total length of this
        // Protocol Data Unit.
        clear();

        final ByteBuffer bhs = ByteBuffer.allocate(BasicHeaderSegment.BHS_FIXED_SIZE);
        int len = 0;
        while (len < BasicHeaderSegment.BHS_FIXED_SIZE) {
            int lens = sChannel.read(bhs);
            if (lens == -1) {
                // The Channel was closed at the Target (e.g. the Target does
                // not support Multiple Connections)
                throw new ClosedChannelException();
            }
            len += lens;
            LOGGER.trace("Receiving through SocketChannel: " + len + " of maximal "
                + BasicHeaderSegment.BHS_FIXED_SIZE);

        }
        bhs.flip();

        deserializeBasicHeaderSegment(bhs);
        // check for further reading
        if (getBasicHeaderSegment().getTotalAHSLength() > 0) {
            final ByteBuffer ahs = ByteBuffer.allocate(basicHeaderSegment.getTotalAHSLength());
            int ahsLength = 0;
            while (ahsLength < getBasicHeaderSegment().getTotalAHSLength()) {
                ahsLength += sChannel.read(ahs);
            }
            len += ahsLength;
            ahs.flip();

            deserializeAdditionalHeaderSegments(ahs);
        }
        if (basicHeaderSegment.getDataSegmentLength() > 0) {
            dataSegment =
                ByteBuffer.allocate(AbstractDataSegment.getTotalLength(basicHeaderSegment
                    .getDataSegmentLength()));
            int dataSegmentLength = 0;
            while (dataSegmentLength < basicHeaderSegment.getDataSegmentLength()) {
                dataSegmentLength += sChannel.read(dataSegment);
            }
            len += dataSegmentLength;
            dataSegment.flip();
        }

        // print debug informations
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(basicHeaderSegment.getParser().getShortInfo());
        }

        return len;
    }

    /**
     * Clears all stored content of this ProtocolDataUnit object.
     */
    public final void clear() {

        basicHeaderSegment.clear();

        headerDigest.reset();

        additionalHeaderSegments.clear();

        dataSegment.clear();
        dataSegment.flip();

        dataDigest.reset();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns an iterator to all contained Additional Header Segment in this
     * PDU.
     * 
     * @return The iterator to the contained Additional Header Segment.
     * @see AdditionalHeaderSegment
     */
    public final Iterator<AdditionalHeaderSegment> getAdditionalHeaderSegments() {

        return additionalHeaderSegments.iterator();
    }

    /**
     * Returns the Basic Header Segment contained in this PDU.
     * 
     * @return The Basic Header Segment.
     * @see BasicHeaderSegment
     */
    public final BasicHeaderSegment getBasicHeaderSegment() {

        return basicHeaderSegment;
    }

    /**
     * Gets the data segment in this PDU.
     * 
     * @return The data segment of this <code>ProtocolDataUnit</code> object.
     */
    public final ByteBuffer getDataSegment() {

        return dataSegment;
    }

    public final void setDataSegment(final ByteBuffer dataSegment) {
        dataSegment.clear();
        this.dataSegment = dataSegment;
        basicHeaderSegment.setDataSegmentLength(dataSegment.capacity());
    }

    /**
     * Sets a new data segment in this PDU.
     * 
     * @param chunk
     *            The new data segment of this <code>ProtocolDataUnit</code> object.
     */
    public final void setDataSegment(final IDataSegmentChunk chunk) {

        if (chunk == null) {
            throw new NullPointerException();
        }

        dataSegment = ByteBuffer.allocate(chunk.getTotalLength());
        dataSegment.put(chunk.getData());
        basicHeaderSegment.setDataSegmentLength(chunk.getLength());
    }

    /**
     * Returns the instance of the used digest algorithm for the header.
     * 
     * @return The instance of the header digest.
     */
    public final IDigest getHeaderDigest() {

        return headerDigest;
    }

    /**
     * Sets the digest of the header to use for data integrity.
     * 
     * @param newHeaderDigest
     *            An instance of the new header digest.
     */
    public final void setHeaderDigest(final IDigest newHeaderDigest) {

        headerDigest = newHeaderDigest;
    }

    /**
     * Returns the instance of the used digest algorithm for the data segment.
     * 
     * @return The instance of the data digest.
     */
    public final IDigest getDataDigest() {

        return dataDigest;
    }

    /**
     * Sets the digest of the data segment to use for data integrity.
     * 
     * @param newDataDigest
     *            An instance of the new data segment digest.
     */
    public final void setDataDigest(final IDigest newDataDigest) {

        dataDigest = newDataDigest;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public final String toString() {

        final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

        sb.append(basicHeaderSegment.toString());

        for (AdditionalHeaderSegment ahs : additionalHeaderSegments) {
            sb.append(ahs.toString());
        }

        return sb.toString();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Calculates the needed size (in bytes) of serializing this object.
     * 
     * @return The needed size to store this object.
     */
    private final int calcSize() {

        int size = BasicHeaderSegment.BHS_FIXED_SIZE;
        size += basicHeaderSegment.getTotalAHSLength() * AdditionalHeaderSegment.AHS_FACTOR;

        // plus the sizes of the used digests
        size += headerDigest.getSize();
        size += dataDigest.getSize();

        size += AbstractDataSegment.getTotalLength(basicHeaderSegment.getDataSegmentLength());

        return size;
    }

    private final int serializeDigest(final ByteBuffer pdu, final IDigest digest) {

        final int size = digest.getSize();
        if (size > 0) {
            digest.reset();
            pdu.mark();
            digest.update(pdu, 0, BasicHeaderSegment.BHS_FIXED_SIZE);
            pdu.putInt((int)digest.getValue());
            pdu.reset();
        }

        return size;
    }

    private final int deserializeDigest(final ByteBuffer pdu, final int offset, final int length,
        final IDigest digest) throws DigestException {

        pdu.mark();
        digest.update(pdu, offset, length);
        digest.validate();
        pdu.reset();

        return digest.getSize();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
