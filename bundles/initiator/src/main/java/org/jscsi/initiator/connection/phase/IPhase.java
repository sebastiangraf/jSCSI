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
package org.jscsi.initiator.connection.phase;

import java.nio.ByteBuffer;

import org.jscsi.initiator.connection.ITask;
import org.jscsi.initiator.connection.Session;
import org.jscsi.initiator.connection.TargetCapacityInformations;
import org.jscsi.parser.login.LoginStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A State Pattern. Each phase of the iSCSI Protocol must implement this
 * interface.
 * 
 * @author Volker Wildi
 */
public interface IPhase {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This method handles the login of a connection (if possible).
     * 
     * @param session
     *            The context object of the current session.
     * @throws Exception
     *             if any error occurs.
     * @return true if task is finished
     */
    public boolean login(final Session session) throws Exception;

    /**
     * This method handles the logout of a connection (if possible) (if possible
     * in the current phase).
     * 
     * @param session
     *            The context object of the current session.
     * @param connectionID
     *            The ID of the connection to close.
     * @return true if task is finished
     * @throws Exception
     *             if any error occurs.
     */
    public boolean logoutConnection(final Session session, final short connectionID) throws Exception;

    /**
     * This method handles the logout of the whole session (with all its
     * connections) (if possible in the current phase).
     * 
     * @param task
     *            The calling Task
     * @param session
     *            The context object of the current session.
     * @return true if task is finished
     * @throws Exception
     *             if any error occurs.
     */
    public boolean logoutSession(final ITask task, final Session session) throws Exception;

    /**
     * This method handles a read operation within this session (if possible in
     * the current phase).
     * 
     * @param task
     *            The calling Task
     * @param session
     *            The context object of the current session.
     * @param dst
     *            The buffer to store the read data.
     * @param logicalBlockAddress
     *            The logical block address to start the read operation.
     * @param length
     *            The number of bytes to read.
     * @return true if task is finished
     * @throws Exception
     *             if any error occurs.
     */
    public boolean read(final ITask task, final Session session, final ByteBuffer dst,
        final int logicalBlockAddress, final long length) throws Exception;

    /**
     * This method handles a write operation within this session (if possible in
     * the current phase).
     * 
     * @param task
     *            The calling Task
     * @param session
     *            The context object of the current session.
     * @param src
     *            Write the remaining bytes to the iSCSI Target.
     * @param logicalBlockAddress
     *            The logical block address to start the write operation.
     * @param length
     *            The number of bytes to write.
     * @return true if task is finished
     * @throws Exception
     *             if any error occurs.
     */
    public boolean write(final ITask task, final Session session, final ByteBuffer src,
        final int logicalBlockAddress, final long length) throws Exception;

    /**
     * This method handles the <code>TargetCapacityInformations</code> within
     * this session (if possible in the current phase).
     * 
     * @param session
     *            The context object of the current session.
     * @param capacityInformation
     *            A <code>TargetCapacityInformations</code> instance to store
     *            these informations.
     * @return true if task is finished
     * @throws Exception
     *             if any error occurs.
     */
    public boolean getCapacity(final Session session, final TargetCapacityInformations capacityInformation)
        throws Exception;

    /**
     * Returns the current stage.
     * 
     * @return The current stage.
     * @see LoginStage
     */
    public LoginStage getStage();

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
}

/**
 * This abstract class contains the basic implementation of a phase. Each method
 * should throw an <code>UnsupportedOperationException</code> to indicate
 * incomplete behavior.
 * 
 * @author Volker Wildi
 */
abstract class AbstractPhase implements IPhase {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The Logger interface. */
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractPhase.class);

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor to create a new, empty <code>AbstractPhase</code> object.
     */
    protected AbstractPhase() {

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public boolean login(final Session session) throws Exception {

        throw new UnsupportedOperationException("This operation is not possible in the current phase.");
    }

    /** {@inheritDoc} */
    public boolean logoutConnection(final Session session, final short connectionID) throws Exception {

        throw new UnsupportedOperationException("This operation is not possible in the current phase.");
    }

    /** {@inheritDoc} */
    public boolean logoutSession(final ITask task, final Session session) throws Exception {

        throw new UnsupportedOperationException("This operation is not possible in the current phase.");
    }

    /** {@inheritDoc} */
    public boolean read(final ITask task, final Session session, final ByteBuffer dst,
        final int logicalBlockAddress, final long length) throws Exception {

        throw new UnsupportedOperationException("This operation is not possible in the current phase.");

    }

    /** {@inheritDoc} */
    public boolean write(final ITask task, final Session session, final ByteBuffer src,
        final int logicalBlockAddress, final long length) throws Exception {

        throw new UnsupportedOperationException("This operation is not possible in the current phase.");
    }

    /** {@inheritDoc} */
    public boolean getCapacity(final Session session, final TargetCapacityInformations capacityInformation)
        throws Exception {

        throw new UnsupportedOperationException("This operation is not possible in the current phase.");
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public LoginStage getStage() {

        throw new UnsupportedOperationException();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
}
