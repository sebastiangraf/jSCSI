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

package org.jscsi.scsi.protocol.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

//TODO: Describe class or interface
public class ByteBufferInputStream extends InputStream
{
   private ByteBuffer buffer;

   public ByteBufferInputStream(ByteBuffer buffer)
   {
      this.buffer = buffer;
   }

   public ByteBufferInputStream(ByteBuffer buffer, boolean duplicate)
   {
      if (duplicate)
      {
         this.buffer = buffer.duplicate();
      }
      else
      {
         this.buffer = buffer;
      }
   }

   public ByteBuffer getByteBuffer()
   {
      return this.buffer;
   }

   @Override
   public int available() throws IOException
   {
      return this.buffer.remaining();
   }

   @Override
   public void close() throws IOException
   {
      super.close();
   }

   @Override
   public synchronized void mark(int arg0)
   {
      this.buffer.mark();
   }

   @Override
   public boolean markSupported()
   {
      return true;
   }

   @Override
   public int read(byte[] dst, int offset, int length) throws IOException
   {
      if (length > this.buffer.remaining())
      {
         length = this.buffer.remaining();
      }

      this.buffer.get(dst, offset, length);
      return length;
   }

   @Override
   public int read(byte[] dst) throws IOException
   {
      int length = dst.length;
      if (length > this.buffer.remaining())
      {
         length = this.buffer.remaining();
      }

      this.buffer.get(dst, 0, length);
      return length;
   }

   @Override
   public synchronized void reset() throws IOException
   {
      this.buffer.reset();
   }

   @Override
   public long skip(long length) throws IOException
   {
      if (length > this.buffer.remaining())
      {
         length = this.buffer.remaining();
      }
      buffer.position((int) (buffer.position() + length));
      return length;
   }

   @Override
   public int read() throws IOException
   {
      try
      {
         byte b = this.buffer.get();
         int t = (int) b & 0xFF;
         //int t = (int) this.buffer.get() & 0xFF;
         return t;
      }
      catch (BufferUnderflowException e)
      {
         throw new IOException("Buffer underflow: " + e.getMessage());
      }
   }

}
