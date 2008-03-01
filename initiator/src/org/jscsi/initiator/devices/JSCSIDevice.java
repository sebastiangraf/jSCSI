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
 * $Id: JSCSIDevice.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.initiator.devices;

import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.initiator.Configuration;
import org.jscsi.initiator.Initiator;

/**
 * <h1>JSCSIDevice</h1>
 * 
 * <p>
 * Implements a jSCSI Device i.e. an initiator that can connect to one target.
 * </p>
 * 
 * @author Bastian Lemke
 * 
 */
public class JSCSIDevice implements Device {

  private final Initiator initiator;

  private final String target;

  private int blockSize = -1;

  private long blockCount = -1;

  /** The Logger interface. */
  private static final Log LOGGER = LogFactory.getLog(JSCSIDevice.class);

  /**
   * Constructor to create an JSCSIDevice. The Device has to be initialized
   * before it can be used.
   * 
   * @param targetName
   *          name of the target to connect to
   * 
   * @throws Exception
   *           if any error occurs
   */
  public JSCSIDevice(final String targetName) throws Exception {

    initiator = new Initiator(Configuration.create());
    target = targetName;
  }

  /** {@inheritDoc} */
  public void close() throws Exception {

    if (initiator == null) {
      throw new NullPointerException();
    }

    initiator.closeSession(target);
    blockSize = -1;
    blockCount = -1;
    LOGGER.info("Closed " + getName() + ".");
  }

  /** {@inheritDoc} */
  public int getBlockSize() {

    if (blockSize == -1) {
      throw new IllegalStateException("You first have to open the Device!");
    }

    return blockSize;
  }

  /** {@inheritDoc} */
  public String getName() {

    return "JSCSIDevice(" + target + ")";
  }

  /** {@inheritDoc} */
  public long getBlockCount() {

    if (blockCount == -1) {
      throw new IllegalStateException("You first have to open the Device!");
    }

    return blockCount;
  }

  /** {@inheritDoc} */
  public void open() throws Exception {

    if (blockCount != -1) {
      throw new IllegalStateException("JSCSIDevice is already opened!");
    }

    initiator.createSession(target);
    blockSize = (int) initiator.getBlockSize(target);
    blockCount = initiator.getCapacity(target);

    LOGGER.info("Initialized " + getName() + ".");
  }

  /** {@inheritDoc} */
  public void read(final long address, final byte[] data) throws Exception {

    if (blockCount == -1) {
      throw new IllegalStateException("You first have to open the Device!");
    }

    long blocks = data.length / blockSize;

    if (address < 0 || address + blocks > blockCount) {
      long adr;
      if (address < 0) {
        adr = address;
      } else {
        adr = address + blocks - 1;
      }
      throw new IllegalArgumentException("Address " + adr + " out of range!");
    }

    if (data.length % blockSize != 0) {
      throw new IllegalArgumentException(
          "Number of bytes is not a multiple of the blocksize!");
    }

    final ByteBuffer dst = ByteBuffer.allocate(data.length);
    initiator.read(this, target, dst, (int) address, data.length);
    dst.rewind();
    dst.get(data);
  }

  /** {@inheritDoc} */
  public void write(final long address, final byte[] data) throws Exception {

    if (blockCount == -1) {
      throw new IllegalStateException("You first have to open the Device!");
    }

    long blocks = data.length / blockSize;

    if (address < 0 || address + blocks > blockCount) {
      long adr;
      if (address < 0) {
        adr = address;
      } else {
        adr = address + blocks - 1;
      }
      throw new IllegalArgumentException("Address " + adr + " out of range.");
    }

    if (data.length % blockSize != 0) {
      throw new IllegalArgumentException(
          "Number of bytes is not a multiple of the blocksize!");
    }

    final ByteBuffer src = ByteBuffer.wrap(data);
    initiator.write(this, target, src, (int) address, data.length);
  }

}
