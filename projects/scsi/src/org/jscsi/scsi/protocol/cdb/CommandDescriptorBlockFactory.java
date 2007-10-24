
package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class CommandDescriptorBlockFactory
{
   private static Map<Integer, Class<? extends CommandDescriptorBlock>> _cdbs =
         new HashMap<Integer, Class<? extends CommandDescriptorBlock>>();

   static
   {
      CommandDescriptorBlockFactory.register(Inquiry.OPERATION_CODE, Inquiry.class);
      CommandDescriptorBlockFactory.register(ModeSelect10.OPERATION_CODE, ModeSelect10.class);
      CommandDescriptorBlockFactory.register(ModeSelect6.OPERATION_CODE, ModeSelect6.class);
      CommandDescriptorBlockFactory.register(ModeSense10.OPERATION_CODE, ModeSense10.class);
      CommandDescriptorBlockFactory.register(ModeSense6.OPERATION_CODE, ModeSense6.class);
      CommandDescriptorBlockFactory.register(Read6.OPERATION_CODE, Read6.class);
      CommandDescriptorBlockFactory.register(Read10.OPERATION_CODE, Read10.class);
      CommandDescriptorBlockFactory.register(Read12.OPERATION_CODE, Read12.class);
      CommandDescriptorBlockFactory.register(Read16.OPERATION_CODE, Read16.class);
      CommandDescriptorBlockFactory.register(ReadCapacity10.OPERATION_CODE, ReadCapacity10.class);
      CommandDescriptorBlockFactory.register(ReadCapacity16.OPERATION_CODE, ReadCapacity16.class);
      CommandDescriptorBlockFactory.register(ReceiveDiagnosticResults.OPERATION_CODE,
            ReceiveDiagnosticResults.class);
      CommandDescriptorBlockFactory.register(ReportLuns.OPERATION_CODE, ReportLuns.class);
      CommandDescriptorBlockFactory.register(ReportSupportedTaskManagementFunctions.OPERATION_CODE,
            ReportSupportedTaskManagementFunctions.class);
      CommandDescriptorBlockFactory.register(RequestSense.OPERATION_CODE, RequestSense.class);
      CommandDescriptorBlockFactory.register(SendDiagnostic.OPERATION_CODE, SendDiagnostic.class);
      CommandDescriptorBlockFactory.register(TestUnitReady.OPERATION_CODE, TestUnitReady.class);
      CommandDescriptorBlockFactory.register(Write6.OPERATION_CODE, Write6.class);
      CommandDescriptorBlockFactory.register(Write10.OPERATION_CODE, Write10.class);
      CommandDescriptorBlockFactory.register(Write12.OPERATION_CODE, Write12.class);
      CommandDescriptorBlockFactory.register(Write16.OPERATION_CODE, Write16.class);
   }

   protected static void register(int operationCode, Class<? extends CommandDescriptorBlock> cdb)
   {
      _cdbs.put(operationCode, cdb);
   }

   /**
    * Used by iSCSI transport layer to decode CDB data off the wire.
    * 
    * @param input
    * @return
    * @throws BufferUnderflowException
    * @throws IOException
    */
   public static CommandDescriptorBlock decode(ByteBuffer input) throws BufferUnderflowException,
         IOException
   {
      byte[] opcode = new byte[1];
      input.duplicate().get(opcode); // Read in the operation code without changing the position
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
