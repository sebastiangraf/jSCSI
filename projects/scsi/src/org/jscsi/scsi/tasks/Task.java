package org.jscsi.scsi.tasks;

import org.jscsi.scsi.protocol.Command;
import org.jscsi.scsi.transport.TargetTransportPort;

public interface Task extends Runnable
{
   /**
    * Begins execution of the task.
    * <p>
    * In production code this method is only called once. However, implementations should allow
    * multiple calls to ease testing implementations.
    * <p>
    * If {@link #abort()} was called beforehand this method must return immediately without
    * performing any actions. It must not write any response data to the target transport port.
    */
   public void run();
   
   /**
    * Attempts to abort the current task. When successful the thread running the task is
    * interrupted, outstanding data transfers are canceled, and any response data is discarded.
    * <p>
    * An abort attempt will generally fail if the task has already been completed and is in the
    * process of writing a response to the target transport port.
    * <p>
    * The task thread is not guaranteed to be ready to join immediately after this method returns.
    * <p>
    * This method must return <code>true</code> if the task has not yet started. When
    * <code>true</code> is returned this task must not write response data to the target
    * transport port and should not begin execution if {@link #run()} is called.
    * 
    * @return True if the abort attempt was successful; False if the task is complete or is in
    *    the process of writing the response data to the target transport port.
    */
   public boolean abort();
   
   /**
    * Returns the command which this task executes.
    */
   public Command getCommand();
   
   /**
    * Returns the target transport port which issued the command this task executes. The task
    * uses this transport port to transfer data and write response data to.
    */
   public TargetTransportPort getTargetTransportPort();
   
}
