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

package org.jscsi;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketHubAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * <h1>JSCSIDevice</h1>
 * 
 * <p>
 * Implements a Device that generates the log events to visualize reads and
 * writes with the Wiskas Eclipse Plugin.
 * </p>
 * 
 * @author Bastian Lemke
 * 
 */
public class WhiskasDevice implements Device {

  private final Device device;

  private int blockSize = -1;

  private long blockCount = -1;

  private SocketHubAppender sha;

  private Logger logger;

  private static final int SOCKET_PORT = 1986;

  /**
   * Constructor to create an WhiskasDevice. The Device has to be initialized
   * before it can be used.
   * 
   * @param initDevice
   *          device to use
   * 
   * @throws Exception
   *           if any error occurs
   */
  public WhiskasDevice(final Device initDevice) throws Exception {

    device = initDevice;
  }

  /** {@inheritDoc} */
  public void close() throws Exception {

    if (blockCount == -1) {
      throw new NullPointerException();
    }

    /* Generate teardown message for Whiskas */
    String logMessage = "teardown " + device.getName();
    LoggingEvent logEvent = new LoggingEvent(Logger.class.getName(), logger,
        Level.ALL, logMessage, null);
    sha.append(logEvent);

    blockSize = -1;
    blockCount = -1;

    sha.close();
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

    return "WhiskasDevice(" + device.getName() + ")";
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
      throw new IllegalStateException("WhiskasDevice is already opened!");
    }

    device.open();

    sha = new SocketHubAppender(SOCKET_PORT);
    logger = Logger.getLogger(WhiskasDevice.class);

    blockSize = device.getBlockSize();
    blockCount = device.getBlockCount();
  }

  /** {@inheritDoc} */
  public void read(final long address, final byte[] data) throws Exception {

    if (blockCount == -1) {
      throw new IllegalStateException("You first have to open the Device!");
    }

    /* Generate log message for Whiskas */
    String logMessage = device.getName() + ",r," + address + "," + data.length;
    LoggingEvent logEvent = new LoggingEvent(Logger.class.getName(), logger,
        Level.ALL, logMessage, null);
    sha.append(logEvent);

    device.read(address, data);
  }

  /** {@inheritDoc} */
  public void write(final long address, final byte[] data) throws Exception {

    if (blockCount == -1) {
      throw new IllegalStateException("You first have to open the Device!");
    }

    /* Generate log message for Whiskas */
    String logMessage = device.getName() + ",w," + address + "," + data.length;
    LoggingEvent logEvent = new LoggingEvent(Logger.class.getName(), logger,
        Level.ALL, logMessage, null);
    sha.append(logEvent);

    device.write(address, data);
  }

}
