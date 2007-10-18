
package org.jscsi.scsi.protocol.sense.additional;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

// TODO: Describe class or interface
public abstract interface SenseKeySpecificField
{

   public abstract void decode( DataInputStream input )
         throws BufferUnderflowException, IOException;
   
   public abstract void encode( DataOutputStream output ) throws BufferOverflowException;
   
   
}


