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
 * $Id$
 * 
 */

package org.jscsi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jscsi.utils.SoftHashMap;

/**
 * <h1>Prefetcher</h1>
 * 
 * <p>
 * A simple Prefetcher for an Device.
 * </p>
 * 
 * @author Bastian Lemke
 * 
 */
public class PrefetchDevice implements Device {

  private final Device device;

  /** Thread pool for prefetching-threads. */
  private final ExecutorService executor;

  private SoftHashMap<Long, byte[]> buffer;

  private long lastBlockAddress;

  private static final int PREFETCH_LENGTH = 4;

  /**
   * Constructor to create an Prefetcher. The Device has to be initialized
   * before it can be used.
   * 
   * @param initDevice
   *          Device to prefetch
   * 
   * @throws Exception
   *           if any error occurs
   */
  public PrefetchDevice(final Device initDevice) throws Exception {

    this.device = initDevice;
    executor = Executors.newCachedThreadPool();
    buffer = new SoftHashMap<Long, byte[]>();
  }

  /** {@inheritDoc} */
  public void close() throws Exception {

    executor.shutdownNow();
    device.close();
  }

  /** {@inheritDoc} */
  public int getBlockSize() {

    return device.getBlockSize();
  }

  /** {@inheritDoc} */
  public String getName() {

    return device.getName();
  }

  /** {@inheritDoc} */
  public long getBlockCount() {

    return device.getBlockCount();
  }

  /** {@inheritDoc} */
  public void open() throws Exception {

    device.open();
  }

  /**
   * Return the actual number of blocks that are prefetched.
   * 
   * @return PREFETCH_LENGTH
   */
  public int getPrefetchLength() {

    return PREFETCH_LENGTH;
  }

  /** {@inheritDoc} */
  public void read(final long address, final byte[] data) throws Exception {

    byte[] tmpData, tmpData2 = new byte[data.length];

    if (lastBlockAddress == (address - data.length / getBlockSize())) {
      tmpData = (byte[]) buffer.get(address);
      if (tmpData == null) {
        tmpData = new byte[data.length * (PREFETCH_LENGTH + 1)];
        device.read(address, tmpData);
        System.arraycopy(tmpData, 0, data, 0, data.length);
        for (int i = 1; i <= PREFETCH_LENGTH; i++) {
          int offset = i * data.length / getBlockSize();
          System.out.println("prefetch: " + (address + offset));
          System.arraycopy(tmpData, i * data.length, tmpData2, 0, data.length);
          buffer.put((address + offset), tmpData2);
        }
      } else {
        System.out.print("hit: ");
      }
    } else {
      device.read(address, data);
    }
    lastBlockAddress = address;

    System.out.println("read address: " + address);
  }

  /** {@inheritDoc} */
  public void write(final long address, final byte[] data) throws Exception {

    device.write(address, data);
  }
}
