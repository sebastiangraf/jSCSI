
package org.jscsi.scsi.protocol;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.cdb.CDB;
import org.jscsi.scsi.protocol.cdb.CDBFactory;
import org.jscsi.scsi.tasks.TaskAttribute;
import org.jscsi.scsi.transport.Nexus;

/**
 * The Command class contains methods needed by the SCSI Transport Protocol Services
 * and the Target Port Task Router.
 * <p>
 * A command is encoded by the Transport Protocol Services on the initiator and decoded
 * by the Transport Protocol Services on the target. This class provides enough
 * information to accomplish this.
 * <p>
 * The Task Router enqueues the command on the proper Logical Unit where it is converted into
 * a Task; some commands are processed directly by the Task Router. A Task uses the Target
 * Transport Port to directly return any data or results.
 * 
 */
public class Command
{
   private static CDBFactory _factory = new CDBFactory();

   private Nexus nexus;
   private CDB commandDescriptorBlock;
   private TaskAttribute taskAttribute;
   private long commandReferenceNumber;
   private int taskPriority;

   /**
    * Creates a Command object. The Command Descriptor Block is decoded using
    * {@link CommandDescriptorBlockFactory#decode(ByteBuffer).
    * 
    * @param nexus The I_T, I_T_L, or I_T_L_Q nexus of the given command.
    * @param commandDescriptorBlock A byte buffer containing the serialized CDB.
    * @param taskAttribute The command's task attribute.
    * @param commandReferenceNumber The command reference number.
    * @param taskPriority The task priority.
    * @throws BufferUnderflowException If the 
    * @throws IOException
    */
   public Command(
         Nexus nexus,
         ByteBuffer commandDescriptorBlock,
         TaskAttribute taskAttribute,
         long commandReferenceNumber,
         int taskPriority) throws BufferUnderflowException, IOException
   {
      super();
      this.nexus = nexus;
      this.commandDescriptorBlock = _factory.decode(commandDescriptorBlock);
      this.taskAttribute = taskAttribute;
      this.commandReferenceNumber = commandReferenceNumber;
      this.taskPriority = taskPriority;
   }

   public Command(
         Nexus nexus,
         CDB commandDescriptorBlock,
         TaskAttribute taskAttribute,
         long commandReferenceNumber,
         int taskPriority)
   {
      super();
      this.nexus = nexus;
      this.commandDescriptorBlock = commandDescriptorBlock;
      this.taskAttribute = taskAttribute;
      this.commandReferenceNumber = commandReferenceNumber;
      this.taskPriority = taskPriority;
   }

   public Nexus getNexus()
   {
      return nexus;
   }

   public CDB getCommandDescriptorBlock()
   {
      return commandDescriptorBlock;
   }

   public TaskAttribute getTaskAttribute()
   {
      return taskAttribute;
   }

   public long getCommandReferenceNumber()
   {
      return commandReferenceNumber;
   }

   public int getTaskPriority()
   {
      return taskPriority;
   }

   @Override
   public String toString()
   {
      return String.format("<Command: %s, nexus: %s, TaskAttribute: %s, CmdRef: %d>",
            this.commandDescriptorBlock, this.nexus, this.taskAttribute,
            this.commandReferenceNumber);
   }
}
