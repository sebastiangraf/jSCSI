/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * SecurityNegotiationPhase.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.initiator.connection.phase;

import org.jscsi.initiator.connection.Session;
import org.jscsi.parser.login.LoginStage;

/**
 * <h1>SecurityNegotiationPhase</h1> <p/> This class represents the Security
 * Negotiation Phase of a session. In this phase only a exchange of securtiy
 * parameters are allowed.
 * 
 * @author Volker Wildi
 */
public final class SecurityNegotiationPhase extends AbstractPhase {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  public final boolean login(final Session session) throws Exception {

    // session.addNewConnection();
    return true;
  }

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
