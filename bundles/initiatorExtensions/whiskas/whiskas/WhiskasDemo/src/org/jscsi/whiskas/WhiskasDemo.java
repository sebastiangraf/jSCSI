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
package org.jscsi.whiskas;

import java.util.Random;

import org.jscsi.initiator.devices.Device;
import org.jscsi.initiator.devices.DummyDevice;
import org.jscsi.initiator.devices.Raid0Device;
import org.jscsi.initiator.devices.WhiskasDevice;


/**
 * <h1>WhiskasDemo</h1>
 * 
 * <p>
 * Creates some log events to demonstrate the WhiskasDevice.
 * </p>
 * 
 * @author Bastian Lemke
 * 
 */
public class WhiskasDemo {

  private final Device raid0, target1, target2;

  /** Used block size. * */
  private static final int BLOCK_SIZE = 8192;

  /** Number of blocks on the dummy device. * */
  private static final int BLOCK_COUNT = 5000;

  /** Time to wait between reads/writes. */
  private static final int SLEEP_TIME = 5;

  private final byte[] data;

  private final Random random;

  /**
   * Constructor to create an WhiskasDevice. The Device has to be initialized
   * before it can be used.
   * 
   * @throws Exception
   *           if any error occurs
   */
  public WhiskasDemo() throws Exception {

    target1 = new WhiskasDevice(new DummyDevice(BLOCK_SIZE, BLOCK_COUNT));
    target2 = new WhiskasDevice(new DummyDevice(BLOCK_SIZE, BLOCK_COUNT));
    raid0 = new WhiskasDevice(
        new Raid0Device(new Device[] { target1, target2 }));
    raid0.open();

    data = new byte[BLOCK_SIZE];
    for (int i = 0; i < data.length; i++) {
      data[i] = 0;
    }

    random = new Random(System.currentTimeMillis());
  }

  private void setUp() throws Exception {

    /*
     * Wait some seconds, because WhiskasDevice has to be configured between
     * opening the SocketHubAppender and writing/reading blocks.
     */
    Thread.sleep(10000);

    raid0.write(BLOCK_COUNT - 2, data);
    raid0.write(BLOCK_COUNT - 1, data);
    raid0.read(BLOCK_COUNT - 2, data);
    raid0.read(BLOCK_COUNT - 1, data);
  }

  private void sequentialRead() throws Exception {

    for (int i = 0; i < BLOCK_COUNT; i++) {
      raid0.read(i, data);
      Thread.sleep(SLEEP_TIME);
    }
  }

  private void randomRead() throws Exception {

    for (int i = 0; i < BLOCK_COUNT; i++) {
      raid0.read(random.nextInt((int) BLOCK_COUNT), data);
      Thread.sleep(SLEEP_TIME);
    }
  }

  /**
   * Main method to start the demo.
   * 
   * @param args
   *          arguments ...
   * @throws Exception
   *           if any error occurs
   */
  public static void main(final String[] args) throws Exception {

    WhiskasDemo demo = new WhiskasDemo();

    demo.setUp();

    while (true) {
      demo.sequentialRead();
      demo.randomRead();
    }
  }
}

