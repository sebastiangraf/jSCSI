//Cleversafe open-source code header - Version 1.1 - December 1, 2006
//
//Cleversafe Dispersed Storage(TM) is software for secure, private and
//reliable storage of the world's data using information dispersal.
//
//Copyright (C) 2005-2007 Cleversafe, Inc.
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
//USA.
//
//Contact Information: 
// Cleversafe, 10 W. 35th Street, 16th Floor #84,
// Chicago IL 60616
// email: licensing@cleversafe.org
//
//END-OF-HEADER
//-----------------------
//@author: John Quigley <jquigley@cleversafe.com>
//@date: January 1, 2008
//---------------------

package org.jscsi.scsi.protocol.cdb;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.jscsi.scsi.protocol.Serializer;

public class CDBFactory implements Serializer
{
   private static Map<Integer, Class<? extends CDB>> _cdbs =
      new HashMap<Integer, Class<? extends CDB>>();

   static
   {
      CDBFactory.register(Inquiry.OPERATION_CODE, Inquiry.class);
      CDBFactory.register(ModeSelect10.OPERATION_CODE, ModeSelect10.class);
      CDBFactory.register(ModeSelect6.OPERATION_CODE, ModeSelect6.class);
      CDBFactory.register(ModeSense10.OPERATION_CODE, ModeSense10.class);
      CDBFactory.register(ModeSense6.OPERATION_CODE, ModeSense6.class);
      CDBFactory.register(Read6.OPERATION_CODE, Read6.class);
      CDBFactory.register(Read10.OPERATION_CODE, Read10.class);
      CDBFactory.register(Read12.OPERATION_CODE, Read12.class);
      CDBFactory.register(Read16.OPERATION_CODE, Read16.class);
      CDBFactory.register(ReadCapacity10.OPERATION_CODE, ReadCapacity10.class);
      CDBFactory.register(ReadCapacity16.OPERATION_CODE, ReadCapacity16.class);
      CDBFactory.register(ReceiveDiagnosticResults.OPERATION_CODE, ReceiveDiagnosticResults.class);
      CDBFactory.register(ReportLuns.OPERATION_CODE, ReportLuns.class);
      CDBFactory.register(ReportSupportedTaskManagementFunctions.OPERATION_CODE,
            ReportSupportedTaskManagementFunctions.class);
      CDBFactory.register(RequestSense.OPERATION_CODE, RequestSense.class);
      CDBFactory.register(SendDiagnostic.OPERATION_CODE, SendDiagnostic.class);
      CDBFactory.register(TestUnitReady.OPERATION_CODE, TestUnitReady.class);
      CDBFactory.register(Write6.OPERATION_CODE, Write6.class);
      CDBFactory.register(Write10.OPERATION_CODE, Write10.class);
      CDBFactory.register(Write12.OPERATION_CODE, Write12.class);
      CDBFactory.register(Write16.OPERATION_CODE, Write16.class);
   }

   protected static void register(int operationCode, Class<? extends CDB> cdb)
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
   @SuppressWarnings("unchecked")
   public CDB decode(ByteBuffer input) throws IOException
   {
      byte[] opcode = new byte[1];
      input.duplicate().get(opcode); // Read in the operation code without changing the position
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(opcode));

      int operationCode = in.readUnsignedByte();

      if (!_cdbs.containsKey(operationCode))
      {
         throw new IOException(String.format(
               "Could not create new cdb with unsupported operation code: %x", operationCode));
      }

      try
      {
         CDB cdb = _cdbs.get(operationCode).newInstance();
         cdb.decode(null, input);
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
