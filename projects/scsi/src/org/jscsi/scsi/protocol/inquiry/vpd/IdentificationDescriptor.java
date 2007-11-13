
package org.jscsi.scsi.protocol.inquiry.vpd;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import org.jscsi.scsi.protocol.Encodable;

public class IdentificationDescriptor implements Encodable
{
   private static Logger _logger = Logger.getLogger(IdentificationDescriptor.class);

   private IdentifierType identifierType;

   private int protocolIdentifier;
   private int codeSet;
   private boolean PIV;
   private int association;
   private int identifierLength;
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

   public void decode(byte[] header, ByteBuffer buffer) throws IOException
   {
   }

   public byte[] encode() throws IOException
   {
      // TODO Auto-generated method stub
      return null;
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
      return identifierLength;
   }

   public void setIdentifierLength(int identifierLength)
   {
      this.identifierLength = identifierLength;
   }
}
