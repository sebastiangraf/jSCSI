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
import java.util.zip.Checksum;

/**
 * <h1>IDigest</h1>
 * <p>
 * An interface representing a digest.
 * 
 * @author Volker Wildi
 */
public interface IDigest extends Checksum {

    /**
     * This method updates the used digest with the values of the given <code>ByteBuffer</code> object.
     * 
     * @param data
     *            The values used for updating the checksum.
     * @param off
     *            Start offset.
     * @param len
     *            Length of the used values. (Must be a multiple of <code>4</code> bytes)
     */
    public void update(final ByteBuffer data, final int off, final int len);

    /**
     * This method validates the calculated checksum with the expected checksum.
     * 
     * @throws DigestException
     *             Dismatch between the calculated and expected checksum.
     */
    public void validate() throws DigestException;

    /**
     * Returns the length in bytes, which are needed to store this digest.
     * 
     * @return The number of bytes of this digest.
     */
    public int getSize();

}
