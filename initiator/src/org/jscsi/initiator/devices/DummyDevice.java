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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <h1>DummyDevice</h1>
 * 
 * <p>
 * Implements a Device that simulates reads and writes.
 * </p>
 * 
 * @author Bastian Lemke
 * 
 */
public class DummyDevice implements Device {

  private Date creationDate;

  private final int blockSize;

  private final long blockCount;

  private boolean opened = false;

  /** The Logger interface. */
  private static final Log LOGGER = LogFactory.getLog(DummyDevice.class);

  /**
   * Constructor to create an DummyDevice. The Device has to be initialized
   * before it can be used.
   * 
   * @param initBlockSize block size of the dummy device
   * @param initBlockCount number of blocks of the dummy device
   * @throws Exception if any error occurs
   */
  public DummyDevice(final int initBlockSize, final long initBlockCount)
      throws Exception {
    blockSize = initBlockSize;
    blockCount = initBlockCount;
  }

  /** {@inheritDoc} */
  public void close() throws Exception {
    LOGGER.info("Closed " + getName() + ".");
    opened = false;
  }

  /** {@inheritDoc} */
  public int getBlockSize() {

    if (!opened) {
      throw new IllegalStateException("You first have to open the Device!");
    }

    return blockSize;
  }

  /** {@inheritDoc} */
  public String getName() {

    if (!opened) {
      throw new IllegalStateException("You first have to open the Device!");
    }

    return "DummyDevice(" + creationDate.getTime() + ")";
  }

  /** {@inheritDoc} */
  public long getBlockCount() {

    if (!opened) {
      throw new IllegalStateException("You first have to open the Device!");
    }

    return blockCount;
  }

  /** {@inheritDoc} */
  public void open() throws Exception {

    if (opened) {
      throw new IllegalStateException("DummyDevice is already opened!");
    }

    opened = true;
    creationDate = new Date();

    LOGGER.info("Initialized " + getName() + ".");
  }

  /** {@inheritDoc} */
  public void read(final long address, final byte[] data) throws Exception {

    if (!opened) {
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

    // do nothing
  }

  /** {@inheritDoc} */
  public void write(final long address, final byte[] data) throws Exception {

    if (!opened) {
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

    // do nothing
  }

}
