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
package org.jscsi.parser.datasegment;

import java.nio.ByteBuffer;

import org.jscsi.exception.InternetSCSIException;

/**
 * <h1>IDataSegment</h1>
 * <p>
 * This interface defines all methods, which a class must to support, if it is a DataSegment.
 * 
 * @author Volker Wildi
 */
public interface IDataSegment {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This method imports <code>len</code> bytes from the given <code>ByteBuffer</code>.
     * 
     * @param src
     *            Source <code>ByteBuffer</code> object.
     * @param len
     *            The number of bytes to import from <code>src</code>.
     * @return The number of bytes, which are imported. Typically, this should
     *         be equal as <code>len</code>.
     */
    @Deprecated
    public int deserialize(final ByteBuffer src, final int len);

    /**
     * This method appends <code>len</code> bytes from the given <code>ByteBuffer</code> at the end of the
     * data buffer of this instance.
     * 
     * @param src
     *            Source <code>ByteBuffer</code> object.
     * @param len
     *            The number of bytes to append from <code>src</code>.
     * @return The number of bytes of the complete data buffer of this instance.
     */
    public int append(final ByteBuffer src, final int len);

    /**
     * This method exports the data buffer to the given <code>ByteBuffer</code> object, which is padded to a
     * integer number of <code>4</code> byte words.
     * 
     * @param dst
     *            Destination <code>ByteBuffer</code> object.
     * @param off
     *            Start position in <code>dst</code>, where to serialize.
     * @return The number of exported bytes (in bytes).
     */
    @Deprecated
    public int serialize(final ByteBuffer dst, final int off);

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns an iterator over the chunks of this data segment in proper
     * sequence.
     * 
     * @return an iterator over the chunks of this data segment in proper
     *         sequence.
     */
    public IDataSegmentIterator iterator();

    /**
     * Returns a <code>SettingsMap</code> instance of this <code>IDataSegment</code> instance. This is only
     * useful with a <code>TextParameterDataSegment</code> instance.
     * 
     * @return The settings of this <code>TextParameterDataSegment</code> instance.
     * @throws InternetSCSIException
     *             if any violation of the iSCSI Standard occurs.
     */
    public SettingsMap getSettings() throws InternetSCSIException;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Clears all made settings of this object. After the call of this method,
     * this object can be reused.
     */
    public void clear();

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the length, which is really used by the <code>dataBuffer</code>.
     * 
     * @return The really used length.
     */
    public int getLength();

    /**
     * Sets the data buffer to the given buffer <code>src</code>. Starting from
     * the position <code>offset</code> with length of <code>len</code>.
     * 
     * @param src
     *            The buffer to read from.
     * @param off
     *            The start offset to read from.
     * @param len
     *            The number of bytes to read.
     * @return The number of bytes really read.
     */
    public int setDataBuffer(final ByteBuffer src, final int off, final int len);

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
