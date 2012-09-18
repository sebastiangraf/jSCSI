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

import org.jscsi.parser.digest.DigestFactory;

/**
 * <h1>ProtocolDataUnitFactory</h1>
 * <p/>
 * A factory to create all supported the <code>ProtocolDataUnit</code> instances.
 * 
 * @author Volker Wildi
 */
public final class ProtocolDataUnitFactory {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The factory to create the supported digests. */
    private final DigestFactory digestFactory = new DigestFactory();

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor to create a new, empty <code>ProtocolDataUnitFactory</code> instance.
     */
    public ProtocolDataUnitFactory() {

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This method creates a <code>ProtocolDataUnit</code> instance, which
     * initializes only the digests to use, and returns it.
     * 
     * @param headerDigest
     *            The name of the digest to use for the protection of the Basic
     *            Header Segment.
     * @param dataDigest
     *            The name of the digest to use for the protection of the Data
     *            Segment.
     * @return A new <code>ProtocolDataUnit</code> instance.
     */
    public final ProtocolDataUnit create(final String headerDigest, final String dataDigest) {

        return new ProtocolDataUnit(digestFactory.create(headerDigest), digestFactory.create(dataDigest));
    }

    /**
     * This method creates a <code>ProtocolDataUnit</code> instance, which is
     * initialized with the given settings, and returns it.
     * 
     * @param immediateFlag
     *            Should this PDU send immediately?
     * @param operationCode
     *            The Operation Code of this PDU.
     * @param finalFlag
     *            Is this PDU the last one in a sequence?
     * @param headerDigest
     *            The name of the digest to use for the protection of the Basic
     *            Header Segment.
     * @param dataDigest
     *            The name of the digest to use for the protection of the Data
     *            Segment.
     * @return A new <code>ProtocolDataUnit</code> instance.
     */
    public final ProtocolDataUnit create(final boolean immediateFlag, final boolean finalFlag,
        final OperationCode operationCode, final String headerDigest, final String dataDigest) {

        final ProtocolDataUnit protocolDataUnit =
            new ProtocolDataUnit(digestFactory.create(headerDigest), digestFactory.create(dataDigest));

        protocolDataUnit.getBasicHeaderSegment().setImmediate(immediateFlag);
        protocolDataUnit.getBasicHeaderSegment().setFinal(finalFlag);
        protocolDataUnit.getBasicHeaderSegment().setOperationCode(protocolDataUnit, operationCode);

        return protocolDataUnit;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
