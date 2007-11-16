package org.jscsi.scsi.transport;

/**
 * Represents an I_T, I_T_L, or I_T_L_Q Nexus.
 * <p>
 * An I_T Nexus identification will have L and Q set to invalid (negative) values.
 * <p>
 * An I_T_L Nexus identification will have Q set to an invalid (negative) value.
 */
public class Nexus
{
   private String initiatorPortIdentifier;
   private String targetPortIdentifier;
   
   private long logicalUnitNumber;
   private long taskTag;
   
   /**
    * Contruct an I_T Nexus identification. L and Q are set to invalid (negative) values.
    * @param initiatorPortIdentifier The initiator port identifier.
    * @param targetPortIdentifier The target port identifier.
    */
   public Nexus(String initiatorPortIdentifier, String targetPortIdentifier)
   {
      super();
      this.initiatorPortIdentifier = initiatorPortIdentifier;
      this.targetPortIdentifier = targetPortIdentifier;
      this.logicalUnitNumber = -1;
      this.taskTag = -1;
   }
   
   /**
    * Construct an I_T_L Nexus identification. Q is set to an invalid (negative) value.
    * 
    * @param initiatorPortIdentifier The initiator port identifier.
    * @param targetPortIdentifier The target port identifier.
    * @param logicalUnitNumber The Logical Unit Number.
    */
   public Nexus(String initiatorPortIdentifier, String targetPortIdentifier, long logicalUnitNumber)
   {
      super();
      this.initiatorPortIdentifier = initiatorPortIdentifier;
      this.targetPortIdentifier = targetPortIdentifier;
      this.logicalUnitNumber = logicalUnitNumber;
      this.taskTag = -1;
   }

   /**
    * Construct an I_T_L_Q Nexus identification.
    * 
    * @param initiatorPortIdentifier The initiator port identifier.
    * @param targetPortIdentifier The target port identifier.
    * @param logicalUnitNumber The Logical Unit Number.
    * @param taskTag The task tag.
    */
   public Nexus(
         String initiatorPortIdentifier,
         String targetPortIdentifier,
         long logicalUnitNumber,
         long taskTag)
   {
      super();
      this.initiatorPortIdentifier = initiatorPortIdentifier;
      this.targetPortIdentifier = targetPortIdentifier;
      this.logicalUnitNumber = logicalUnitNumber;
      this.taskTag = taskTag;
   }
   
   /**
    * Construct an I_T_L_Q Nexus identification from an I_T_L Nexus identification and a task tag.
    * A convenience constructor for use with a sequence of commands using shifting task tags on
    * a stable I_T_L Nexus. 
    * 
    * @param nexus An I_T_L Nexus identification.
    * @param taskTag The task tag.
    */
   public Nexus(Nexus nexus, long taskTag )
   {
      assert nexus.logicalUnitNumber > 0 : "I_T_L_Q Nexus should be constructed from I_T_L Nexus";
      this.initiatorPortIdentifier = nexus.initiatorPortIdentifier;
      this.targetPortIdentifier = nexus.targetPortIdentifier;
      this.logicalUnitNumber = nexus.logicalUnitNumber;
      this.taskTag = taskTag;
   }
   
   /**
    * The Initiator Port Identifier.
    */
   public String getInitiatorPortIdentifier()
   {
      return initiatorPortIdentifier;
   }
   
   /**
    * The Target Port Identifier.
    */
   public String getTargetPortIdentifier()
   {
      return targetPortIdentifier;
   }
   
   /**
    * The Logical Unit Number. Negative for I_T Nexus identifiers.
    */
   public long getLogicalUnitNumber()
   {
      return logicalUnitNumber;
   }
   
   /**
    * The Task Tag. Negative for I_T and I_T_L Nexus identifiers.
    */
   public long getTaskTag()
   {
      return taskTag;
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result =
            prime * result
                  + ((initiatorPortIdentifier == null) ? 0 : initiatorPortIdentifier.hashCode());
      result = prime * result + (int) (logicalUnitNumber ^ (logicalUnitNumber >>> 32));
      result =
            prime * result + ((targetPortIdentifier == null) ? 0 : targetPortIdentifier.hashCode());
      result = prime * result + (int) (taskTag ^ (taskTag >>> 32));
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      final Nexus other = (Nexus) obj;
      if (initiatorPortIdentifier == null)
      {
         if (other.initiatorPortIdentifier != null)
            return false;
      }
      else if (!initiatorPortIdentifier.equals(other.initiatorPortIdentifier))
         return false;
      if (logicalUnitNumber != other.logicalUnitNumber)
         return false;
      if (targetPortIdentifier == null)
      {
         if (other.targetPortIdentifier != null)
            return false;
      }
      else if (!targetPortIdentifier.equals(other.targetPortIdentifier))
         return false;
      if (taskTag != other.taskTag)
         return false;
      return true;
   }
   
   @Override
   public String toString()
   {
      String output = new String();
      if (this.logicalUnitNumber == -1 || this.taskTag == -1)
      {
         output += "<I_T nexus initiatorPort: " + this.initiatorPortIdentifier
            + " targetPort: " + this.targetPortIdentifier;
      }
      else if (this.taskTag == -1)
      {
         output += "<I_T_L nexus initiatorPort: " + this.initiatorPortIdentifier
               + " targetPort: " + this.targetPortIdentifier + " LUN: "
               + this.logicalUnitNumber;
      }
      else
      {
         output += "<I_T_L_Q nexus initiatorPort: " + this.initiatorPortIdentifier
               + " targetPort: " + this.targetPortIdentifier + " LUN: "
               + this.logicalUnitNumber + " taskTag: " + this.taskTag;
      }
      output += ">";
      return output;
   }
}
