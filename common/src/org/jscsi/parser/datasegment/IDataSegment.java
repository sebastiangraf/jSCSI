/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * IDataSegment.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.parser.datasegment;

import java.nio.ByteBuffer;

import org.jscsi.parser.exception.InternetSCSIException;

/**
 * <h1>IDataSegment</h1>
 * <p>
 * This interface defines all methods, which a class must to support, if it is a
 * DataSegment.
 * 
 * @author Volker Wildi
 */
public interface IDataSegment {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This method imports <code>len</code> bytes from the given
   * <code>ByteBuffer</code>.
   * 
   * @param src
   *          Source <code>ByteBuffer</code> object.
   * @param len
   *          The number of bytes to import from <code>src</code>.
   * @return The number of bytes, which are imported. Typically, this should be
   *         equal as <code>len</code>.
   */
  @Deprecated
  public int deserialize(final ByteBuffer src, final int len);

  /**
   * This method appends <code>len</code> bytes from the given
   * <code>ByteBuffer</code> at the end of the data buffer of this instance.
   * 
   * @param src
   *          Source <code>ByteBuffer</code> object.
   * @param len
   *          The number of bytes to append from <code>src</code>.
   * @return The number of bytes of the complete data buffer of this instance.
   */
  public int append(final ByteBuffer src, final int len);

  /**
   * This method exports the data buffer to the given <code>ByteBuffer</code>
   * object, which is padded to a integer number of <code>4</code> byte words.
   * 
   * @param dst
   *          Destination <code>ByteBuffer</code> object.
   * @param off
   *          Start position in <code>dst</code>, where to serialize.
   * @return The number of exported bytes (in bytes).
   */
  @Deprecated
  public int serialize(final ByteBuffer dst, final int off);

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Returns an iterator over the chunks of this data segment in proper
   * sequence.
   * 
   * @return an iterator over the chunks of this data segment in proper
   *         sequence.
   */
  public IDataSegmentIterator iterator();

  /**
   * Returns a <code>SettingsMap</code> instance of this
   * <code>IDataSegment</code> instance. This is only useful with a
   * <code>TextParameterDataSegment</code> instance.
   * 
   * @return The settings of this <code>TextParameterDataSegment</code>
   *         instance.
   * @throws InternetSCSIException
   *           if any violation of the iSCSI Standard occurs.
   */
  public SettingsMap getSettings() throws InternetSCSIException;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Clears all made settings of this object. After the call of this method,
   * this object can be reused.
   */
  public void clear();

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Returns the length, which is really used by the <code>dataBuffer</code>.
   * 
   * @return The really used length.
   */
  public int getLength();

  /**
   * Sets the data buffer to the given buffer <code>src</code>. Starting from
   * the position <code>offset</code> with length of <code>len</code>.
   * 
   * @param src
   *          The buffer to read from.
   * @param off
   *          The start offset to read from.
   * @param len
   *          The number of bytes to read.
   * @return The number of bytes really read.
   */
  public int setDataBuffer(final ByteBuffer src, final int off, final int len);

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
