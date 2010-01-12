/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * ITaskBalancer.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.initiator.taskbalancer;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.initiator.connection.Connection;
import org.jscsi.initiator.connection.Session;
import org.jscsi.parser.exception.NoSuchConnectionException;

/**
 * <h1>ILoadBalancer</h1>
 * <p/>
 * This is an load balancer to distribute the workload to all opened connections
 * within a session.
 * 
 * @author Volker Wildi
 */
@Deprecated
public interface ITaskBalancer {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Returns the next connection, which should used for the next task.
   * 
   * @return The <code>Connection</code> instance to use.
   * @throws NoSuchConnectionException
   *           If there is no such connection.
   */
  public Connection getConnection() throws NoSuchConnectionException;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Releases a used connection, so that it can be used by another task.
   * 
   * @param connection
   *          The <code>Connection</code> instance to release.
   * @throws NoSuchConnectionException
   *           If there is no such connection.
   */
  public void releaseConnection(Connection connection)
      throws NoSuchConnectionException;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
