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
 * $Id: NullDigest.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.digest;

import java.nio.ByteBuffer;
import java.security.DigestException;

/**
 * <h1>NullDigest</h1>
 * <p>
 * This class represents an empty digest algorithm. So, this class does nothing!
 * 
 * @author Volker Wildi
 */
final class NullDigest implements IDigest {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The size of the digest number (in bytes) to serialize. */
  private static final int DIGEST_SIZE = 0;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new, empty <code>NullDigest</code> object.
   */
  public NullDigest() {

  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public void update(final ByteBuffer data, final int off, final int len) {

  }

  /** {@inheritDoc} */
  public void update(final int b) {

    // do nothing
  }

  /** {@inheritDoc} */
  public void update(final byte[] b, final int off, final int len) {

    // do nothing
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final long getValue() {

    return 0;
  }

  /** {@inheritDoc} */
  public final void reset() {

    // do nothing
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final void validate() throws DigestException {

    // do nothing
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final int getSize() {

    return DIGEST_SIZE;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
