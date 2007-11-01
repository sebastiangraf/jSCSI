
package org.jscsi.scsi.transport;

import java.nio.ByteBuffer;

import org.jscsi.scsi.target.Target;
import org.jscsi.scsi.tasks.Status;

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
    * Registers a SCSI Target. The target name is used by the transport services to advertise the
    * target. Incoming commands are enqueued onto the target's Task Router.
    * 
    * @param target
    *           The target to register.
    */
   void registerTarget(Target target);

   /**
    * Removes a SCSI Target. After removal no further commands will be sent to the target.
    * 
    * @param targetName
    *           The name of the target to remove.
    * @throws Exception
    *            If a target with the given name has not be registered.
    */
   void removeTarget(String targetName) throws Exception;

   /**
    * Called from the Task implementation for Write commands among others. TODO Finish
    * documentation.
    * 
    * @param nexus
    * @param commandReferenceNumber
    * @param output
    * @return
    */
   boolean readData(Nexus nexus, long commandReferenceNumber, ByteBuffer output);

   /**
    * Called from the Task implementation for Read commands among others. TODO Finish documentation.
    * 
    * @param nexus
    * @param commandReferenceNumber
    * @param input
    * @return
    */
   boolean writeData(Nexus nexus, long commandReferenceNumber, ByteBuffer input);

   void terminateDataTransfer(Nexus nexus);

   /**
    * Enqueues return data to send to the initiator indicated by the given Nexus. Used by both Task
    * Routers and Logical Units, depending on the original command.
    * 
    * @param nexus
    *           The Nexus of the original SCSI request.
    * @param commandReferenceNumber
    *           TODO
    * @param status
    *           The command status.
    * @param senseData
    *           Autosense data; <code>null</code> if a positive status was returned.
    * @param input
    *           The input buffer if return data is expected; <code>null</code> if no return data
    *           expected or a negative condition precludes returning data.
    */
   void writeResponse(Nexus nexus, long commandReferenceNumber, Status status, ByteBuffer senseData);
}
