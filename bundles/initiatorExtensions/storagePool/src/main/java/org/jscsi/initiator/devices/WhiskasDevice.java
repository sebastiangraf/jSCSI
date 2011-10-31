/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketHubAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * <h1>JSCSIDevice</h1>
 * <p>
 * Implements a Device that generates the log events to visualize reads and
 * writes with the Wiskas Eclipse Plugin.
 * </p>
 * 
 * @author Bastian Lemke
 */
public class WhiskasDevice implements Device {

  private final Device device;

  private int blockSize = -1;

  private long blockCount = -1;

  private SocketHubAppender sha;

  private Logger logger;

  /**
   * Constructor to create an WhiskasDevice. The Device has to be initialized
   * before it can be used.
   * 
   * @param initDevice
   *          device to use
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

    sha = LogAppender.getInstance();
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
    String logMessage = device.getName() + ",r," + address + "," + data.length
        / device.getBlockSize();
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
    String logMessage = device.getName() + ",w," + address + "," + data.length
        / device.getBlockSize();
    LoggingEvent logEvent = new LoggingEvent(Logger.class.getName(), logger,
        Level.ALL, logMessage, null);
    sha.append(logEvent);

    device.write(address, data);
  }
}

/**
 * Singleton to prevent the SocketHupAppender from beeing opened more than once.
 * 
 * @author Bastian Lemke
 */
final class LogAppender {

  private static SocketHubAppender sha = null;

  private static final int SOCKET_PORT = 1986;

  private LogAppender() {

  }

  /**
   * Get an instance of the SocketHupAppender.
   * 
   * @return sha
   */
  public static synchronized SocketHubAppender getInstance() {

    if (sha == null) {
      sha = new SocketHubAppender(SOCKET_PORT);
    }
    return sha;
  }
}
