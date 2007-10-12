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
 * $Id: IDigest.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.digest;

import java.nio.ByteBuffer;
import java.security.DigestException;
import java.util.zip.Checksum;

/**
 * <h1>IDigest</h1>
 * <p>
 * An interface representing a digest.
 * 
 * @author Volker Wildi
 */
public interface IDigest extends Checksum {

  /**
   * This method updates the used digest with the values of the given
   * <code>ByteBuffer</code> object.
   * 
   * @param data
   *          The values used for updating the checksum.
   * @param off
   *          Start offset.
   * @param len
   *          Length of the used values. (Must be a multiple of <code>4</code>
   *          bytes)
   */
  public void update(final ByteBuffer data, final int off, final int len);

  /**
   * This method validates the calculated checksum with the expected checksum.
   * 
   * @throws DigestException
   *           Dismatch between the calculated and expected checksum.
   */
  public void validate() throws DigestException;

  /**
   * Returns the length in bytes, which are needed to store this digest.
   * 
   * @return The number of bytes of this digest.
   */
  public int getSize();

}
