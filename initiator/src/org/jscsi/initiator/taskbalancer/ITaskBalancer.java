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
 * <h1>ILoadBalancer</h1> <p/> This is an load balancer to distribute the
 * workload to all opened connections within a session.
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

  //--------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Releases a used connection, so that it can be used by another task.
   * 
   * @param connection 
   *          The <code>Connection</code> instance to release.
   * @throws NoSuchConnectionException
   *           If there is no such connection.
   */
  public void releaseConnection(Connection connection) throws NoSuchConnectionException;


  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

/**
 * <h1>AbstractLoadBalancer</h1> <p/> Each load balancer must extend this
 * abstract class to support some basic features.
 * 
 * @author Volker Wildi
 */
abstract class AbstractTaskBalancer implements ITaskBalancer {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** This list contains all free connections of a iSCSI Session. */
  protected LinkedBlockingQueue<Connection> freeConnections;
  
  /** This list contains all used connections of a iSCSI Session. */
  protected LinkedBlockingQueue<Connection> usedConnections;

  /** The Logger interface. */
  protected static final Log LOGGER = LogFactory.getLog(AbstractTaskBalancer.class);
  
  /** The Session. */
  protected Session session;

  

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new <code>AbstractLoadBalancer</code> instance,
   * which distribute the work to these connections.
   * 
   * @param initConnections
   *          The list with all opened connections.
   */
  protected AbstractTaskBalancer(final LinkedBlockingQueue<Connection> initConnections) {

    freeConnections = initConnections;
    usedConnections = new LinkedBlockingQueue<Connection>();
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public abstract Connection getConnection()
  throws NoSuchConnectionException;

  /** {@inheritDoc} */
  public void releaseConnection(final Connection connection) throws NoSuchConnectionException {
    if (connection != null) {
      try {
        usedConnections.remove(connection);
        freeConnections.add(connection);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}
