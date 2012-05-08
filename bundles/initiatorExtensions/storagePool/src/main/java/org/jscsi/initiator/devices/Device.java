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
package org.jscsi.initiator.devices;

/**
 * <h1>Device</h1>
 * <p>
 * A Device is an interface to control e.g. iSCSI connections between an
 * initiator and a target. It provides methods to read and write data.
 * </p>
 * <p>
 * Multiple threads can concurrently access this device. Only the open() and
 * close() methods are synchronized.
 * </p>
 * 
 * @author Bastian Lemke
 */
public interface Device {

    // TODO: throw Exception if isn't initialized and read()/write()/.. is
    // called

    /**
     * Initialize this Device, e.g. create the connection from an iSCSI
     * initiator to a target.
     * 
     * @throws Exception
     *             if any error occurs.
     */
    public void open() throws Exception;

    /**
     * Close this device. Clean up everything e.g. close connections between
     * iSCSI initiator and target.
     * 
     * @throws Exception
     *             if any error occurs.
     */
    public void close() throws Exception;

    /**
     * Get the number of bytes in one block on this device.
     * 
     * @return the blocksize of the Device.
     */
    public int getBlockSize();

    /**
     * Get the number of blocks available on this device.
     * 
     * @return the number of blocks available on this device.
     */
    public long getBlockCount();

    /**
     * Get the name of this device.
     * 
     * @return the name of this device.
     */
    public String getName();

    /**
     * Read data from this device. <code>data.length</code> bytes from
     * blockaddress <code>address</code> will be read and written to
     * <code>data</code>
     * 
     * @param address
     *            logical blockaddress to read data from.
     * @param data
     *            empty byte array to write read data to. This byte array must
     *            be a multiple of blocksize.
     * @throws Exception
     *             if any error occurs.
     */
    public void read(final long address, final byte[] data) throws Exception;

    /**
     * Write data to this device. All bytes from <code>data</code> will be
     * written to blockaddress <code>address</code> on this device.
     * 
     * @param address
     *            logical blockaddress to write data to.
     * @param data
     *            byte array with the data to write. This byte array must be a
     *            multiple of blocksize.
     * @throws Exception
     *             if any error occurs.
     */
    public void write(final long address, final byte[] data) throws Exception;

}
