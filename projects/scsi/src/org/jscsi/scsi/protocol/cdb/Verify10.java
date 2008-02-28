//
// Cleversafe open-source code header - Version 1.1 - December 1, 2006
//
// Cleversafe Dispersed Storage(TM) is software for secure, private and
// reliable storage of the world's data using information dispersal.
//
// Copyright (C) 2005-2007 Cleversafe, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
// USA.
//
// Contact Information: Cleversafe, 10 W. 35th Street, 16th Floor #84,
// Chicago IL 60616
// email licensing@cleversafe.org
//
// END-OF-HEADER
//-----------------------
// @author: jquigley
//
// Date: Feb 27, 2008
//---------------------

package org.jscsi.scsi.protocol.cdb;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.scsi.protocol.util.ByteBufferInputStream;

public class Verify10 extends AbstractCDB
{
   public static final int OPERATION_CODE = 0x2F;
   
   private int VRPROTECT;
   private boolean DPO;
   private boolean BYTCHK;
   private int lba;
   private int groupNumber;
   private int verificationLength;
   
   public Verify10()
   {
      super(OPERATION_CODE);
   }
   
   public Verify10(int VRPROTECT, boolean DPO, boolean BYTCHK, int lba, int groupNumber, int verificationLength, int control)
   {
      super(OPERATION_CODE);
      
      this.VRPROTECT = VRPROTECT;
      this.DPO = DPO;
      this.BYTCHK = BYTCHK;
      this.lba = lba;
      this.groupNumber = groupNumber;
      this.verificationLength = verificationLength;
      super.setControl(control);
   }
   

   public void decode(byte[] header, ByteBuffer input) throws IOException
   {
      DataInputStream in = new DataInputStream(new ByteBufferInputStream(input));

      int operationCode = in.readUnsignedByte();
      if (operationCode != OPERATION_CODE)
      {
         throw new IOException("Invalid operation code: " + Integer.toHexString(operationCode));
      }
      
      int tmp = in.readUnsignedByte();
      this.VRPROTECT = tmp >>> 5;
      this.DPO = ((tmp >>> 4) & 0x01) == 1;
      this.BYTCHK = ((tmp >>> 1) & 0x01) == 1;
      this.lba = in.readInt();
      
      tmp = in.readUnsignedByte();
      this.groupNumber = tmp & 0x1F;
      this.verificationLength = in.readUnsignedShort();
      super.setControl(in.readUnsignedByte());
   }

   public byte[] encode() throws IOException
   {
      ByteArrayOutputStream cdb = new ByteArrayOutputStream(this.size());
      DataOutputStream out = new DataOutputStream(cdb);

      try
      {
         out.writeByte(OPERATION_CODE);
         
         int tmp = 0;
         tmp = (this.VRPROTECT << 5);
         tmp |= ((this.DPO ? 1 : 0) << 4);
         tmp |= ((this.BYTCHK ? 1 : 0 << 1));
         out.writeByte(tmp);
         
         out.writeInt(this.lba);
         out.writeByte(this.groupNumber);
         out.writeShort(this.verificationLength);
         out.writeByte(super.getControl());

         return cdb.toByteArray();
      }
      catch (IOException e)
      {
         throw new RuntimeException("Unable to encode CDB.");
      }
   }

   public int size()
   {
      return 10;
   }
   
   public String toString()
   {
      return "<Verify10>";
   }
}
