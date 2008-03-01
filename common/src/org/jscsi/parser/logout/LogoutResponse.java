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
 * $Id: LogoutResponse.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.logout;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>LogoutResponse</h1>
 * <p>
 * This enumeration defines all the Logout Responses, which the iSCSI Standard
 * (RFC3720) defines.
 * 
 * @author Volker Wildi
 * 
 */
public enum LogoutResponse {

  /**
   * Connection or session closed successfully.
   */
  CONNECTION_CLOSED_SUCCESSFULLY((byte) 0),

  /**
   * CID not found.
   */
  CID_NOT_FOUND((byte) 1),

  /**
   * Connection recovery is not supported. If Logout reason code was recovery
   * and target does not support it as indicated by the ErrorRecoveryLevel.
   */
  CONNECTION_RECOVERY_NOT_SUPPORTED((byte) 2),

  /**
   * Cleanup failed for various reasons.
   */
  CLEANUP_FAILED((byte) 3);

  private final byte value;

  private static Map<Byte, LogoutResponse> mapping;

  private LogoutResponse(final byte newValue) {

    if (LogoutResponse.mapping == null) {
      LogoutResponse.mapping = new HashMap<Byte, LogoutResponse>();
    }

    LogoutResponse.mapping.put(newValue, this);
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
  public static final LogoutResponse valueOf(final byte value) {

    return LogoutResponse.mapping.get(value);
  }
}
