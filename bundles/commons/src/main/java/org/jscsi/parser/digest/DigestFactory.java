/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * DigestFactory.java 2500 2007-03-05 13:29:08Z lemke $
 */

package org.jscsi.parser.digest;

/**
 * <h1>DigestFactory</h1> <p/> A factory to create instances of the supported
 * digest algorithms.
 * 
 * @author Volker Wildi
 */
public final class DigestFactory {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create new, empty <code>DigestFactory</code> instance.
   */
  public DigestFactory() {

  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This method creates an <code>IDigest</code> instance of the given type.
   * 
   * @param digestName
   *          The name of the digest type.
   * @return The <code>IDigest</code> instance of the given type.
   */
  public final IDigest create(final String digestName) {

    IDigest digest;
    if (digestName.compareTo("None") == 0) {
      digest = new NullDigest();
    } else if (digestName.compareTo("CRC32C") == 0) {
      digest = new CRC32CDigest();
    } else {
      throw new IllegalArgumentException("Digest Type (" + digestName
          + ") is unknown.");
    }

    return digest;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
