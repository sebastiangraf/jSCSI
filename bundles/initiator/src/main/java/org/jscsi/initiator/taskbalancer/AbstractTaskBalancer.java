
package org.jscsi.initiator.taskbalancer;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.initiator.connection.Connection;
import org.jscsi.initiator.connection.Session;
import org.jscsi.parser.exception.NoSuchConnectionException;

/**
 * <h1>AbstractLoadBalancer</h1>
 * <p/>
 * Each load balancer must extend this abstract class to support some basic
 * features.
 * 
 * @author Volker Wildi
 */
public abstract class AbstractTaskBalancer {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** This list contains all free connections of a iSCSI Session. */
  protected LinkedBlockingQueue<Connection> freeConnections;

  /** The Logger interface. */
  protected static final Log LOGGER = LogFactory
      .getLog(AbstractTaskBalancer.class);

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
  protected AbstractTaskBalancer(
      final LinkedBlockingQueue<Connection> initConnections) {

    freeConnections = initConnections;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  public abstract Connection getConnection() throws NoSuchConnectionException;

  public void releaseConnection(final Connection connection)
      throws NoSuchConnectionException {

    if (connection != null) {
      try {
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