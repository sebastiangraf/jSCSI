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
 * $Id: LoginOperationalNegotiationPhase.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.connection;

import org.jscsi.parser.login.LoginStage;

/**
 * <h1>LoginOperationalNegotiationPhase</h1>
 * <p/> This class represents the Login Operational Negotiation Phase of a
 * session. In this phase only further exchange of the parameters are allowed.
 * 
 * @author Volker Wildi
 */
final class LoginOperationalNegotiationPhase extends AbstractPhase {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  public final LoginStage getStage() {

    return LoginStage.LOGIN_OPERATIONAL_NEGOTIATION;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
