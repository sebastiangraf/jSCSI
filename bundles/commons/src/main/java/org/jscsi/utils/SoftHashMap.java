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
package org.jscsi.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <h1>SoftHashMap</h1>
 * <p>
 * Based on the SoftHashMap implemented by Dr. Heinz Kabutz.
 * </p>
 * <p>
 * Hash map based on soft references. The hash map always makes sure a limited amount of strong references it
 * maintained in FIFO order to _simulate_ LRU.
 * </p>
 * <p>
 * Note that the put and remove methods always return null.
 * </p>
 * 
 * @param <K>
 *            Key object of type K.
 * @param <V>
 *            Value object of type V.
 * 
 */
public final class SoftHashMap<K, V> extends AbstractMap<K, V> {

    /** Default strong reference count. */
    private static final int DEFAULT_STRONG_REFERENCE_COUNT = 32;

    /** The internal HashMap that will hold the SoftReference. */
    private final Map<K, SoftReference<V>> internalMap;

    /** The number of "hard" references to hold internally. */
    private final int strongReferenceCount;

    /** The FIFO list of strong references, order of last access. */
    private V[] strongReferenceArray;

    /** Current offset of FIFO list. */
    private int currentStrongReferenceOffset;

    /** Reference queue for cleared SoftReference objects. */
    private final ReferenceQueue<SoftValue<V>> queue;

    /**
     * Default constructor internally using 32 strong references.
     */
    public SoftHashMap() {

        this(DEFAULT_STRONG_REFERENCE_COUNT);
    }

    /**
     * Constructor that allows to specify how many strong references should be
     * used internally.
     * 
     * @param initStrongReferenceCount
     *            Number of internal strong references.
     */
    @SuppressWarnings("unchecked")
    public SoftHashMap(final int initStrongReferenceCount) {

        internalMap = new HashMap<K, SoftReference<V>>();
        strongReferenceCount = initStrongReferenceCount;
        strongReferenceArray = (V[])new Object[initStrongReferenceCount];
        currentStrongReferenceOffset = 0;
        queue = new ReferenceQueue<SoftValue<V>>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final V get(final Object key) {

        V value = null;
        final SoftReference<V> softReference = internalMap.get(key);
        if (softReference != null) {
            // Soft reference was garbage collected.
            value = softReference.get();
            if (value == null) {
                // Reflect garbage collected soft reference in internal hash
                // map.
                internalMap.remove(key);
            } else {
                synchronized (strongReferenceArray) {
                    // FIFO on strong references.
                    strongReferenceArray[currentStrongReferenceOffset++] = value;
                    // Assure FIFO does not grow beyond strongReferenceCount.
                    if (currentStrongReferenceOffset >= strongReferenceCount) {
                        currentStrongReferenceOffset = 0;
                    }
                }
            }
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final V put(final K key, final V value) {

        processQueue();
        internalMap.put(key, new SoftValue<V>(value, key, queue));
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final V remove(final Object key) {

        processQueue();
        internalMap.remove(key);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public final synchronized void clear() {

        strongReferenceArray = (V[])new Object[strongReferenceCount];
        processQueue();
        internalMap.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int size() {

        processQueue();
        return internalMap.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Set<Map.Entry<K, V>> entrySet() {

        throw new UnsupportedOperationException();
    }

    /**
     * Remove garbage collected soft values with the help of the reference
     * queue.
     */
    @SuppressWarnings({
        "rawtypes", "unchecked"
    })
    private final void processQueue() {

        SoftValue<V> softValue;
        while ((softValue = (SoftValue)queue.poll()) != null) {
            internalMap.remove(softValue.key);
        }
    }

    /**
     * Internal subclass to store keys and values for more convenient lookups.
     */
    private final class SoftValue<T extends V> extends SoftReference<V> {

        private final K key;

        /**
         * Constructor.
         * 
         * @param initValue
         *            Value wrapped as soft reference.
         * @param initKey
         *            Key for given value.
         * @param initReferenceQueue
         *            Reference queue for cleanup.
         */

        @SuppressWarnings({
            "rawtypes", "unchecked"
        })
        private SoftValue(final V initValue, final K initKey, final ReferenceQueue initReferenceQueue) {

            super(initValue, initReferenceQueue);
            key = initKey;
        }
    }

}
