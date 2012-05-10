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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Map;

import org.jscsi.exception.InternetSCSIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>OperationTextKeys</h1>
 * <p>
 * This class encaspulates all methods needed for the operation text keys, which can emerge in the data
 * segment of an iSCSI message (RFC3720).
 * 
 * @author Volker Wildi
 */
final class TextParameterDataSegment extends AbstractDataSegment {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The Logger interface. */
    private static final Logger LOGGER = LoggerFactory.getLogger(TextParameterDataSegment.class);

    /** All strings should be interpreted as this encoding. */
    private static final String DEFAULT_TEXT_ENCODING = "UTF-8";

    /** Delimiter between the key and the value of a key-value-pair. */
    private static final String KEY_VALUE_DELIMITER = "=";

    /** Delimiter between two key-value-pairs. */
    private static final String PAIR_DELIMITER = "\0";

    /**
     * Each line consists of this number of tokens and has the following
     * structure: &lt;key&gt; = &lt;value&gt;.
     */
    private static final int NUMBER_OF_TOKENS = 2;

    /** The index of the key in the array. */
    private static final int KEY_INDEX = 0;

    /** The index of the value in the array. */
    private static final int VALUE_INDEX = 1;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** This data structure contains all the key-value-pairs of this PDU. */
    private final SettingsMap settings;

    /** Flag to indicate that the buffer and/or the setting map are out of sync. */
    private boolean isDirty;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, to create a new, empty <code>TextParameterDataSegment</code> object with a maximum
     * length of <code>initMaximumLength</code> bytes.
     * 
     * @param initChunkSize
     *            The size (in bytes) of one chunk, which represents the <code>MaxRecvDataSegmentLength</code>
     *            .
     */
    public TextParameterDataSegment(final int initChunkSize) {

        super(initChunkSize);

        settings = new SettingsMap();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Add a given operation text keys with the given value to the key value
     * pairs.
     * 
     * @param textKey
     *            One of the valid operation text keys listed above.
     * @param value
     *            The value of this operation text key.
     * @see de.unikn.inf.disy.blockdebix.iscsi.parser.datasegment.OperationalTextKey
     */
    public final void add(final OperationalTextKey textKey, final String value) {

        final String s = textKey.value() + KEY_VALUE_DELIMITER + value + PAIR_DELIMITER;
        resizeBuffer(s.length(), true);
        dataBuffer.put(s.getBytes());

        isDirty = true;
    }

    /**
     * Add all text parameters of the given <code>textKeys</code> map to this <code>ProtocolDataUnit</code>
     * object.
     * 
     * @param textKeys
     *            Map, which contains all the text parameters to insert.
     */
    public final void addAll(final SettingsMap textKeys) {

        for (Map.Entry<OperationalTextKey, String> e : textKeys.entrySet()) {
            add(e.getKey(), e.getValue());
        }

        isDirty = true;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final int append(final ByteBuffer src, final int len) {

        if (len == 0) {
            return 0;
        }

        resizeBuffer(len, true);
        dataBuffer.put(src);

        isDirty = true;

        return dataBuffer.limit();
    }

    /** {@inheritDoc} */
    public final int deserialize(final ByteBuffer src, final int len) {

        if (len == 0) {
            return 0;
        }

        clear();
        resizeBuffer(len, false);

        return length;
    }

    private final void updateSettings() throws InternetSCSIException {

        if (length == 0) {
            return;
        }

        dataBuffer.rewind();

        try {
            // split into key-value pairs
            final String[] data = new String(dataBuffer.array(), DEFAULT_TEXT_ENCODING).split(PAIR_DELIMITER);

            // split the key and value of a key-value pair
            String[] keyValue;
            for (int i = 0; i < data.length; i++) {
                keyValue = data[i].split(KEY_VALUE_DELIMITER);
                if (keyValue.length != NUMBER_OF_TOKENS) {
                    throw new InternetSCSIException("This PDU does not contain a valid key-value-pair.");
                }

                settings.add(OperationalTextKey.valueOfEx(keyValue[KEY_INDEX]), keyValue[VALUE_INDEX]);
            }
        } catch (UnsupportedEncodingException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Unsupported Encoding: " + e.getLocalizedMessage());
            }

            // exception rethrow
            throw new InternetSCSIException(e.getMessage());
        }

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the <code>SettingsMap</code> of this <code>TextParameterDataSegment</code> object.
     * 
     * @return The stored settings of this <code>TextParameterDataSegment</code> object.
     * @throws InternetSCSIException
     *             if any violation of the iSCSI Standard occurs.
     */
    public final SettingsMap getSettings() throws InternetSCSIException {

        if (isDirty) {
            updateSettings();
        }

        return settings;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Clears all stored content of this OperationTextKeys object.
     */
    public final void clear() {

        super.clear();

        settings.clear();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     * 
     * @throws Exception
     */
    public final boolean equals(final Object anObject) {

        if (anObject instanceof TextParameterDataSegment) {
            try {
                final TextParameterDataSegment anotherTPDS = (TextParameterDataSegment)anObject;
                return getSettings().equals(anotherTPDS.getSettings());
            } catch (Exception e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(e.getLocalizedMessage());
                }
            }
        }

        return super.equals(anObject);
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {

        return super.hashCode();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
