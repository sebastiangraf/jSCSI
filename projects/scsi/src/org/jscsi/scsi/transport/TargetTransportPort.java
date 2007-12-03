
package org.jscsi.scsi.transport;

import java.nio.ByteBuffer;

import org.jscsi.core.scsi.Status;
import org.jscsi.scsi.target.Target;

/**
 * The SCSI Target Transport Port provides an interface to the Service Delivery Subsystem services
 * required by SCSI Target Devices.
 * <p>
 * The target port is responsible for enqueuing incoming commands onto the Task Router of the
 * appropriate SCSI target. As the Task Router does not distinguish between command and data
 * transfer services the transport port is responsible for writing all expected incoming data to a
 * byte buffer and enqueuing that buffer along with the incoming command. The transport port must
 * then process returning status, sense data, and input data buffers which are sent directly from
 * Logical Units.
 */
public interface TargetTransportPort
{
   /**
    * Performs Receive Data-Out operation. If successful all expected data will have been read to
    * the output buffer. If it failed a partial transfer may have occurred.
    * <p>
    * This method is called by task implementations for commands such as WRITE, MODE SENSE, and
    * REPORT LUNS.
    * <p>
    * This method shall normally return false if the nexus or command reference number are
    * invalid. However, if {@link #terminateDataTransfer(Nexus, long)} is called this method
    * shall throw an InterruptedException for any nexus and command reference number which
    * was terminated until those values are reused by a new incoming command.
    * <p>
    * This method shall write zero bytes and return true if the expected transfer length is zero.
    * 
    * @param nexus Generally either an I_T_L nexus or an I_T_L_Q nexus
    * @param commandReferenceNumber The command reference number associated with the nexus.
    * @param output The data output buffer which data will be written to. The transport port
    *    shall put at {@link ByteBuffer#position()} until all incoming data is written. The
    *    operation will fail if a {@link BufferOverflowException} occurs. The caller should
    *    take care to rewind or reset the buffer on completion.
    * @return True if all expected data has been written; False if no data or partial data
    *    has been written.
    */
   boolean readData(Nexus nexus, long commandReferenceNumber, ByteBuffer output)
         throws InterruptedException;

   /**
    * Performs a Send Data-In operation. If successful all expected data will have been written
    * from the input buffer. If it failed a partial transfer may have occurred.
    * <p>
    * This method is called by task implementations for commands such as READ and MODE SELECT.
    * <p>
    * This method shall normally return false if the nexus or command reference number are
    * invalid. However, if {@link #terminateDataTransfer(Nexus, long)} is called this method
    * shall throw an InterruptedException for any nexus and command reference number which
    * was terminated until those values are reused by a new incoming command.
    * <p>
    * This method shall write zero bytes and return true if the expected transfer length is zero.
    * 
    * @param nexus Generally either an I_T_L nexus or an I_T_L_Q nexus.
    * @param commandReferenceNumber The command reference number associated with the nexus.
    * @param input The data input buffer which data will be read from. The transport port shall
    *    transfer all data from {@link ByteBuffer#position()} to {@link ByteBuffer#limit()}. The
    *    expected transfer length is thus (limit - position).
    * @return True if all expected data has been written; False if no data or partial data has
    *    been written.
    */
   boolean writeData(Nexus nexus, long commandReferenceNumber, ByteBuffer input)
         throws InterruptedException;

   /**
    * Instructs the transport layer to terminate data transfer for the indicated nexus. The
    * transport layer shall throw an {@link InterruptedException} from any in-progress
    * {@link #writeData(Nexus, long, ByteBuffer)} or {@link #readData(Nexus, long, ByteBuffer)}
    * operation.
    * <p>
    * Interrupting a thread performing a read or write operation shall have the same effect.
    * <p>
    * This method does nothing if there are no in-progress data transfers for the indicated nexus.
    * <p>
    * Future attempts to transfer data using this nexus and command reference number shall
    * also throw an {@link InterruptedException} until a new incoming command reusing those
    * values appears. At that point transfer attempts must return either <code>true</code> for
    * success or <code>false</code> for failure.
    * 
    * @param nexus Generally either an I_T_L nexus or an I_T_L_Q nexus.
    * @param commandReferenceNumber
    */
   void terminateDataTransfer(Nexus nexus, long commandReferenceNumber);

   /**
    * Enqueues return data to send to the initiator indicated by the given Nexus. Used by both Task
    * Routers and Logical Units, depending on the original command.
    * 
    * @param nexus The nexus of the original SCSI request.
    * @param commandReferenceNumber The command reference number associated with the nexus.
    * @param status The final status of the command.
    * @param senseData Autosense data; <code>null</code> if the status is not 
    *    {@link Status#CHECK_CONDITION}. Transfer from the sense data buffer shall occur in the
    *    same manner as the data buffer in {@link #writeData(Nexus, long, ByteBuffer)}.
    */
   void writeResponse(Nexus nexus, long commandReferenceNumber, Status status, ByteBuffer senseData);
}
