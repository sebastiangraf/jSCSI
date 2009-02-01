
package org.jscsi.initiator.connection;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import org.jscsi.initiator.connection.phase.IPhase;

/**
 * <h1>ITask</h1> <p/> This interface defines all methods, which a task has to
 * support.
 * 
 * @author Volker Wildi
 */
public interface ITask {

  /**
   * This method is call, when this <code>ITask</code> instance is polled from
   * the head of the <code>taskQueue</code> to start a task.
   * 
   * @return Void nothing at all
   * @throws Exception
   *           if any error occurs.
   */
  public Void call() throws Exception;

  /**
   * Returns the instance to the calling thread of this task.
   * 
   * @return the instance of the calling object.
   */
  public Object getCaller();
}

// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------

/**
 * <h1>AbstractTask</h1> <p/> This abstract class defines all common methods,
 * for the implementing Tasks.
 * 
 * @author Volker Wildi
 */
abstract class AbstractTask implements ITask {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The invoking caller of this task. */
  protected final Object caller;

  /** The <code>Session</code> instance of this task. */
  protected final Session session;

  /** The <code>IPhase</code> instance of this task. */
  protected IPhase phase;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new, empty <code>AbstractTask</code> subclass
   * instance, which is initialized with the given values.
   * 
   * @param initCaller
   *          The invoking caller of this task.
   * @param referenceSession
   *          The session, where this task is executed in.
   */
  AbstractTask(final Object initCaller, final Session referenceSession) {

    caller = initCaller;
    session = referenceSession;
    phase = referenceSession.phase;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final Object getCaller() {

    return caller;
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {

    return getClass().getSimpleName();
  }

}

// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
/**
 * <h1>LoginTask</h1> <p/> This defines a LoginTask.
 * 
 * @author Volker Wildi
 */

final class LoginTask extends AbstractTask {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new, empty <code>LoginTask</code> instance, which
   * is initialized with the given values.
   * 
   * @param initCaller
   *          The invoking caller of this task.
   * @param referenceSession
   *          The session, where this task is executed in.
   */
  LoginTask(final Object initCaller, final Session referenceSession) {

    super(initCaller, referenceSession);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final Void call() throws Exception {

    if (phase.login(session)) {
      session.finishedTask(this);
    }
    return null;
  }

}

// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
/**
 * <h1>LogoutTask</h1> <p/> This defines a LogoutTask.
 * 
 * @author Volker Wildi
 */
final class LogoutTask extends AbstractTask {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new, empty <code>LogoutTask</code> instance, which
   * is initialized with the given values.
   * 
   * @param initCaller
   *          The invoking caller of this task.
   * @param referenceSession
   *          The session, where this task is executed in.
   */
  LogoutTask(final Object initCaller, final Session referenceSession) {

    super(initCaller, referenceSession);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final Void call() throws Exception {

    if (phase.logoutSession(this, session)) {
      session.finishedTask(this);
    }
    return null;
  }
}

// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
/**
 * <h1>IOTask</h1> <p/> This defines a IOTask.
 * 
 * @author Volker Wildi
 */
abstract class IOTask extends AbstractTask implements Callable<Void> {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The buffer to store/read the data of this IOTask. */
  protected final ByteBuffer buffer;

  /** The logical block address of the start of this task. */
  protected final int logicalBlockAddress;

  /** The length (in bytes) of this task. */
  protected final long length;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new, empty <code>IOTask</code> instance, which is
   * initialized with the given values.
   * 
   * @param initCaller
   *          The invoking caller of this task.
   * @param referenceSession
   *          The session, where this task is executed in.
   * @param dst
   *          Destination ByteBuffer.
   * @param initLogicalBlockAddress
   *          Initial logical Block Address.
   * @param initLength
   *          //TODO
   */
  IOTask(final Object initCaller, final Session referenceSession,
      final ByteBuffer dst, final int initLogicalBlockAddress,
      final long initLength) {

    super(initCaller, referenceSession);
    buffer = dst;
    logicalBlockAddress = initLogicalBlockAddress;
    length = initLength;

  }
}

// --------------------------------------------------------------------------
// --------------------------------------------------------------------------

/**
 * <h1>ReadTask</h1> <p/> This defines a ReadTask.
 * 
 * @author Volker Wildi
 */
final class ReadTask extends IOTask {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new, empty <code>ReadTask</code> instance, which is
   * initialized with the given values.
   * 
   * @param initCaller
   *          The invoking caller of this task.
   * @param referenceSession
   *          The session, where this task is executed in.
   * @param dst
   *          Destination ByteBuffer.
   * @param initLogicalBlockAddress
   *          Initial logical Block Address.
   * @param initLength
   *          //TODO
   */
  ReadTask(final Object initCaller, final Session referenceSession,
      final ByteBuffer dst, final int initLogicalBlockAddress,
      final long initLength) {

    super(initCaller, referenceSession, dst, initLogicalBlockAddress,
        initLength);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final Void call() throws Exception {

    if (phase.read(this, session, buffer, logicalBlockAddress, length)) {
      session.finishedTask(this);
    }
    return null;
  }
}

// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
/**
 * <h1>WriteTask</h1> <p/> This defines a WriteTask.
 * 
 * @author Volker Wildi
 */
final class WriteTask extends IOTask {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new, empty <code>WriteTask</code> instance, which
   * is initialized with the given values.
   * 
   * @param initCaller
   *          The invoking caller of this task.
   * @param referenceSession
   *          The session, where this task is executed in.
   * @param src
   *          Source ByteBuffer.
   * @param initLogicalBlockAddress
   *          Initial logical Block Address.
   * @param initLength
   *          //TODO
   */
  WriteTask(final Object initCaller, final Session referenceSession,
      final ByteBuffer src, final int initLogicalBlockAddress,
      final long initLength) {

    super(initCaller, referenceSession, src, initLogicalBlockAddress,
        initLength);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final Void call() throws Exception {

    if (phase.write(this, session, buffer, logicalBlockAddress, length)) {
      session.finishedTask(this);
    }
    return null;
  }
}

// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
