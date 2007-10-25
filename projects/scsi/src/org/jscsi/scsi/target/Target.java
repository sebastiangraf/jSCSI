package org.jscsi.scsi.target;

import java.util.ArrayList;
import java.util.List;

import org.jscsi.scsi.authentication.AuthenticationHandler;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.tasks.TaskRouter;
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
public abstract class Target
{
   
   private String targetName;
   private TaskRouter taskRouter;
   private ModePageRegistry modePageRegistry;
   
   private List<AuthenticationHandler> authHandlers;

   abstract void enqueue( TargetTransportPort port, Command command );
   
   /**
    * Constructs a new target with a given name, task router, and set of authentication handlers.
    * 
    * @param targetName A target name compatible with the underlying transport layer. For iSCSI
    *    this must be an IQN or EUI.
    * @param router The task router. All logical units are registered with the task router.
    * @param handlers A set of zero or more authentication handlers. At the start of the iSCSI
    *    login phase the target-available authentication methods are sent to the initiator in
    *    the order returned by this handler list. Callers must ensure list uniqueness.
    */
   public Target(String targetName, TaskRouter router, ModePageRegistry modePageRegistry, List<AuthenticationHandler> handlers)
   {
      this.targetName = targetName;
      this.taskRouter = router;
      this.modePageRegistry = modePageRegistry;
      this.authHandlers = new ArrayList<AuthenticationHandler>();
      this.authHandlers.addAll(handlers);
   }

   /**
    * The Target Device Name of this target.
    */
   public String getTargetName()
   {
      return targetName;
   }

   /**
    * The Task Router of this target.
    */
   public TaskRouter getTaskRouter()
   {
      return taskRouter;
   }

   /**
    * A list of authentication handlers for this target. The authentication methods must be
    * presented to the initiator in the order returned by this list.
    */
   public List<AuthenticationHandler> getAuthenticationHandlers()
   {
      return authHandlers;
   }
   
   public ModePageRegistry getModeRegistry()
   {
      return this.modePageRegistry;
   }
}
