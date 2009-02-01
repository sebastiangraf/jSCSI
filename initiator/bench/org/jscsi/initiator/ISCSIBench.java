/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. 
 */

package org.jscsi.initiator;


import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Patrice Brend'amour
 */
public abstract class ISCSIBench {

 /** number of runs. */
 public static final int NUMRUNS = 5;

 /** LOGGER. */ 
 protected static final Log LOGGER = LogFactory.getLog(ISCSIBench.class);

 /** The size (in bytes) of a single block. */
 protected static final int BLOCK_SIZE = 32 * 1024;

 /** The random number generator to fill the buffer to send. */
 protected final Random randomGenerator;
 
 
 
 /**
  * The last block address of the given target for the given block size of
  * <code>BLOCK_SIZE</code> bytes.
  */
 protected long lastBlockAddress;

  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------

 
 
 public ISCSIBench() {
   randomGenerator = new Random(System.currentTimeMillis());
   lastBlockAddress = (1*1024*1024*1024)/BLOCK_SIZE;
 }
 
 
  /**
   * This Method opens an unbuffered connection to the target.
   * 
   * @throws Exception
   */
  public abstract void openConnection() throws Exception;


  /**
   * This Method closes a connection.
   * 
   * @throws Exception
   */
  public abstract void closeConnection() throws Exception;

  /**
   * Method to write one block to the target.
   * 
   * @param address
   *          logical block address to write block to
   * @param data
   *          byte array with data
   * @throws Exception
   */
  public abstract void write(final int numBlocks) throws Exception;

  /**
   * Method to read one block from the target.
   * 
   * @param address
   *          logical block address to read block from
   * @param data
   *          byte array to write read data to
   * @throws Exception
   */
  public abstract void read(int numBlocks) throws Exception;

//--------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  protected final int getLastBlockAddress(final int blocks) {

    return randomGenerator.nextInt((int) lastBlockAddress - blocks + 1);
  }


}
