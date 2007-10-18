
package org.jscsi.scsi.transport;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.Command;

/**
 * The SCSI Initiator Transport Port provides an interface to the Service Delivery Subsystem
 * services required by SCSI Initiator Devices.
 * <p>
 * Data In and Data Out services are handled internally using the input and output byte buffers
 * specified when a command is sent.
 */
public interface InitiatorTransportPort
{
   
   /**
    * Sends a command to a Target device. Most commands must have a valid I_T_L_x nexus specified.
    * Commands directed at the target device as a whole (such as REPORT LUNS) must have a valid
    * I_T nexus.
    * <p>
    * The application client must specify input and output byte buffers as required by the sent
    * command. The transport port handles reading from the output buffer and writing to the
    * input buffer. The input buffer must be large enough to handle the expected return data.
    * The output buffer must not be changed before a task complete confirmation is returned.
    * <p>
    * On task completion service response, status, and sense data are returned to the
    * confirmation handler. A same output byte buffer as passed in will also be returned.
    * Confirmations will occur asynchronously as tasks are completed by the target device.
    * 
    * @param command A command with a valid I_T or I_T_L_x nexus.
    * @param input A byte buffer ready to accept incoming data. The current write position must
    *    be the position the application client wishes incoming data to be written to. 
    *    The limit must be equal to or greater than (expected input length + current position).
    *    May be <code>null</code> if the command will not return data. It must be possible to
    *    do random (non-sequential) writes to the byte buffer.
    * @param output A byte buffer containing the desired output data. The current position must
    *    be the position from which the application client wishes to send data from.
    *    The limit must be equal to or greater than (output length + current position). May be
    *    <code>null</code> if the command will not write data. It must be possible to perform
    *    random (non-sequential) reads from the output buffer.
    * @param handler Task complete confirmation messages will be sent to this command handler.
    *    A new buffer will be allocated to store any incoming sense data to. The handler should
    *    be capable of parsing the sense data returned by the specific command.
    */
   void send( Command command, ByteBuffer input, ByteBuffer output, ConfirmationHandler handler );
   
   /**
    * Cancel a task specified by an I_T_L_Q nexus. The cancellation will not be
    * processed if the nexus object does not specify a task tag.
    * 
    * @param nexus An I_T_L_Q Nexus.
    */
   // TODO: Should this be blocking, or keep the confirmation handler?
   void cancel( Nexus nexus, ConfirmationHandler handler );
   
   
   
   
   
}


