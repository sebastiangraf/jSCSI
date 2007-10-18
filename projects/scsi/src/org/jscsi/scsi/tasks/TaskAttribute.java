
package org.jscsi.scsi.tasks;
import java.util.HashMap;
import java.util.Map;

/**
 * Valid task attributes as defined in SAM-3 section 8.6.1.
 */
public enum TaskAttribute
{
   /**
    * If only SIMPLE QUEUE TAG messages are used, the target may execute the
    * commands in any order that is deemed desirable within the constraints of
    * the queue management algorithm specified in the control mode page (see
    * [SAM2, 8.3.3.1]).
    */
   SIMPLE,
   /**
    * If ORDERED QUEUE TAG messages are used, the target shall execute the
    * commands in the order received with respect to other commands received
    * with ORDERED QUEUE TAG messages. All commands received with a SIMPLE
    * QUEUE TAG message prior to a command received with an ORDERED QUEUE TAG
    * message, regardless of initiator, shall be executed before that command
    * with the ORDERED QUEUE TAG message. All commands received with a SIMPLE
    * QUEUE TAG message after a command received with an ORDERED QUEUE TAG
    * message, regardless of initiator, shall be executed after that command
    * with the ORDERED QUEUE TAG message.
    */
   ORDERED,
   /**
    * A command received with a HEAD OF QUEUE TAG message is placed first in
    * the queue, to be executed next. A command received with a HEAD OF QUEUE
    * TAG message shall be executed prior to any queued I/O process.
    * Consecutive commands received with HEAD OF QUEUE TAG messages are
    * executed in a last-in-first-out order.
    */
   HEAD_OF_QUEUE,
   /**
    * If accepted, a task having the ACA attribute shall be entered into the task set in the
    * enabled task state. There shall be no more than one ACA task per task set.
    */
   ACA;


}


