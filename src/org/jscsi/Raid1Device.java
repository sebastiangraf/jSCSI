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
 * $Id: Raid1Device.java 2642 2007-04-10 09:46:49Z lemke $
 * 
 */

package org.jscsi;

import java.nio.ByteBuffer;
import java.util.Vector;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <h1>Raid1Device</h1>
 * 
 * <p>
 * Implements a RAID 1 Device with several iSCSI Targets.
 * </p>
 * 
 * @author Bastian Lemke
 * 
 */
public class Raid1Device implements Device {

  private final Initiator initiator;

  private final String[] targets;

  /** Blocksize of the targets. */
  private int blockSize = -1;

  /** Available size on this Raid1Device. */
  private long size = -1;

  /** Pointer to next target to read data from. */
  private int targetPointer;

  /** Thread pool for write- and read-threads. */
  private final ExecutorService executor;

  /** Thread barrier for write- and read-threads. */
  private CyclicBarrier barrier;

  /**
   * Constructor to create an Raid1Device. The Device has to be initialized
   * before it can be used.
   * 
   * @param targetNames
   *          names of the targets
   * 
   * @throws Exception
   *           if any error occurs
   */
  public Raid1Device(final String[] targetNames) throws Exception {

    initiator = new Initiator(Configuration.create());
    targets = targetNames;
    targetPointer = 0;
    executor = Executors.newFixedThreadPool(targets.length);
  }

  /** {@inheritDoc} */
  public void close() throws Exception {

    if (initiator == null) {
      throw new NullPointerException();
    }

    for (int i = 0; i < targets.length; i++) {
      initiator.closeSession(targets[i]);
    }
    executor.shutdown();
  }

  /** {@inheritDoc} */
  public int getBlockSize() {
    
    if (blockSize == -1){
      throw new IllegalStateException("You first have to open the Device!");
    }

    return blockSize;
  }

  /** {@inheritDoc} */
  public String getName() {

    String name = "RAID-1";
    for (String t : targets) {
      name += "_" + t;
    }
    return name;
  }

  /** {@inheritDoc} */
  public long getBlockCount() {
    
    if (size == -1){
      throw new IllegalStateException("You first have to open the Device!");
    }

    return size;
  }

  /** {@inheritDoc} */
  public void open() throws Exception {

    blockSize = 0;
    for (String t : targets) {
      initiator.createSession(t);
      if (blockSize == 0) {
        blockSize = (int) initiator.getBlockSize(t);
      } else if (blockSize != (int) initiator.getBlockSize(t)) {
        throw new IllegalArgumentException(
            "All targets must have the same block size!");
      }
    }

    /* Find the smallest target */
    size = Long.MAX_VALUE;
    for (String t : targets) {
      size = Math.min(size, initiator.getCapacity(t));
    }

  }

  /** {@inheritDoc} */
  public void read(final long address, final byte[] data) throws Exception {
    
    if (size == -1 || blockSize == -1){
      throw new IllegalStateException("You first have to open the Device!");
    }

    int blocks = data.length / blockSize;

    if (address < 0 || address + blocks > size) {
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

    int parts = (blocks >= targets.length) ? targets.length : (int) blocks;
    barrier = new CyclicBarrier(parts + 1);
    int targetBlockCount;
    Vector<byte[]> targetData = new Vector<byte[]>();
    int targetBlockAddress = (int) address;

    for (int i = 0; i < parts; i++) {
      targetBlockCount = blocks / targets.length;
      if (i < (blocks % targets.length)) {
        targetBlockCount++;
      }
      targetData.add(new byte[targetBlockCount * blockSize]);

      /** Start Thread to read Data from Target */
      if (targetBlockCount != 0) {
        executor.execute(new ReadThread(targets[targetPointer],
            targetBlockAddress, targetData.get(i)));
      }
      targetBlockAddress += targetBlockCount;
      targetPointer = (targetPointer < targets.length - 1) ? targetPointer + 1
          : 0;
    }
    barrier.await();

    /** Merge the results. */
    int pos = 0;
    for (int i = 0; i < targetData.size(); i++) {
      System.arraycopy(targetData.get(i), 0, data, pos,
          targetData.get(i).length);
      pos += targetData.get(i).length;
    }
  }

  /** {@inheritDoc} */
  public void write(final long address, final byte[] data) throws Exception {
    
    if (size == -1 || blockSize == -1){
      throw new IllegalStateException("You first have to open the Device!");
    }

    long blocks = data.length / blockSize;

    if (address < 0 || address + blocks > size) {
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

    barrier = new CyclicBarrier(targets.length + 1);
    for (int i = 0; i < targets.length; i++) {
      executor.execute(new WriteThread(targets[i], (int) address, data));
    }
    barrier.await();
  }

  /**
   * Private class to represent one single read-action in an own thread for one
   * target.
   * 
   * @author bastian
   * 
   */
  private final class ReadThread implements Runnable {

    private final String target;

    private final int logicalBlockAddress;

    private final byte[] data;

    private ReadThread(final String readTarget, final int readBlockAddress,
        final byte[] readData) {

      target = readTarget;
      logicalBlockAddress = readBlockAddress;
      data = readData;
    }

    public void run() {

      try {
        final ByteBuffer dst = ByteBuffer.allocate(data.length);
        initiator.read(this, target, dst, logicalBlockAddress, data.length);
        dst.rewind();
        dst.get(data);
        barrier.await();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Private class to represent one single write-action in an own thread for one
   * target.
   * 
   * @author Bastian Lemke
   * 
   */
  private final class WriteThread implements Runnable {

    private final String target;

    private final int logicalBlockAddress;

    private final byte[] data;

    private WriteThread(final String writeTarget, final int writeBlockAddress,
        final byte[] writeData) {

      target = writeTarget;
      logicalBlockAddress = writeBlockAddress;
      data = writeData;
    }

    public void run() {

      try {
        final ByteBuffer src = ByteBuffer.wrap(data);
        initiator.write(this, target, src, logicalBlockAddress, data.length);
        barrier.await();
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }
}
