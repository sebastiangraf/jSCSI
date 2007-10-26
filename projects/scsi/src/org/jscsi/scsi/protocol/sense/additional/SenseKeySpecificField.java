
package org.jscsi.scsi.protocol.sense.additional;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

import org.jscsi.scsi.protocol.Encodable;
import org.jscsi.scsi.protocol.Serializer;

// TODO: Describe class or interface
public abstract interface SenseKeySpecificField extends Encodable, Serializer
{
   
}


