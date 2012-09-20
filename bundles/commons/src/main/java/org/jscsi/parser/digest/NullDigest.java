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
package org.jscsi.parser.digest;

import java.nio.ByteBuffer;
import java.security.DigestException;

/**
 * <h1>NullDigest</h1>
 * <p>
 * This class represents an empty digest algorithm. So, this class does nothing!
 * 
 * @author Volker Wildi
 */
final class NullDigest implements IDigest {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The size of the digest number (in bytes) to serialize. */
    private static final int DIGEST_SIZE = 0;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a new, empty <code>NullDigest</code> object.
     */
    public NullDigest() {

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public void update(final ByteBuffer data, final int off, final int len) {

    }

    /** {@inheritDoc} */
    public void update(final int b) {

        // do nothing
    }

    /** {@inheritDoc} */
    public void update(final byte[] b, final int off, final int len) {

        // do nothing
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final long getValue() {

        return 0;
    }

    /** {@inheritDoc} */
    public final void reset() {

        // do nothing
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final void validate() throws DigestException {

        // do nothing
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final int getSize() {

        return DIGEST_SIZE;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (o instanceof NullDigest == false)
            return false;
        return true;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
