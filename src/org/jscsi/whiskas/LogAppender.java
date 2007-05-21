package org.jscsi.whiskas;

import org.apache.log4j.net.SocketHubAppender;

/**
 * Singleton to prevent the SocketHupAppender from beeing opened more than once.
 * @author Bastian Lemke
 *
 */
public final class LogAppender {
  private static SocketHubAppender sha = null;

  private static final int SOCKET_PORT = 1986;

  private LogAppender() {

  }

  /**
   * Get an instance of the SocketHupAppender.
   * @return sha
   */
  public static synchronized SocketHubAppender getInstance() {
    if (sha == null) {
      sha = new SocketHubAppender(SOCKET_PORT);
    }
    return sha;
  }
}
