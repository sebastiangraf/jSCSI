/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * LoginConstants.java 2500 2007-03-05 13:29:08Z lemke $
 */

package org.jscsi.parser.login;

/**
 * <h1>LoginConstants</h1>
 * <p>
 * This class defines only constants, which are used by the Login classes.
 * 
 * @author Volker Wildi
 */
final class LoginConstants {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** Current Stage bit mask. */
  static final int CSG_FLAG_MASK = 0x000C0000;

  /** Number of bits to shift to the current stage. */
  static final int CSG_BIT_SHIFT = 18;

  /** Next Stage bit mask. */
  static final int NSG_FLAG_MASK = 0x00030000;

  /** Bit mask, where the 11th and 12th bit are set. */
  static final int BIT_11_AND_12_FLAG_MASK = 0x00300000;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** Hidden default constructor. */
  private LoginConstants() {

  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
