
package org.jscsi.scsi.target;

import java.util.List;

import org.jscsi.scsi.authentication.AuthenticationHandler;
import org.jscsi.scsi.lu.LogicalUnit;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.tasks.management.TaskManagementFunction;
import org.jscsi.scsi.tasks.management.TaskServiceResponse;
import org.jscsi.scsi.transport.Nexus;
import org.jscsi.scsi.transport.TargetTransportPort;

/**
 * A SCSI Target. The Target class provides a Target Name, a Task Router, and zero or more
 * Authentication Handlers.
 * <p>
 * The Target Name is used by the transport layer for any discovery mechanisms. As a proprietary
 * requirement, the transport layer must be capable of dynamic registration and removal of target
 * devices. Targets shall be referenced by Target Names within the transport layer.
 * <p>
 * The transport layer, after reading in the entire incoming data stream, will enqueue incoming
 * commands and a buffer containing the incoming data to the Target's Task Router. The Task Router
 * processes any commands sent to invalid Logical Units or non-LU commands. Other commands are
 * forwarded to the proper Logical Unit. Return data from Logical Units is sent directly to the
 * transport layer, bypassing the router.
 * <p>
 * Authentication handlers are used during the login phase for iSCSI transport. This authentication
 * architecture is a proprietary extension designed to support Logical Units which require
 * authentication. Authentication handlers are ignored for SCSI transport other than iSCSI.
 * 
 */
public interface Target
{

   void enqueue(TargetTransportPort port, Command command);
   
   TaskServiceResponse execute(Nexus nexus, TaskManagementFunction function);

   ////////////////////////////////////////////////////////////////////////////////////////////////
   // getters/setters

   /**
    * The Target Device Name of this target.
    */
   public String getTargetName();

   /**
    * A list of authentication handlers for this target. The authentication methods must be
    * presented to the initiator in the order returned by this list.
    */
   public List<AuthenticationHandler> getAuthHandlers();

   /**
    * A list of authentication handlers for this target. The authentication methods must be
    * presented to the initiator in the order returned by this list.
    */
   public void setAuthHandlers(List<AuthenticationHandler> handlers);

   /**
    * Used by the TargetTransportPort to indicate an I_T Nexus loss event. Indicates to each Logical
    * Unit that an I_T Nexus loss has occurred.
    */
   public void nexusLost();

   /**
    * Register a Logical Unit with the given LUN.
    * 
    * @param number The Logical Unit Number.
    * @param lu The Logical Unit.
    * @throws Exception If the LUN is already assigned.
    */
   void registerLogicalUnit( long id, LogicalUnit lu ) throws Exception;
   
   /**
    * Remove a Logical Unit from the task router. After removal no further commands will be
    * sent to the LU.
    * 
    * @param number The LUN.
    * @throws Exception If the LUN is not valid.
    */
   void removeLogicalUnit( long id ) throws Exception;
}
