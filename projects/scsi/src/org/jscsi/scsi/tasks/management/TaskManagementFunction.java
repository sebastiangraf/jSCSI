
package org.jscsi.scsi.tasks.management;

/**
 * This enumerations defines all valid task management functions, which 
 * are defined in the iSCSI Standard (RFC 3720) and the SCSI Architecture
 * Model 2 [SAM2].
 */
public enum TaskManagementFunction
{
   ABORT_TASK,
   ABORT_TASK_SET,
   CLEAR_ACA,
   CLEAR_TASK_SET,
   LOGICAL_UNIT_RESET,
   TARGET_RESET,
   WAKEUP;
}
