package org.jscsi.scsi.transport;

import java.nio.ByteBuffer;

import org.jscsi.core.scsi.Status;

/**
 * Asynchronous handler used to handle confirmation of a complete command.
 */
public interface ConfirmationHandler
{
   /**
    * Handles confirmation of a complete command.
    * 
    * @param response The service response from the transport layer.
    * @param nexus The originating command Nexus.
    * @param input The input buffer. This is the same object as passed into
    *    {@link InitiatorTransportPort#send(Command, ByteBuffer, ByteBuffer, ConfirmationHandler)}.
    *    Incoming input data is written to the input buffer before the handler is called.
    *    <code>null</code> only if the command did not require an input buffer and the send
    *    caller did not provide one. 
    * @param status The returned command status.
    * @param senseData Any autosense data.
    */
   void handle(ServiceResponse response, Nexus nexus, ByteBuffer input, Status status, ByteBuffer senseData);
}
