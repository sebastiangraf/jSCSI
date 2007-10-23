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
// @author: mmotwani
//
// Date: Oct 23, 2007
//---------------------

package org.jscsi.scsi.protocol.blockdesc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// TODO: Describe class or interface
public class GeneralBlockDescriptor
{
   private byte _densityCode = -1;
   private int _numBlocks = -1;
   private int _blockLength = -1;

   public GeneralBlockDescriptor()
   {
   }

   public GeneralBlockDescriptor(byte densityCode, int numBlocks, int blockLength)
   {
      this._densityCode = densityCode;
      this._numBlocks = numBlocks;
      this._blockLength = blockLength;
   }

   public byte getDensityCode()
   {
      return this._densityCode;
   }

   public int getNumBlocks()
   {
      return this._numBlocks;
   }

   public int getBlockLength()
   {
      return this._blockLength;
   }

   public GeneralBlockDescriptor decode(DataInputStream dataIn)
   {
      try
      {
         int i0 = dataIn.readInt();
         this._densityCode = (byte) ((i0 >> 24) & (0xFF));
         this._numBlocks = i0 & 0xFFFFFF;
         this._blockLength = dataIn.readInt() & 0xFFFFFF;
         return this;
      }
      catch (IOException e)
      {
         throw new RuntimeException("Could not decode general descriptor block from input data");
      }
   }

   public void encode(DataOutputStream out)
   {
      try
      {
         out.writeInt(((((int) this._densityCode) << 24) & 0xFF000000)
               | (this._numBlocks & 0xFFFFFF));
         out.writeInt(this._blockLength & 0xFFFFFF);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Could not encode general descriptor block");
      }
   }

   public static GeneralBlockDescriptor getInstance(DataInputStream dataIn)
   {
      GeneralBlockDescriptor gbd = new GeneralBlockDescriptor();
      return gbd.decode(dataIn);
   }
}
