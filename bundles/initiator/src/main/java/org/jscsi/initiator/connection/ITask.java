/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jscsi.initiator.connection;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import org.jscsi.initiator.connection.phase.IPhase;

/**
 * <h1>ITask</h1>
 * <p/>
 * This interface defines all methods, which a task has to support.
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public interface ITask {

    /**
     * This method is call, when this <code>ITask</code> instance is polled from
     * the head of the <code>taskQueue</code> to start a task.
     * 
     * @return Void nothing at all
     * @throws Exception
     *             if any error occurs.
     */
    public Void call() throws Exception;

}

// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------
// --------------------------------------------------------------------------

/**
 * <h1>AbstractTask</h1>
 * <p/>
 * This abstract class defines all common methods, for the implementing Tasks.
 * 
 * @author Volker Wildi
 */
abstract class AbstractTask implements ITask {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

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
     *            The invoking caller of this task.
     * @param referenceSession
     *            The session, where this task is executed in.
     */
    AbstractTask(final Session referenceSession) {

        session = referenceSession;
        phase = referenceSession.phase;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

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
 * <h1>LoginTask</h1>
 * <p/>
 * This defines a LoginTask.
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
     * 
     * @param referenceSession
     *            The session, where this task is executed in.
     */
    LoginTask(final Session referenceSession) {

        super(referenceSession);
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
 * <h1>LogoutTask</h1>
 * <p/>
 * This defines a LogoutTask.
 * 
 * @author Volker Wildi
 */
final class LogoutTask extends AbstractTask {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a new, empty <code>LogoutTask</code> instance,
     * which is initialized with the given values.
     * 
     * 
     * @param referenceSession
     *            The session, where this task is executed in.
     */
    LogoutTask(final Session referenceSession) {

        super(referenceSession);
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
 * <h1>IOTask</h1>
 * <p/>
 * This defines a IOTask.
 * 
 * @author Sebastian Graf, University of Konstanz
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
     * 
     * @param referenceSession
     *            The session, where this task is executed in.
     * @param dst
     *            Destination ByteBuffer.
     * @param initLogicalBlockAddress
     *            Initial logical Block Address.
     * @param initLength
     *            length of buffer
     */
    IOTask(final Session referenceSession, final ByteBuffer dst, final int initLogicalBlockAddress,
        final long initLength) {

        super(referenceSession);
        buffer = dst;
        logicalBlockAddress = initLogicalBlockAddress;
        length = initLength;

    }
}

// --------------------------------------------------------------------------
// --------------------------------------------------------------------------

/**
 * <h1>ReadTask</h1>
 * <p/>
 * This defines a ReadTask.
 * 
 * @author Volker Wildi
 */
final class ReadTask extends IOTask {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a new, empty <code>ReadTask</code> instance, which
     * is initialized with the given values.
     * 
     * @param referenceSession
     *            The session, where this task is executed in.
     * @param dst
     *            Destination ByteBuffer.
     * @param initLogicalBlockAddress
     *            Initial logical Block Address.
     * @param initLength
     *            length of buffer
     */
    ReadTask(final Session referenceSession, final ByteBuffer dst, final int initLogicalBlockAddress,
        final long initLength) {

        super(referenceSession, dst, initLogicalBlockAddress, initLength);
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
 * <h1>WriteTask</h1>
 * <p/>
 * This defines a WriteTask.
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
     * 
     * @param referenceSession
     *            The session, where this task is executed in.
     * @param src
     *            Source ByteBuffer.
     * @param initLogicalBlockAddress
     *            Initial logical Block Address.
     * @param initLength
     *            length of buffer
     */
    WriteTask(final Session referenceSession, final ByteBuffer src, final int initLogicalBlockAddress,
        final long initLength) {

        super(referenceSession, src, initLogicalBlockAddress, initLength);
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
