/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id: Device.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.initiator.devices;

/**
 * <h1>Device</h1>
 * <p>
 * A Device is an interface to control e.g. iSCSI connections between an
 * initiator and a target. It provides methods to read and write data.
 * </p>
 * 
 * <p>
 * Multiple threads can concurrently access this device. Only the open() and
 * close() methods are synchronized.
 * </p>
 * 
 * @author Bastian Lemke
 * 
 */
public interface Device {

  // TODO: throw Exception if isn't initialized and read()/write()/.. is called

  /**
   * Initialize this Device, e.g. create the connection from an iSCSI initiator
   * to a target.
   * 
   * @throws Exception
   *           if any error occurs.
   * 
   */
  public void open() throws Exception;

  /**
   * Close this device. Clean up everything e.g. close connections between iSCSI
   * initiator and target.
   * 
   * @throws Exception
   *           if any error occurs.
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
   *          logical blockaddress to read data from.
   * @param data
   *          empty byte array to write read data to. This byte array must be a
   *          multiple of blocksize.
   * @throws Exception
   *           if any error occurs.
   */
  public void read(final long address, final byte[] data) throws Exception;

  /**
   * Write data to this device. All bytes from <code>data</code> will be
   * written to blockaddress <code>address</code> on this device.
   * 
   * @param address
   *          logical blockaddress to write data to.
   * @param data
   *          byte array with the data to write. This byte array must be a
   *          multiple of blocksize.
   * @throws Exception
   *           if any error occurs.
   */
  public void write(final long address, final byte[] data) throws Exception;

}
