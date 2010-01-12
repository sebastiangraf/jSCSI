/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * SimpleTaskBalancer.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.initiator.taskbalancer;

import java.util.concurrent.LinkedBlockingQueue;
import org.jscsi.initiator.connection.Connection;
import org.jscsi.parser.exception.NoSuchConnectionException;

/**
 * <h1>SimpleLoadBalancer</h1>
 * <p/>
 * This simple load balancer distribute only the work to the first connection.
 * 
 * @author Volker Wildi
 */
public final class SimpleTaskBalancer extends AbstractTaskBalancer {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  /**
   * Constructor to create a <code>SimpleLoadBalancer</code> instance.
   * 
   * @param initConnections
   *          The list with all opened connections.
   */
  public SimpleTaskBalancer(
      final LinkedBlockingQueue<Connection> initConnections) {

    super(initConnections);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  public Connection getConnection() throws NoSuchConnectionException {

    Connection retConnection;
    try {
      retConnection = freeConnections.take();
      return retConnection;

    } catch (InterruptedException e) {
      e.printStackTrace();
      return null;
    }
  }
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
