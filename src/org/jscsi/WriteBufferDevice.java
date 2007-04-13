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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class WriteBufferDevice implements Device {

  private final Device device;

  private static final long FLUSH_TIME = 5000;

  private static final int MAX_WRITE_COUNT = 10;

  private Map<Long, byte[]> buffer;

  private FlushThread flushThread;

  private int writeCount;

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
  public WriteBufferDevice(final Device initDevice) throws Exception {

    this.device = initDevice;
    buffer = new HashMap<Long, byte[]>();
    flushThread = new FlushThread();
    flushThread.start();
    writeCount = 0;
  }

  /** {@inheritDoc} */
  public void close() throws Exception {

    flushThread.interrupt();
    flush();
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
   * Flush the buffer to the target.
   * 
   * @throws Exception
   */
  private final synchronized void flush() throws Exception {

    List<Long> sortedKeys = new ArrayList<Long>(buffer.keySet());
    Collections.sort(sortedKeys);

    while (sortedKeys.size() > 0) {
      long lastKey = sortedKeys.get(0);
      int length = buffer.get(lastKey).length;
      int i = 1;
      while (i < sortedKeys.size() && sortedKeys.get(i) == lastKey + 1) {
        i++;
      }
      byte[] data = new byte[length * i];
      for (int j = 0; j < i; j++) {
        System.arraycopy(buffer.get(sortedKeys.get(j)), 0, data, j * length,
            length);
      }
      device.write(lastKey, data);
      if (sortedKeys.size() != 1) {
        sortedKeys = sortedKeys.subList(i, sortedKeys.size() - 1);
      } else {
        sortedKeys.clear();
      }
    }
    buffer.clear();
    //System.out.println("buffer flushed!");
  }

  /** {@inheritDoc} */
  public void read(final long address, final byte[] data) throws Exception {

    device.read(address, data);
  }

  /** {@inheritDoc} */
  public void write(final long address, final byte[] data) throws Exception {

    writeCount++;
    if (writeCount >= MAX_WRITE_COUNT) {
      writeCount = 0;
      flush();
    }
    buffer.put(address, data);
  }

  private final class FlushThread extends Thread {

    private FlushThread() {

    }

    public void run() {

      try {
        while (true) {
          sleep(FLUSH_TIME);
          flush();
        }
      } catch (InterruptedException e) {
        // do nothing
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }
}
