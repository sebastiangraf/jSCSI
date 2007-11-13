
package org.jscsi.scsi.protocol.inquiry.vpd;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

public class IdentificationDescriptor
{
   private static Logger _logger = Logger.getLogger(IdentificationDescriptor.class);

   private IdentifierType identifierType;

   private int protocolIdentifier;
   private int codeSet;
   private boolean PIV;
   private int association;
   private byte[] identifier;

   public IdentificationDescriptor()
   {
   }

   public IdentificationDescriptor(
         IdentifierType identifierType,
         int protocolIdentifier,
         int codeSet,
         boolean PIV,
         int association,
         byte[] identifier)
   {
      this.identifierType = identifierType;
      this.protocolIdentifier = protocolIdentifier;
      this.codeSet = codeSet;
      this.PIV = PIV;
      this.association = association;
      this.identifier = identifier;
   }

   public byte[] encode() throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(this.identifier.length + 4);
      DataOutputStream out = new DataOutputStream(baos);

      try
      {
         // byte 0
         out.writeByte((this.getProtocolIdentifier() << 4)|this.getCodeSet());
         
         // byte 1
         int b1 = (this.isPIV()? 1 : 0) << 7;
         b1 |= this.getAssociation() << 4;
         b1 |= this.getIdentifierType().value();
         out.writeByte(b1);
         
         // byte 2
         out.writeByte(0);
         
         // byte 3
         out.writeByte(this.identifier.length);
         
         // identifier
         out.write(this.identifier);

         return baos.toByteArray();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   
   /////////////////////////////////////////////////////////////////////////////
   // getters/setters

   public int getProtocolIdentifier()
   {
      return protocolIdentifier;
   }

   public void setProtocolIdentifier(int protocolIdentifier)
   {
      this.protocolIdentifier = protocolIdentifier;
   }

   public int getCodeSet()
   {
      return codeSet;
   }

   public void setCodeSet(int codeSet)
   {
      this.codeSet = codeSet;
   }

   public boolean isPIV()
   {
      return PIV;
   }

   public void setPIV(boolean piv)
   {
      PIV = piv;
   }

   public int getAssociation()
   {
      return association;
   }

   public void setAssociation(int association)
   {
      this.association = association;
   }

   public IdentifierType getIdentifierType()
   {
      return identifierType;
   }

   public void setIdentifierType(IdentifierType identifierType)
   {
      this.identifierType = identifierType;
   }

   public byte[] getIdentifier()
   {
      return identifier;
   }

   public void setIdentifier(byte[] identifier)
   {
      this.identifier = identifier;
   }
   
   public int getIdentifierLength()
   {
      return this.identifier.length;
   }
}
