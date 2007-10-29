package org.jscsi.scsi.target;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jscsi.scsi.authentication.AuthenticationHandler;
import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.protocol.inquiry.InquiryDataRegistry;
import org.jscsi.scsi.protocol.mode.ModePageRegistry;
import org.jscsi.scsi.tasks.TaskFactory;
import org.jscsi.scsi.tasks.TaskRouter;
import org.jscsi.scsi.transport.TargetTransportPort;

public class GeneralTarget implements Target
{
   private static Logger _logger = Logger.getLogger(GeneralTarget.class);
   
   
   ////////////////////////////////////////////////////////////////////////////////////////////////
   // private data members
   
   
   private String _targetName;
   private TaskRouter _taskRouter;
   private ModePageRegistry _modePageRegistry;   
   private TaskFactory _targetTaskFactory;
   private InquiryDataRegistry _inquiryDataRegistry;
   private List<AuthenticationHandler> _authHandlers;

   
   ////////////////////////////////////////////////////////////////////////////////////////////////
   // constructor(s)


   /**
    * Constructs a new target with a given name, task router, and set of authentication handlers.
    * 
    * @param _targetName A target name compatible with the underlying transport layer. For iSCSI
    *    this must be an IQN or EUI.
    * @param router The task router. All logical units are registered with the task router.
    * @param handlers A set of zero or more authentication handlers. At the start of the iSCSI
    *    login phase the target-available authentication methods are sent to the initiator in
    *    the order returned by this handler list. Callers must ensure list uniqueness.
    */
   public GeneralTarget(String targetName, TaskRouter router, ModePageRegistry modePageRegistry, List<AuthenticationHandler> handlers)
   {
      this._targetName = targetName;
      this._taskRouter = router;
      this._modePageRegistry = modePageRegistry;
      this._authHandlers = new ArrayList<AuthenticationHandler>();
      this._authHandlers.addAll(handlers);
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////////////////////
   // primary methods
   
   
   public void enqueue( TargetTransportPort port, Command command )
   {
      
   }


   ////////////////////////////////////////////////////////////////////////////////////////////////
   // getters/setters
   
   /**
    * The Target Device Name of this target.
    */
   public String getTargetName()
   {
      return _targetName;
   }


   /**
    * A list of authentication handlers for this target. The authentication methods must be
    * presented to the initiator in the order returned by this list.
    */
   public List<AuthenticationHandler> getAuthHandlers()
   {
      return _authHandlers;
   }

   /**
    * A list of authentication handlers for this target. The authentication methods must be
    * presented to the initiator in the order returned by this list.
    */
   public void setAuthHandlers(List<AuthenticationHandler> handlers)
   {
      _authHandlers = handlers;
   }
}
