
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

// TODO: Describe class or interface
public class CommandDescriptorBlockFactory
{
   private static Map<Integer,Class<? extends CommandDescriptorBlock>> _cdbs =
      new HashMap<Integer,Class<? extends CommandDescriptorBlock>>();
   
   static
   {
      CommandDescriptorBlockFactory.register(Inquiry.OPERATION_CODE, Inquiry.class);
      CommandDescriptorBlockFactory.register(ModeSelect10.OPERATION_CODE, ModeSelect10.class);
      CommandDescriptorBlockFactory.register(ModeSelect6.OPERATION_CODE, ModeSelect6.class);
      CommandDescriptorBlockFactory.register(Inquiry.OPERATION_CODE, Inquiry.class);
      CommandDescriptorBlockFactory.register(Inquiry.OPERATION_CODE, Inquiry.class);
      CommandDescriptorBlockFactory.register(Inquiry.OPERATION_CODE, Inquiry.class);
      CommandDescriptorBlockFactory.register(Inquiry.OPERATION_CODE, Inquiry.class);
      CommandDescriptorBlockFactory.register(Inquiry.OPERATION_CODE, Inquiry.class);
      CommandDescriptorBlockFactory.register(Inquiry.OPERATION_CODE, Inquiry.class);
      CommandDescriptorBlockFactory.register(Inquiry.OPERATION_CODE, Inquiry.class);
      CommandDescriptorBlockFactory.register(Inquiry.OPERATION_CODE, Inquiry.class);
      CommandDescriptorBlockFactory.register(Inquiry.OPERATION_CODE, Inquiry.class);
      CommandDescriptorBlockFactory.register(Inquiry.OPERATION_CODE, Inquiry.class);
   }
   
   protected static void register(int operationCode, Class<? extends CommandDescriptorBlock> cdb)
   {
      _cdbs.put(operationCode, cdb);
   }
   
   /**
    * Used by iSCSI transport layer to decode CDB data off the wire.
    * @param input
    * @return
    * @throws BufferUnderflowException
    * @throws IOException
    */
   public static CommandDescriptorBlock decode( ByteBuffer input ) 
   throws BufferUnderflowException, IOException
   {
      byte[] opcode = new byte[1];
      input.get(opcode);
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(opcode));
      
      int operationCode = in.readUnsignedByte();
      
      try
      {
         CommandDescriptorBlock cdb = _cdbs.get(operationCode).newInstance();
         cdb.decode(input);
         return cdb;
      }
      catch (InstantiationException e)
      {
         throw new IOException("Could not create new cdb parser: " + e.getMessage());
      }
      catch (IllegalAccessException e)
      {
         throw new IOException("Could not create new cdb parser: " + e.getMessage());
      }
   }
}
