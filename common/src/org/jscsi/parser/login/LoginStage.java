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
 * $Id: LoginStage.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.login;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>LoginStage</h1>
 * <p>
 * This enumeration defines all valid constants for the Login Stages used in the
 * fields <em>Current Stage (CSG)</em> and <em>Next Stage(NSG)</em> fields
 * of a Login Request message. This values are defined in the iSCSI Protocol
 * (RFC3720).
 * 
 * @author Volker Wildi
 */
public enum LoginStage {

  /** The Security Negotiation Flag. */
  SECURITY_NEGOTIATION((byte) 0),

  /** The Login Operational Negotiation Flag. */
  LOGIN_OPERATIONAL_NEGOTIATION((byte) 1),

  /** The Full Feature Phase Flag. */
  FULL_FEATURE_PHASE((byte) 3);

  private final byte value;

  private static Map<Byte, LoginStage> mapping;

  private LoginStage(final byte newValue) {

    if (LoginStage.mapping == null) {
      LoginStage.mapping = new HashMap<Byte, LoginStage>();
    }

    LoginStage.mapping.put(newValue, this);
    value = newValue;
  }

  /**
   * Returns the value of this enumeration.
   * 
   * @return The value of this enumeration.
   */
  public final byte value() {

    return value;
  }

  /**
   * Returns the constant defined for the given <code>value</code>.
   * 
   * @param value
   *          The value to search for.
   * @return The constant defined for the given <code>value</code>. Or
   *         <code>null</code>, if this value is not defined by this
   *         enumeration.
   */
  public static final LoginStage valueOf(final byte value) {

    return LoginStage.mapping.get(value);
  }

}
