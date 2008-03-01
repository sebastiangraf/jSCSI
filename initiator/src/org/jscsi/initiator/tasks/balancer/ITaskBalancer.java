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
 * $Id: ITaskBalancer.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.initiator.tasks.balancer;

import java.util.List;

import org.jscsi.initiator.connection.Connection;
import org.jscsi.parser.exception.NoSuchConnectionException;

/**
 * <h1>ILoadBalancer</h1>
 * <p/>
 * 
 * This is an load balancer to distribute the workload to all opened connections
 * within a session.
 * 
 * @author Volker Wildi
 */
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
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

/**
 * <h1>AbstractLoadBalancer</h1>
 * <p/>
 * 
 * Each load balancer must extend this abstract class to support some basic
 * features.
 * 
 * @author Volker Wildi
 */
abstract class AbstractTaskBalancer implements ITaskBalancer {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** This list contains all opened connections of a iSCSI Session. */
  protected final List<Connection> connections;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new <code>AbstractLoadBalancer</code> instance,
   * which distribute the work to these connections.
   * 
   * @param initConnections
   *          The list with all opened connections.
   */
  protected AbstractTaskBalancer(final List<Connection> initConnections) {

    connections = initConnections;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public abstract Connection getConnection() throws NoSuchConnectionException;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
