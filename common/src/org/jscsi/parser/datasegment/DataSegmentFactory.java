/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * DataSegmentFactory.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.parser.datasegment;

import java.nio.ByteBuffer;

/**
 * <h1>DataSegmentFactory</h1>
 * <p>
 * This factory creates a instance to a <code>DataSegment</code> object.
 * 
 * @author Volker Wildi
 */
public final class DataSegmentFactory {

  /**
   * This enumeration defines the valid stati of the data segment.
   */
  public enum DataSegmentFormat {
    /** The data segment has to be interpreted as text (login parameters). */
    TEXT,
    /** The data segment has to be interpreted as binary data. */
    BINARY,
    /**
     * The data segment has to be interpreted as a SCSI response data segment.
     */
    SCSI_RESPONSE,
    /** There is no data segment allowed. */
    NONE;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Make this contructor hidden to any class.
   */
  private DataSegmentFactory() {

  }

  /**
   * Creates a data segment of the given format and with the given chunk size
   * (in bytes). This data segment is initialized with the data of
   * <code>buffer</code>.
   * 
   * @param buffer
   *          The initialization buffer.
   * @param format
   *          A format from the <code>DataSegmentFormat</code> enumeration.
   * @param maxChunkSize
   *          The size (in bytes) of one chunk, which represents the
   *          <code>MaxRecvDataSegmentLength</code>.
   * @return The instance of an <codE>DataSegment</code> object.
   */
  public static final IDataSegment create(final ByteBuffer buffer,
      final DataSegmentFormat format, final int maxChunkSize) {

    return create(buffer, buffer.position(), buffer.remaining(), format,
        maxChunkSize);
  }

  /**
   * Creates a data segment of the given format and with the given chunk size
   * (in bytes). This data segment is initialized with the data of
   * <code>buffer</code>.
   * 
   * @param buffer
   *          The initialization buffer.
   * @param position
   *          The position of the variable <code>buffer</code>.
   * @param length
   *          The length (in bytes) to read from <code>buffer</code>.
   * @param format
   *          A format from the <code>DataSegmentFormat</code> enumeration.
   * @param maxChunkSize
   *          The size (in bytes) of one chunk, which represents the
   *          <code>MaxRecvDataSegmentLength</code>.
   * @return The instance of an <codE>DataSegment</code> object.
   */
  public static final IDataSegment create(final ByteBuffer buffer,
      final int position, final int length, final DataSegmentFormat format,
      final int maxChunkSize) {

    final IDataSegment dataSegment = DataSegmentFactory.create(format,
        maxChunkSize);
    dataSegment.setDataBuffer(buffer, position, length);

    return dataSegment;
  }

  /**
   * Creates a data segment of the given format and with the given chunk size
   * (in bytes).
   * 
   * @param format
   *          A format from the <code>DataSegmentFormat</code> enumeration.
   * @param maxChunkSize
   *          The size (in bytes) of one chunk, which represents the
   *          <code>MaxRecvDataSegmentLength</code>.
   * @return The instance of an <codE>DataSegment</code> object.
   */
  public static final IDataSegment create(final DataSegmentFormat format,
      final int maxChunkSize) {

    final IDataSegment dataSegment;
    switch (format) {
      case TEXT:
        dataSegment = new TextParameterDataSegment(maxChunkSize);
        break;

      case BINARY:
        dataSegment = new BinaryDataSegment(maxChunkSize);
        break;

      case NONE:
        dataSegment = new NullDataSegment(maxChunkSize);
        break;

      default:
        throw new IllegalArgumentException("Unknown data segment format.");
    }

    return dataSegment;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
